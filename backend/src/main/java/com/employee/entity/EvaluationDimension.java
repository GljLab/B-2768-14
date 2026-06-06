package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation_dimension")
public class EvaluationDimension {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long cycleId;
    
    private String dimensionName;
    
    private String dimensionDesc;
    
    private Integer maxScore;
    
    private Integer sortOrder;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
