package com.employee.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.employee.entity.UserSession;
import com.employee.mapper.UserSessionMapper;
import com.employee.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class SessionService {
    
    private static final int MAX_SESSIONS_PER_USER = 5;
    
    private final UserSessionMapper userSessionMapper;
    private final JwtUtil jwtUtil;
    
    @Value("${jwt.expiration}")
    private Long tokenExpiration;
    
    public UserSession createSession(int userType, Long userId, String token, String ipAddress, String deviceType) {
        cleanupExpiredSessions(userType, userId);
        
        UserSession session = new UserSession();
        session.setUserType(userType);
        session.setUserId(userId);
        session.setToken(token);
        session.setIpAddress(ipAddress);
        session.setDeviceType(deviceType);
        session.setLoginTime(LocalDateTime.now());
        session.setExpireTime(LocalDateTime.now().plusDays(7));
        session.setIsActive(1);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        
        userSessionMapper.insert(session);
        
        enforceMaxSessions(userType, userId);
        
        log.debug("会话已创建: userId={}, userType={}", userId, userType);
        return session;
    }
    
    public List<UserSession> getActiveSessions(Long userId, int userType) {
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getUserId, userId)
               .eq(UserSession::getUserType, userType)
               .eq(UserSession::getIsActive, 1)
               .gt(UserSession::getExpireTime, LocalDateTime.now())
               .orderByDesc(UserSession::getLoginTime);
        
        return userSessionMapper.selectList(wrapper);
    }
    
    public void invalidateSession(Long sessionId) {
        userSessionMapper.update(null,
            new LambdaUpdateWrapper<UserSession>()
                .eq(UserSession::getId, sessionId)
                .set(UserSession::getIsActive, 0)
                .set(UserSession::getUpdatedAt, LocalDateTime.now())
        );
        log.info("会话已失效: sessionId={}", sessionId);
    }
    
    public void invalidateSessionByToken(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        userSessionMapper.update(null,
            new LambdaUpdateWrapper<UserSession>()
                .eq(UserSession::getToken, token)
                .set(UserSession::getIsActive, 0)
                .set(UserSession::getUpdatedAt, LocalDateTime.now())
        );
    }
    
    public void invalidateAllSessions(Long userId, int userType, Long excludeSessionId) {
        LambdaUpdateWrapper<UserSession> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserSession::getUserId, userId)
               .eq(UserSession::getUserType, userType)
               .eq(UserSession::getIsActive, 1);
        
        if (excludeSessionId != null) {
            wrapper.ne(UserSession::getId, excludeSessionId);
        }
        
        wrapper.set(UserSession::getIsActive, 0)
               .set(UserSession::getUpdatedAt, LocalDateTime.now());
        
        userSessionMapper.update(null, wrapper);
    }
    
    public boolean isTokenValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getToken, token)
               .eq(UserSession::getIsActive, 1)
               .gt(UserSession::getExpireTime, LocalDateTime.now());
        
        return userSessionMapper.selectCount(wrapper) > 0;
    }
    
    public UserSession getSessionByToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getToken, token);
        
        return userSessionMapper.selectOne(wrapper);
    }
    
    private void cleanupExpiredSessions(int userType, Long userId) {
        userSessionMapper.delete(
            new LambdaQueryWrapper<UserSession>()
                .eq(UserSession::getUserId, userId)
                .eq(UserSession::getUserType, userType)
                .lt(UserSession::getExpireTime, LocalDateTime.now())
        );
    }
    
    private void enforceMaxSessions(int userType, Long userId) {
        LambdaQueryWrapper<UserSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSession::getUserId, userId)
               .eq(UserSession::getUserType, userType)
               .eq(UserSession::getIsActive, 1)
               .orderByDesc(UserSession::getLoginTime);
        
        List<UserSession> sessions = userSessionMapper.selectList(wrapper);
        
        if (sessions.size() > MAX_SESSIONS_PER_USER) {
            for (int i = MAX_SESSIONS_PER_USER; i < sessions.size(); i++) {
                invalidateSession(sessions.get(i).getId());
            }
        }
    }
    
    public void cleanupAllExpiredSessions() {
        userSessionMapper.delete(
            new LambdaQueryWrapper<UserSession>()
                .lt(UserSession::getExpireTime, LocalDateTime.now())
        );
    }
    
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                   .atZone(ZoneId.systemDefault())
                   .toLocalDateTime();
    }
}
