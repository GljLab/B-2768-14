package com.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.employee.common.Result;
import com.employee.entity.Employee;
import com.employee.entity.LoginLog;
import com.employee.mapper.EmployeeMapper;
import com.employee.service.AbnormalLoginDetectionService;
import com.employee.service.AccountLockService;
import com.employee.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/security")
@RequiredArgsConstructor
@Tag(name = "管理员安全管理", description = "管理员端安全相关接口")
public class AdminSecurityController {
    
    private final LoginLogService loginLogService;
    private final AccountLockService accountLockService;
    private final EmployeeMapper employeeMapper;
    private final AbnormalLoginDetectionService abnormalLoginDetectionService;
    
    @GetMapping("/login-logs")
    @Operation(summary = "获取所有登录日志")
    public Result<IPage<LoginLog>> getLoginLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer loginResult,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        IPage<LoginLog> logs = loginLogService.getAllLoginLogs(
            page, size, username, userType, loginResult, startTime, endTime);
        return Result.success(logs);
    }
    
    @GetMapping("/abnormal-logs")
    @Operation(summary = "获取所有异常登录记录")
    public Result<List<LoginLog>> getAbnormalLoginLogs() {
        List<LoginLog> logs = loginLogService.getAbnormalLoginLogs();
        return Result.success(logs);
    }
    
    @PostMapping("/employees/{employeeId}/unlock")
    @Operation(summary = "解锁员工账号")
    public Result<String> unlockEmployee(@PathVariable Long employeeId) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            return Result.error("员工不存在");
        }
        accountLockService.unlockEmployeeAccount(employeeId);
        return Result.success("账号已解锁");
    }
    
    @PostMapping("/admin/{adminId}/unlock")
    @Operation(summary = "解锁管理员账号")
    public Result<String> unlockAdmin(@PathVariable Long adminId) {
        accountLockService.unlockAdminAccount(adminId);
        return Result.success("账号已解锁");
    }
}
