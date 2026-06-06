package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("work_goal")
public class WorkGoal {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long cycleId;
    
    private Long employeeId;
    
    private String goalName;
    
    private String planContent;
    
    private Integer weight;
    
    private Integer status;
    
    private String adminAdvice;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
