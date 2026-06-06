package com.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkGoalRequest {
    private Long id;
    
    private Long cycleId;
    
    @NotBlank(message = "目标名称不能为空")
    private String goalName;
    
    private String planContent;
    
    @NotNull(message = "重要度权重不能为空")
    private Integer weight;
    
    private Integer status;
    
    private String adminAdvice;
}
