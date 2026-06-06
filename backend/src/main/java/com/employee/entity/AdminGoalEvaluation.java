package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_goal_evaluation")
public class AdminGoalEvaluation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long adminEvalId;
    
    private Long goalId;
    
    private Integer score;
    
    private String comment;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
