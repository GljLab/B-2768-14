package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EvaluationProgressVO {
    private Long cycleId;
    private String cycleName;
    private Integer totalEmployees;
    
    private Integer goalCompleted;
    private Integer goalPending;
    
    private Integer selfEvalCompleted;
    private Integer selfEvalPending;
    
    private Integer colleagueEvalCompleted;
    private Integer colleagueEvalPending;
    
    private Integer adminEvalCompleted;
    private Integer adminEvalPending;
    
    private Integer oneOnOneCompleted;
    private Integer oneOnOnePending;
    
    private List<EmployeeProgressVO> employees;
}
