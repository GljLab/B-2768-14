package com.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "信息查询请求")
public class AnnouncementQueryRequest {
    
    @Schema(description = "页码")
    private Integer pageNum = 1;
    
    @Schema(description = "每页条数")
    private Integer pageSize = 10;
    
    @Schema(description = "类别ID")
    private Long categoryId;
    
    @Schema(description = "重要程度")
    private Integer importance;
    
    @Schema(description = "阅览状态: 0=未阅, 1=已阅")
    private Integer isRead;
    
    @Schema(description = "发布状态: 0=待发布, 1=已发布, 2=已撤回")
    private Integer publishStatus;
    
    @Schema(description = "关键词")
    private String keyword;
    
    @Schema(description = "是否归档（过期）")
    private Integer isArchived;
    
    @Schema(description = "发布人ID（查询我发布的）")
    private Long publisherId;
}
