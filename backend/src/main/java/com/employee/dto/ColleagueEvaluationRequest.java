package com.employee.dto;

import lombok.Data;

import java.util.List;

@Data
public class ColleagueEvaluationRequest {
    private Long relationId;
    
    private Long cycleId;
    
    private Long evaluatedId;
    
    private String comment;
    
    private String strengths;
    
    private String improvements;
    
    private List<DimensionScoreRequest> dimensionScores;
}
