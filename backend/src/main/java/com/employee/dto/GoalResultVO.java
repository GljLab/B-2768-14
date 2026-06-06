package com.employee.dto;

import lombok.Data;

@Data
public class GoalResultVO {
    private Long goalId;
    private String goalName;
    private Integer weight;
    private Integer selfScore;
    private Integer adminScore;
    private Integer achievementDegree;
    private String actualResult;
}
