package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("survey_answer_item")
public class SurveyAnswerItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long answerId;
    
    private Long questionId;
    
    private Integer questionType;
    
    private Long optionId;
    
    private Integer ratingScore;
    
    private String ratingItem;
    
    private String textContent;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
