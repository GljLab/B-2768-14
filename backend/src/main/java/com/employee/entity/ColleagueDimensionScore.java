package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("colleague_dimension_score")
public class ColleagueDimensionScore {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long evaluationId;
    
    private Long dimensionId;
    
    private String dimensionName;
    
    private Integer score;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
