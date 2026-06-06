package com.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "员工登录请求")
public class EmployeeLoginRequest {
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "员工邮箱", example = "zhangwei@company.com")
    private String email;
    
    @NotBlank(message = "密码不能为空")
    @Schema(description = "登录密码", example = "123456")
    private String password;
    
    @Data
    @Schema(description = "修改密码请求")
    public static class ChangePasswordRequest {
        
        @NotBlank(message = "当前密码不能为空")
        @Schema(description = "当前密码")
        private String currentPassword;
        
        @NotBlank(message = "新密码不能为空")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$", message = "密码至少8位，需包含字母和数字")
        @Schema(description = "新密码")
        private String newPassword;
        
        @NotBlank(message = "确认密码不能为空")
        @Schema(description = "确认新密码")
        private String confirmPassword;
    }
}
