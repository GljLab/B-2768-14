package com.employee.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkHighlightRequest {
    private Long id;
    
    private Long cycleId;
    
    @NotBlank(message = "亮点标题不能为空")
    private String title;
    
    private String content;
    
    private Integer highlightType;
    
    private String attachments;
}
