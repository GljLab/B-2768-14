package com.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "问卷统计结果")
public class SurveyStatisticsVO {
    
    @Schema(description = "问卷ID")
    private Long surveyId;
    
    @Schema(description = "问卷标题")
    private String title;
    
    @Schema(description = "问卷说明")
    private String description;
    
    @Schema(description = "截止日期")
    private LocalDateTime deadline;
    
    @Schema(description = "是否匿名")
    private Integer isAnonymous;
    
    @Schema(description = "发放部门列表")
    private List<String> targetNames;
    
    @Schema(description = "已完成人数")
    private Integer completedCount;
    
    @Schema(description = "总人数")
    private Integer totalCount;
    
    @Schema(description = "完成率")
    private Double completionRate;
    
    @Schema(description = "题目统计列表")
    private List<QuestionStatistics> questionStatistics;
    
    @Data
    @Schema(description = "题目统计")
    public static class QuestionStatistics {
        @Schema(description = "题目ID")
        private Long questionId;
        
        @Schema(description = "题目类型")
        private Integer questionType;
        
        @Schema(description = "题目标题")
        private String title;
        
        @Schema(description = "评分题统计（评分题时使用）")
        private List<RatingStatistics> ratingStatistics;
        
        @Schema(description = "单选题统计（单选题时使用）")
        private List<OptionStatistics> optionStatistics;
        
        @Schema(description = "开放题答案列表（开放题时使用）")
        private List<TextAnswer> textAnswers;
    }
    
    @Data
    @Schema(description = "评分统计")
    public static class RatingStatistics {
        @Schema(description = "评分项名称")
        private String ratingItem;
        
        @Schema(description = "平均分")
        private Double averageScore;
    }
    
    @Data
    @Schema(description = "选项统计")
    public static class OptionStatistics {
        @Schema(description = "选项ID")
        private Long optionId;
        
        @Schema(description = "选项文本")
        private String optionText;
        
        @Schema(description = "选择人数")
        private Integer selectCount;
        
        @Schema(description = "占比")
        private Double percentage;
    }
    
    @Data
    @Schema(description = "文本答案")
    public static class TextAnswer {
        @Schema(description = "答案内容")
        private String content;
        
        @Schema(description = "提交人姓名（非匿名时显示）")
        private String employeeName;
        
        @Schema(description = "提交人部门（非匿名时显示）")
        private String departmentName;
    }
}
