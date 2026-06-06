package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "员工请求")
public class EmployeeRequest {
    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名", example = "张三")
    private String name;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@company.com")
    private String email;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
    
    @Schema(description = "部门ID", example = "1")
    private Long departmentId;
    
    @Schema(description = "部门名称", example = "技术部")
    private String department;
    
    @NotBlank(message = "职位不能为空")
    @Schema(description = "职位", example = "软件工程师")
    private String position;
    
    @NotNull(message = "入职日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "入职日期", example = "2024-01-01")
    private LocalDate hireDate;
    
    @Schema(description = "状态: 1=在职, 0=离职", example = "1")
    private Integer status;
}
