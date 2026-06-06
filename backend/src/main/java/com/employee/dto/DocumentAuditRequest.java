package com.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "证件审核请求")
public class DocumentAuditRequest {
    
    @NotNull(message = "证件ID不能为空")
    @Schema(description = "证件ID")
    private Long documentId;
    
    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果: 1=通过, 2=拒绝")
    private Integer auditStatus;
    
    @Schema(description = "拒绝理由（拒绝时必填）")
    private String auditReason;
}
