package com.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "信息类别请求")
public class AnnouncementCategoryRequest {
    
    @Schema(description = "类别ID（编辑时使用）")
    private Long id;
    
    @NotBlank(message = "类别名称不能为空")
    @Schema(description = "类别名称")
    private String categoryName;
    
    @NotBlank(message = "类别编码不能为空")
    @Schema(description = "类别编码")
    private String categoryCode;
    
    @Schema(description = "图标名称")
    private String icon;
    
    @Schema(description = "排序")
    private Integer sortOrder;
    
    @Schema(description = "状态: 1=启用, 0=禁用")
    private Integer status;
}
