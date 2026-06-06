package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.employee.entity.LoginLog;
import com.employee.mapper.LoginLogMapper;
import com.employee.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class AbnormalLoginDetectionService {
    
    private static final int ABNORMAL_HOUR_START = 0;
    private static final int ABNORMAL_HOUR_END = 6;
    private static final int IP_CHANGE_THRESHOLD_MINUTES = 60;
    
    @Value("${app.timezone:Asia/Shanghai}")
    private String timezone;
    
    private final LoginLogMapper loginLogMapper;
    private final TimeUtil timeUtil;
    
    public DetectionResult detectAbnormalLogin(int userType, Long userId, String currentIp, LocalDateTime loginTime, Long currentLogId) {
        List<String> abnormalTypes = new ArrayList<>();
        
        if (isMidnightLogin(loginTime)) {
            abnormalTypes.add("凌晨登录");
        }
        
        if (isQuickIpChange(userType, userId, currentIp, loginTime)) {
            abnormalTypes.add("IP异常变化");
        }
        
        boolean isAbnormal = !abnormalTypes.isEmpty();
        
        if (isAbnormal && currentLogId != null) {
            LoginLog log = new LoginLog();
            log.setId(currentLogId);
            log.setIsAbnormal(1);
            log.setAbnormalType(String.join(",", abnormalTypes));
            loginLogMapper.updateById(log);
        }
        
        return new DetectionResult(isAbnormal, abnormalTypes);
    }
    
    private boolean isMidnightLogin(LocalDateTime loginTime) {
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime zonedTime = loginTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId);
        int hour = zonedTime.getHour();
        log.debug("异常检测 - 原始时间: {}, 时区: {}, 转换后小时: {}", loginTime, timezone, hour);
        return hour >= ABNORMAL_HOUR_START && hour < ABNORMAL_HOUR_END;
    }
    
    private boolean isQuickIpChange(int userType, Long userId, String currentIp, LocalDateTime currentLoginTime) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginLog::getUserId, userId)
               .eq(LoginLog::getUserType, userType)
               .eq(LoginLog::getLoginResult, 1)
               .orderByDesc(LoginLog::getLoginTime)
               .last("LIMIT 2");
        
        List<LoginLog> recentLogs = loginLogMapper.selectList(wrapper);
        
        if (recentLogs.isEmpty()) {
            return false;
        }
        
        LoginLog lastLog = null;
        for (LoginLog log : recentLogs) {
            if (log.getLoginTime().isBefore(currentLoginTime)) {
                lastLog = log;
                break;
            }
        }
        
        if (lastLog == null) {
            return false;
        }
        
        long minutesDiff = ChronoUnit.MINUTES.between(lastLog.getLoginTime(), currentLoginTime);
        
        String lastIp = lastLog.getIpAddress();
        
        return minutesDiff < IP_CHANGE_THRESHOLD_MINUTES && 
               lastIp != null && 
               !lastIp.equals(currentIp) &&
               !lastIp.equals("127.0.0.1");
    }
    
    public LoginLog getLatestAbnormalLogin(int userType, Long userId) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginLog::getUserId, userId)
               .eq(LoginLog::getUserType, userType)
               .eq(LoginLog::getIsAbnormal, 1)
               .eq(LoginLog::getLoginResult, 1)
               .orderByDesc(LoginLog::getLoginTime)
               .last("LIMIT 1");
        
        return loginLogMapper.selectOne(wrapper);
    }
    
    public static class DetectionResult {
        private final boolean abnormal;
        private final List<String> abnormalTypes;
        
        public DetectionResult(boolean abnormal, List<String> abnormalTypes) {
            this.abnormal = abnormal;
            this.abnormalTypes = abnormalTypes;
        }
        
        public boolean isAbnormal() {
            return abnormal;
        }
        
        public List<String> getAbnormalTypes() {
            return abnormalTypes;
        }
    }
}
