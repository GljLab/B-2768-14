package com.employee.dto;

import lombok.Data;

@Data
public class DimensionScoreRequest {
    private Long dimensionId;
    private String dimensionName;
    private Integer score;
}
