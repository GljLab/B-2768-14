package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.employee.entity.LoginLog;
import com.employee.mapper.LoginLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class LoginLogService {
    
    private final LoginLogMapper loginLogMapper;
    private final HttpServletRequest request;
    
    public void recordLoginLog(int userType, String username, Long userId, boolean success, String failureReason) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserType(userType);
        loginLog.setUsername(username);
        loginLog.setUserId(userId);
        loginLog.setIpAddress(getClientIp());
        loginLog.setDeviceType(getDeviceType());
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setLoginResult(success ? 1 : 0);
        loginLog.setFailureReason(failureReason);
        loginLog.setIsAbnormal(0);
        
        loginLogMapper.insert(loginLog);
        log.debug("登录日志已记录: username={}, success={}", username, success);
    }
    
    public List<LoginLog> getEmployeeLoginLogs(Long employeeId, int limit) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginLog::getUserType, AccountLockService.USER_TYPE_EMPLOYEE)
               .eq(LoginLog::getUserId, employeeId)
               .orderByDesc(LoginLog::getLoginTime)
               .last("LIMIT " + limit);
        
        return loginLogMapper.selectList(wrapper);
    }
    
    public IPage<LoginLog> getAllLoginLogs(int page, int size, String username, Integer userType, 
                                           Integer loginResult, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        
        if (username != null && !username.isEmpty()) {
            wrapper.like(LoginLog::getUsername, username);
        }
        if (userType != null) {
            wrapper.eq(LoginLog::getUserType, userType);
        }
        if (loginResult != null) {
            wrapper.eq(LoginLog::getLoginResult, loginResult);
        }
        if (startTime != null) {
            wrapper.ge(LoginLog::getLoginTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(LoginLog::getLoginTime, endTime);
        }
        
        wrapper.orderByDesc(LoginLog::getLoginTime);
        
        return loginLogMapper.selectPage(new Page<>(page, size), wrapper);
    }
    
    public List<LoginLog> getAbnormalLoginLogs() {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginLog::getIsAbnormal, 1)
               .orderByDesc(LoginLog::getLoginTime);
        
        return loginLogMapper.selectList(wrapper);
    }
    
    public LoginLog getLastSuccessfulLogin(Long userId, int userType) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginLog::getUserId, userId)
               .eq(LoginLog::getUserType, userType)
               .eq(LoginLog::getLoginResult, 1)
               .orderByDesc(LoginLog::getLoginTime)
               .last("LIMIT 1, 1");
        
        return loginLogMapper.selectOne(wrapper);
    }
    
    public void markAsAbnormal(Long logId, String abnormalType) {
        LoginLog loginLog = new LoginLog();
        loginLog.setId(logId);
        loginLog.setIsAbnormal(1);
        loginLog.setAbnormalType(abnormalType);
        loginLogMapper.updateById(loginLog);
    }
    
    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
    
    private String getDeviceType() {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown";
        }
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android") || 
            userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "Mobile";
        }
        return "PC";
    }
    
    public String getCurrentIp() {
        return getClientIp();
    }
    
    public String getCurrentDeviceType() {
        return getDeviceType();
    }
}
