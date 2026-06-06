package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.employee.dto.EmployeeLoginRequest;
import com.employee.dto.LoginResponse;
import com.employee.dto.LoginResultDTO;
import com.employee.entity.Employee;
import com.employee.entity.LoginLog;
import com.employee.mapper.EmployeeMapper;
import com.employee.utils.JwtUtil;
import com.employee.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class EmployeeAuthService {
    private final EmployeeMapper employeeMapper;
    private final AccountLockService accountLockService;
    private final LoginLogService loginLogService;
    private final SessionService sessionService;
    private final AbnormalLoginDetectionService abnormalLoginDetectionService;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public LoginResultDTO login(EmployeeLoginRequest request) {
        String email = request.getEmail();
        log.info("员工尝试登录: {}", email);
        
        if (accountLockService.isAccountLocked(email, AccountLockService.USER_TYPE_EMPLOYEE)) {
            LocalDateTime unlockTime = accountLockService.getUnlockTime(email, AccountLockService.USER_TYPE_EMPLOYEE);
            loginLogService.recordLoginLog(AccountLockService.USER_TYPE_EMPLOYEE, email, null, false, "账号已锁定");
            return LoginResultDTO.locked("账号已被锁定，请30分钟后重试", unlockTime);
        }
        
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getEmail, email);
        
        Employee employee = employeeMapper.selectOne(wrapper);
        
        if (employee == null) {
            log.warn("员工不存在: {}", email);
            loginLogService.recordLoginLog(AccountLockService.USER_TYPE_EMPLOYEE, email, null, false, "账号不存在");
            return LoginResultDTO.failure("邮箱或密码错误", 0);
        }
        
        if (employee.getStatus() == 0) {
            log.warn("员工已离职: {}", email);
            loginLogService.recordLoginLog(AccountLockService.USER_TYPE_EMPLOYEE, email, employee.getId(), false, "账号已离职");
            return LoginResultDTO.failure("账号未激活或已停用", 0);
        }
        
        boolean passwordMatched = false;
        boolean isFirstLogin = false;
        
        if (employee.getPassword() == null) {
            String initialPassword = getInitialPassword(employee.getPhone());
            if (request.getPassword().equals(initialPassword)) {
                employee.setPassword(PasswordUtil.encode(initialPassword));
                employeeMapper.updateById(employee);
                passwordMatched = true;
                isFirstLogin = true;
            }
        } else {
            passwordMatched = PasswordUtil.matches(request.getPassword(), employee.getPassword());
        }
        
        if (!passwordMatched) {
            log.warn("员工密码错误: {}", email);
            accountLockService.recordFailedAttempt(email, AccountLockService.USER_TYPE_EMPLOYEE);
            int remainingAttempts = accountLockService.getRemainingAttempts(email, AccountLockService.USER_TYPE_EMPLOYEE);
            
            String failureReason = "密码错误";
            if (remainingAttempts <= 0) {
                failureReason = "密码错误次数过多，账号已锁定";
            }
            loginLogService.recordLoginLog(AccountLockService.USER_TYPE_EMPLOYEE, email, employee.getId(), false, failureReason);
            
            if (remainingAttempts <= 0) {
                LocalDateTime unlockTime = accountLockService.getUnlockTime(email, AccountLockService.USER_TYPE_EMPLOYEE);
                return LoginResultDTO.locked("账号已被锁定，请30分钟后重试", unlockTime);
            }
            
            return LoginResultDTO.failure("邮箱或密码错误，还可尝试" + remainingAttempts + "次", remainingAttempts);
        }
        
        accountLockService.resetFailedAttempts(email, AccountLockService.USER_TYPE_EMPLOYEE);
        
        String token = jwtUtil.generateEmployeeToken(employee.getId(), employee.getEmail());
        
        String ipAddress = loginLogService.getCurrentIp();
        String deviceType = loginLogService.getCurrentDeviceType();
        
        sessionService.createSession(
            AccountLockService.USER_TYPE_EMPLOYEE,
            employee.getId(),
            token,
            ipAddress,
            deviceType
        );
        
        LocalDateTime loginTime = LocalDateTime.now();
        
        loginLogService.recordLoginLog(AccountLockService.USER_TYPE_EMPLOYEE, email, employee.getId(), true, null);
        
        LoginLog latestLog = getLatestSuccessLog(employee.getId());
        Long logId = latestLog != null ? latestLog.getId() : null;
        
        AbnormalLoginDetectionService.DetectionResult detectionResult = 
            abnormalLoginDetectionService.detectAbnormalLogin(
                AccountLockService.USER_TYPE_EMPLOYEE,
                employee.getId(),
                ipAddress,
                loginTime,
                logId
            );
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(employee.getEmail());
        response.setRole(JwtUtil.ROLE_EMPLOYEE);
        response.setEmployeeId(employee.getId());
        response.setName(employee.getName());
        response.setAvatar(employee.getAvatar());
        response.setIsActive(isFirstLogin ? 0 : employee.getIsActive());
        
        if (detectionResult.isAbnormal()) {
            String abnormalMsg = String.format("检测到异常登录：%s。如非本人操作请及时修改密码", 
                String.join("、", detectionResult.getAbnormalTypes()));
            return LoginResultDTO.successWithAbnormal(response, abnormalMsg);
        }
        
        return LoginResultDTO.success(response);
    }
    
    private LoginLog getLatestSuccessLog(Long employeeId) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginLog::getUserId, employeeId)
               .eq(LoginLog::getUserType, AccountLockService.USER_TYPE_EMPLOYEE)
               .eq(LoginLog::getLoginResult, 1)
               .orderByDesc(LoginLog::getLoginTime)
               .last("LIMIT 1");
        return loginLogService.getEmployeeLoginLogs(employeeId, 1).stream().findFirst().orElse(null);
    }
    
    public void firstLoginChangePassword(Long employeeId, EmployeeLoginRequest.ChangePasswordRequest request) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        
        if (!PasswordUtil.matches(request.getCurrentPassword(), employee.getPassword())) {
            throw new IllegalArgumentException("当前密码不正确");
        }
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入不一致，请重新输入");
        }
        
        employee.setPassword(PasswordUtil.encode(request.getNewPassword()));
        employee.setIsActive(1);
        employeeMapper.updateById(employee);
    }
    
    public void changePassword(Long employeeId, EmployeeLoginRequest.ChangePasswordRequest request) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        
        if (!PasswordUtil.matches(request.getCurrentPassword(), employee.getPassword())) {
            throw new IllegalArgumentException("当前密码不正确");
        }
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入不一致，请重新输入");
        }
        
        employee.setPassword(PasswordUtil.encode(request.getNewPassword()));
        employeeMapper.updateById(employee);
    }
    
    public String getInitialPassword(String phone) {
        if (phone == null || phone.length() < 6) {
            return "123456";
        }
        return phone.substring(phone.length() - 6);
    }
}
