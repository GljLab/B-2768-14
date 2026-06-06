package com.employee.dto;

import lombok.Data;

@Data
public class GradeStandardRequest {
    private String gradeName;
    private Integer minScore;
    private Integer maxScore;
    private Integer sortOrder;
}
