package com.employee.dto;

import lombok.Data;

@Data
public class GoalEvaluationRequest {
    private Long goalId;
    private Integer selfScore;
    private Integer achievementDegree;
    private String actualResult;
}
