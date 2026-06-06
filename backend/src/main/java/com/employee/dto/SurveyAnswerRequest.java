package com.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "提交问卷答案请求")
public class SurveyAnswerRequest {
    
    @NotNull(message = "问卷ID不能为空")
    @Schema(description = "问卷ID")
    private Long surveyId;
    
    @Schema(description = "答案列表")
    private List<AnswerItemRequest> answers;
    
    @Data
    @Schema(description = "答案项请求")
    public static class AnswerItemRequest {
        @NotNull(message = "题目ID不能为空")
        @Schema(description = "题目ID")
        private Long questionId;
        
        @NotNull(message = "题目类型不能为空")
        @Schema(description = "题目类型: 1=评分题, 2=单选题, 3=开放题")
        private Integer questionType;
        
        @Schema(description = "选项ID（单选题时使用）")
        private Long optionId;
        
        @Schema(description = "评分分数（评分题时使用）")
        private Integer ratingScore;
        
        @Schema(description = "评分项名称（评分题时使用）")
        private String ratingItem;
        
        @Schema(description = "文本内容（开放题时使用）")
        private String textContent;
    }
}
