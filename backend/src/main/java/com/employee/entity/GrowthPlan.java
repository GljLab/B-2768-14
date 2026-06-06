package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("growth_plan")
public class GrowthPlan {
    @TableId(type = IdType.AUTO)
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
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
