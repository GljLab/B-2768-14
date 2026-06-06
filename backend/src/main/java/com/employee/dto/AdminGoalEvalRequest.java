package com.employee.dto;

import lombok.Data;

@Data
public class AdminGoalEvalRequest {
    private Long goalId;
    private Integer score;
    private String comment;
}
