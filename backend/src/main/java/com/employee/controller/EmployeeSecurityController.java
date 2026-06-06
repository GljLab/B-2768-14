package com.employee.controller;

import com.employee.common.Result;
import com.employee.entity.LoginLog;
import com.employee.entity.UserSession;
import com.employee.service.AbnormalLoginDetectionService;
import com.employee.service.AccountLockService;
import com.employee.service.LoginLogService;
import com.employee.service.SessionService;
import com.employee.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/security")
@RequiredArgsConstructor
@Tag(name = "员工安全管理", description = "员工端安全相关接口")
public class EmployeeSecurityController {
    
    private final LoginLogService loginLogService;
    private final SessionService sessionService;
    private final AbnormalLoginDetectionService abnormalLoginDetectionService;
    private final JwtUtil jwtUtil;
    
    @GetMapping("/login-logs")
    @Operation(summary = "获取员工登录记录")
    public Result<List<LoginLog>> getLoginLogs(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        List<LoginLog> logs = loginLogService.getEmployeeLoginLogs(employeeId, 20);
        return Result.success(logs);
    }
    
    @GetMapping("/sessions")
    @Operation(summary = "获取当前在线设备")
    public Result<Map<String, Object>> getSessions(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        
        List<UserSession> sessions = sessionService.getActiveSessions(employeeId, AccountLockService.USER_TYPE_EMPLOYEE);
        
        UserSession currentSession = sessionService.getSessionByToken(token);
        Long currentSessionId = currentSession != null ? currentSession.getId() : null;
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessions", sessions);
        result.put("currentSessionId", currentSessionId);
        
        return Result.success(result);
    }
    
    @PostMapping("/sessions/{sessionId}/revoke")
    @Operation(summary = "下线指定设备")
    public Result<String> revokeSession(@RequestHeader("Authorization") String authHeader,
                                         @PathVariable Long sessionId) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        
        UserSession currentSession = sessionService.getSessionByToken(token);
        if (currentSession != null && currentSession.getId().equals(sessionId)) {
            return Result.error("不能下线当前登录设备");
        }
        
        UserSession targetSession = null;
        List<UserSession> sessions = sessionService.getActiveSessions(employeeId, AccountLockService.USER_TYPE_EMPLOYEE);
        for (UserSession s : sessions) {
            if (s.getId().equals(sessionId)) {
                targetSession = s;
                break;
            }
        }
        
        if (targetSession == null || !targetSession.getUserId().equals(employeeId)) {
            return Result.error("无权限操作");
        }
        
        sessionService.invalidateSession(sessionId);
        return Result.success("设备已下线");
    }
    
    @GetMapping("/latest-abnormal")
    @Operation(summary = "获取最新异常登录提醒")
    public Result<LoginLog> getLatestAbnormalLogin(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        
        LoginLog abnormalLog = abnormalLoginDetectionService.getLatestAbnormalLogin(
            AccountLockService.USER_TYPE_EMPLOYEE, employeeId);
        
        return Result.success(abnormalLog);
    }
}
