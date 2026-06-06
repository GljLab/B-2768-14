package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("growth_report")
public class GrowthReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long cycleId;
    
    private Long employeeId;
    
    private String employeeName;
    
    private Long departmentId;
    
    private String departmentName;
    
    private BigDecimal selfScore;
    
    private BigDecimal colleagueScore;
    
    private BigDecimal adminScore;
    
    private BigDecimal totalScore;
    
    private String grade;
    
    private Integer departmentRank;
    
    private Integer companyRank;
    
    private String dimensionScores;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    private String cycleName;
}
