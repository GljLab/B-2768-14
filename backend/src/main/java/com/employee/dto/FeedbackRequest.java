package com.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "提交意见请求")
public class FeedbackRequest {
    
    @NotBlank(message = "意见分类不能为空")
    @Schema(description = "意见分类: 制度建议/薪酬福利/工作环境/团队管理/其他")
    private String category;
    
    @NotBlank(message = "意见内容不能为空")
    @Schema(description = "意见内容")
    private String content;
}
