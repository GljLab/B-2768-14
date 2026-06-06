package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation_cycle_employee")
public class EvaluationCycleEmployee {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long cycleId;
    
    private Long employeeId;
    
    private String employeeName;
    
    private Long departmentId;
    
    private String departmentName;
    
    private Integer goalStatus;
    
    private Integer selfEvalStatus;
    
    private Integer colleagueEvalStatus;
    
    private Integer adminEvalStatus;
    
    private Integer oneOnOneStatus;
    
    private Integer feedbackStatus;
    
    private Integer reportStatus;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @TableField(exist = false)
    private String cycleName;
    
    @TableField(exist = false)
    private String startDate;
    
    @TableField(exist = false)
    private String endDate;
}
