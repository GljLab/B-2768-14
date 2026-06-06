package com.employee.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopEmployeeVO {
    private Long employeeId;
    private String employeeName;
    private String departmentName;
    private BigDecimal totalScore;
    private String grade;
    private Integer consecutiveExcellent;
}
