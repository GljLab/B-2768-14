package com.employee.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CycleTrendVO {
    private String cycleName;
    private BigDecimal averageScore;
    private Integer cycleCount;
}
