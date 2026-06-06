package com.employee.controller;

import com.employee.common.Result;
import com.employee.dto.EmployeeLoginRequest;
import com.employee.entity.Employee;
import com.employee.service.EmployeeAuthService;
import com.employee.service.EmployeeProfileService;
import com.employee.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employee/profile")
@RequiredArgsConstructor
@Tag(name = "员工个人资料", description = "员工个人资料相关接口")
public class EmployeeProfileController {
    private final EmployeeProfileService employeeProfileService;
    private final EmployeeAuthService employeeAuthService;
    private final JwtUtil jwtUtil;
    
    @GetMapping
    @Operation(summary = "获取个人资料", description = "员工获取自己的个人资料")
    public Result<Employee> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        Employee employee = employeeProfileService.getProfile(employeeId);
        return Result.success(employee);
    }
    
    @PutMapping
    @Operation(summary = "更新个人资料", description = "员工更新自己的个人资料")
    public Result<String> updateProfile(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody Employee employee) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        String message = employeeProfileService.updateProfile(employeeId, employee);
        return Result.success(message);
    }
    
    @PutMapping("/avatar")
    @Operation(summary = "更新头像", description = "员工更新自己的头像")
    public Result<String> updateAvatar(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody Map<String, String> request) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        String avatarUrl = request.get("avatarUrl");
        employeeProfileService.updateAvatar(employeeId, avatarUrl);
        return Result.success("头像更新成功");
    }
    
    @PutMapping("/change-password")
    @Operation(summary = "修改密码", description = "员工修改登录密码")
    public Result<String> changePassword(@RequestHeader("Authorization") String authHeader,
                                         @Valid @RequestBody EmployeeLoginRequest.ChangePasswordRequest request) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        employeeAuthService.changePassword(employeeId, request);
        return Result.success("密码修改成功，请重新登录");
    }
}
