package com.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequest {
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称长度不能超过50个字符")
    private String name;
    
    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    private String code;
    
    private Long parentId;
    
    private Long managerId;
    
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
    
    private Integer sortOrder;
}
