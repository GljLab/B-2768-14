package com.employee.dto;

import lombok.Data;

@Data
public class AdminEvaluationVO {
    private Integer overallScore;
    private String overallComment;
    private String developmentSuggestion;
    private String adminName;
}
