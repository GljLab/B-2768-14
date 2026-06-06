package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "问卷创建/编辑请求")
public class SurveyRequest {
    
    @Schema(description = "问卷ID（编辑时使用）")
    private Long id;
    
    @NotBlank(message = "问卷标题不能为空")
    @Schema(description = "问卷标题")
    private String title;
    
    @Schema(description = "问卷说明")
    private String description;
    
    @NotNull(message = "截止日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "截止日期")
    private LocalDateTime deadline;
    
    @Schema(description = "是否匿名: 1=匿名, 0=实名")
    private Integer isAnonymous;
    
    @Schema(description = "发放范围")
    private List<TargetRequest> targets;
    
    @Schema(description = "题目列表")
    private List<QuestionRequest> questions;
    
    @Data
    @Schema(description = "发放范围请求")
    public static class TargetRequest {
        @NotNull(message = "目标类型不能为空")
        @Schema(description = "目标类型: 1=全体员工, 2=部门")
        private Integer targetType;
        
        @Schema(description = "目标ID（部门ID，全体员工时为NULL）")
        private Long targetId;
        
        @Schema(description = "目标名称")
        private String targetName;
    }
    
    @Data
    @Schema(description = "题目请求")
    public static class QuestionRequest {
        @Schema(description = "题目ID（编辑时使用）")
        private Long id;
        
        @NotNull(message = "题目类型不能为空")
        @Schema(description = "题目类型: 1=评分题, 2=单选题, 3=开放题")
        private Integer questionType;
        
        @NotBlank(message = "题目标题不能为空")
        @Schema(description = "题目标题")
        private String title;
        
        @Schema(description = "排序")
        private Integer sortOrder;
        
        @Schema(description = "是否必填: 1=是, 0=否")
        private Integer required;
        
        @Schema(description = "选项列表（评分题的评分项或单选题的选项）")
        private List<OptionRequest> options;
    }
    
    @Data
    @Schema(description = "选项请求")
    public static class OptionRequest {
        @Schema(description = "选项ID（编辑时使用）")
        private Long id;
        
        @NotBlank(message = "选项文本不能为空")
        @Schema(description = "选项文本")
        private String optionText;
        
        @Schema(description = "排序")
        private Integer sortOrder;
    }
}
