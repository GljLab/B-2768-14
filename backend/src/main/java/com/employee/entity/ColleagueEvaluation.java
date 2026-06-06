package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("colleague_evaluation")
public class ColleagueEvaluation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long relationId;
    
    private Long cycleId;
    
    private Long evaluatorId;
    
    private Long evaluatedId;
    
    private String comment;
    
    private String strengths;
    
    private String improvements;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    
    private Integer isDeleted;
    
    private Long deletedBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
