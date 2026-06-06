package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("survey_question_option")
public class SurveyQuestionOption {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long questionId;
    
    private String optionText;
    
    private Integer sortOrder;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @TableField(exist = false)
    private Integer selectCount;
    
    @TableField(exist = false)
    private Double percentage;
}
