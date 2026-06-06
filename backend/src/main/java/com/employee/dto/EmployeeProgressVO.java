package com.employee.dto;

import lombok.Data;

@Data
public class EmployeeProgressVO {
    private Long employeeId;
    private String employeeName;
    private String departmentName;
    private Integer goalStatus;
    private Integer selfEvalStatus;
    private Integer colleagueEvalStatus;
    private Integer adminEvalStatus;
    private Integer oneOnOneStatus;
}
