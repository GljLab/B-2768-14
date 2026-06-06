package com.employee.dto;

import lombok.Data;

import java.util.List;

@Data
public class SelfEvaluationRequest {
    private Long cycleId;
    
    private String summary;
    
    private String growth;
    
    private String challenges;
    
    private String improvements;
    
    private List<GoalEvaluationRequest> goalEvaluations;
}
