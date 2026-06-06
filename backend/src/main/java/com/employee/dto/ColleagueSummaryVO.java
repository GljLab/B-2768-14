package com.employee.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ColleagueSummaryVO {
    private BigDecimal averageScore;
    private Integer evaluatorCount;
    private List<String> strengths;
    private List<String> improvements;
    private Map<String, BigDecimal> dimensionAverages;
}
