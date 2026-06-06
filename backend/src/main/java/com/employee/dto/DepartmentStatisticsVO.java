package com.employee.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepartmentStatisticsVO {
    private Long departmentId;
    private String departmentName;
    private Integer activeCount;
    private Integer inactiveCount;
    private BigDecimal avgTenureMonths;
    private Integer totalCount;
}
