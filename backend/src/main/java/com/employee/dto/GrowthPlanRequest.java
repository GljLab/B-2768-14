package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GrowthPlanRequest {
    private Long id;
    private Long employeeId;
    private Long cycleId;
    private Long nextCycleId;
    private String abilityToImprove;
    private String specificActions;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedCompletion;
    
    private String verificationMethod;
    private Integer status;
    private String adminAdvice;
}
