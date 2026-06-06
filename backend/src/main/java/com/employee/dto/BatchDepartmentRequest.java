package com.employee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchDepartmentRequest {
    @NotNull(message = "员工ID列表不能为空")
    private List<Long> employeeIds;
    
    @NotNull(message = "目标部门ID不能为空")
    private Long departmentId;
}
