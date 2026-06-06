package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "信息发布请求")
public class AnnouncementRequest {
    
    @Schema(description = "信息ID（编辑时使用）")
    private Long id;
    
    @NotBlank(message = "标题不能为空")
    @Schema(description = "标题")
    private String title;
    
    @NotNull(message = "类别不能为空")
    @Schema(description = "类别ID")
    private Long categoryId;
    
    @Schema(description = "详细内容（富文本）")
    private String content;
    
    @NotNull(message = "重要程度不能为空")
    @Schema(description = "重要程度: 1=常规, 2=关注, 3=紧急")
    private Integer importance;
    
    @Schema(description = "是否置顶")
    private Integer isTop;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "起始展示日期")
    private LocalDate publishDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "截止展示日期")
    private LocalDate expiryDate;
    
    @Schema(description = "是否强制确认")
    private Integer isForceConfirm;
    
    @Schema(description = "附件信息JSON")
    private String attachments;
    
    @Schema(description = "发布状态: 0=待发布, 1=已发布")
    private Integer publishStatus;
    
    @Schema(description = "接收范围")
    private List<TargetRequest> targets;
    
    @Data
    @Schema(description = "接收范围请求")
    public static class TargetRequest {
        @NotNull(message = "目标类型不能为空")
        @Schema(description = "目标类型: 1=全公司, 2=部门, 3=个人")
        private Integer targetType;
        
        @Schema(description = "目标ID（部门ID或员工ID）")
        private Long targetId;
        
        @Schema(description = "目标名称")
        private String targetName;
    }
}
