package com.employee.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminEvaluationRequest {
    private Long cycleId;
    
    private Long employeeId;
    
    private Integer overallScore;
    
    private String overallComment;
    
    private String developmentSuggestion;
    
    private List<AdminGoalEvalRequest> goalEvaluations;
}
