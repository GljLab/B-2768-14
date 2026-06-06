package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("survey_target")
public class SurveyTarget {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long surveyId;
    
    private Integer targetType;
    
    private Long targetId;
    
    private String targetName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
