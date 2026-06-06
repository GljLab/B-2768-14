package com.employee.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepartmentAverageVO {
    private Long departmentId;
    private String departmentName;
    private BigDecimal averageScore;
    private Integer employeeCount;
}
