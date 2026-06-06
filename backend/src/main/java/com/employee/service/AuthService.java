package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.employee.dto.LoginRequest;
import com.employee.dto.LoginResponse;
import com.employee.dto.LoginResultDTO;
import com.employee.entity.AdminUser;
import com.employee.entity.LoginLog;
import com.employee.mapper.AdminUserMapper;
import com.employee.utils.JwtUtil;
import com.employee.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class AuthService {
    private final AdminUserMapper adminUserMapper;
    private final AccountLockService accountLockService;
    private final LoginLogService loginLogService;
    private final SessionService sessionService;
    private final AbnormalLoginDetectionService abnormalLoginDetectionService;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public LoginResultDTO login(LoginRequest request) {
        String username = request.getUsername();
        log.info("管理员尝试登录: {}", username);
        
        if (accountLockService.isAccountLocked(username, AccountLockService.USER_TYPE_ADMIN)) {
            LocalDateTime unlockTime = accountLockService.getUnlockTime(username, AccountLockService.USER_TYPE_ADMIN);
            loginLogService.recordLoginLog(AccountLockService.USER_TYPE_ADMIN, username, null, false, "账号已锁定");
            return LoginResultDTO.locked("账号已被锁定，请30分钟后重试", unlockTime);
        }
        
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getUsername, username);
        
        AdminUser user = adminUserMapper.selectOne(wrapper);
        
        if (user == null) {
            log.warn("管理员不存在: {}", username);
            loginLogService.recordLoginLog(AccountLockService.USER_TYPE_ADMIN, username, null, false, "账号不存在");
            return LoginResultDTO.failure("用户名或密码错误", 0);
        }
        
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            log.warn("管理员密码错误: {}", username);
            accountLockService.recordFailedAttempt(username, AccountLockService.USER_TYPE_ADMIN);
            int remainingAttempts = accountLockService.getRemainingAttempts(username, AccountLockService.USER_TYPE_ADMIN);
            
            String failureReason = "密码错误";
            if (remainingAttempts <= 0) {
                failureReason = "密码错误次数过多，账号已锁定";
            }
            loginLogService.recordLoginLog(AccountLockService.USER_TYPE_ADMIN, username, user.getId(), false, failureReason);
            
            if (remainingAttempts <= 0) {
                LocalDateTime unlockTime = accountLockService.getUnlockTime(username, AccountLockService.USER_TYPE_ADMIN);
                return LoginResultDTO.locked("账号已被锁定，请30分钟后重试", unlockTime);
            }
            
            return LoginResultDTO.failure("用户名或密码错误，还可尝试" + remainingAttempts + "次", remainingAttempts);
        }
        
        accountLockService.resetFailedAttempts(username, AccountLockService.USER_TYPE_ADMIN);
        
        String token = jwtUtil.generateAdminToken(user.getId(), user.getUsername());
        
        String ipAddress = loginLogService.getCurrentIp();
        String deviceType = loginLogService.getCurrentDeviceType();
        
        sessionService.createSession(
            AccountLockService.USER_TYPE_ADMIN,
            user.getId(),
            token,
            ipAddress,
            deviceType
        );
        
        LoginLog latestLog = getLatestLoginLog(user.getId());
        Long logId = latestLog != null ? latestLog.getId() : null;
        
        LocalDateTime loginTime = LocalDateTime.now();
        AbnormalLoginDetectionService.DetectionResult detectionResult = 
            abnormalLoginDetectionService.detectAbnormalLogin(
                AccountLockService.USER_TYPE_ADMIN,
                user.getId(),
                ipAddress,
                loginTime,
                logId
            );
        
        loginLogService.recordLoginLog(AccountLockService.USER_TYPE_ADMIN, username, user.getId(), true, null);
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(JwtUtil.ROLE_ADMIN);
        
        if (detectionResult.isAbnormal()) {
            String abnormalMsg = String.format("检测到异常登录：%s。如非本人操作请及时修改密码", 
                String.join("、", detectionResult.getAbnormalTypes()));
            return LoginResultDTO.successWithAbnormal(response, abnormalMsg);
        }
        
        return LoginResultDTO.success(response);
    }
    
    private LoginLog getLatestLoginLog(Long userId) {
        return loginLogService.getLastSuccessfulLogin(userId, AccountLockService.USER_TYPE_ADMIN);
    }
}
