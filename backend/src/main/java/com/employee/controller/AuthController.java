package com.employee.controller;

import com.employee.common.Result;
import com.employee.dto.EmployeeLoginRequest;
import com.employee.dto.LoginRequest;
import com.employee.dto.LoginResponse;
import com.employee.dto.LoginResultDTO;
import com.employee.service.AuthService;
import com.employee.service.EmployeeAuthService;
import com.employee.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录相关接口")
public class AuthController {
    private final AuthService authService;
    private final EmployeeAuthService employeeAuthService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员用户名密码登录")
    public Result<LoginResultDTO> login(@Valid @RequestBody LoginRequest request) {
        LoginResultDTO result = authService.login(request);
        if (result.isSuccess()) {
            return Result.success(result);
        } else {
            return Result.error(result.getMessage());
        }
    }
    
    @PostMapping("/employee/login")
    @Operation(summary = "员工登录", description = "员工邮箱密码登录")
    public Result<LoginResultDTO> employeeLogin(@Valid @RequestBody EmployeeLoginRequest request) {
        LoginResultDTO result = employeeAuthService.login(request);
        if (result.isSuccess()) {
            return Result.success(result);
        } else {
            return Result.error(result.getMessage());
        }
    }
    
    @PostMapping("/employee/first-login/change-password")
    @Operation(summary = "首次登录修改密码", description = "员工首次登录时修改初始密码")
    public Result<String> firstLoginChangePassword(@RequestHeader("Authorization") String authHeader, 
                                                   @Valid @RequestBody EmployeeLoginRequest.ChangePasswordRequest request) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.getEmployeeIdFromToken(token);
        employeeAuthService.firstLoginChangePassword(employeeId, request);
        return Result.success("密码修改成功，请重新登录");
    }
}
