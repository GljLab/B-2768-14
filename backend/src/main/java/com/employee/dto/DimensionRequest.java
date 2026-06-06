package com.employee.dto;

import lombok.Data;

@Data
public class DimensionRequest {
    private String dimensionName;
    private String dimensionDesc;
    private Integer maxScore;
    private Integer sortOrder;
}
