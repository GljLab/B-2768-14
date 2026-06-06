package com.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {
    
    @Schema(description = "JWT token")
    private String token;
    
    @Schema(description = "用户名/邮箱")
    private String username;
    
    @Schema(description = "角色: admin/employee")
    private String role;
    
    @Schema(description = "员工ID（员工登录时返回）")
    private Long employeeId;
    
    @Schema(description = "员工姓名")
    private String name;
    
    @Schema(description = "员工头像")
    private String avatar;
    
    @Schema(description = "是否已激活")
    private Integer isActive;
}
