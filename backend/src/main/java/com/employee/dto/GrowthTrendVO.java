package com.employee.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GrowthTrendVO {
    private String cycleName;
    private BigDecimal totalScore;
    private String grade;
}
