package com.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "证件统计VO")
public class DocumentStatisticsVO {
    
    @Schema(description = "员工总数")
    private Integer totalEmployees;
    
    @Schema(description = "已上传身份证人数")
    private Integer idCardCount;
    
    @Schema(description = "身份证上传率")
    private Double idCardRate;
    
    @Schema(description = "已上传学历证书人数")
    private Integer educationCount;
    
    @Schema(description = "学历证书上传率")
    private Double educationRate;
    
    @Schema(description = "本月到期证件数量")
    private Integer expiringThisMonth;
    
    @Schema(description = "已过期证件数量")
    private Integer expiredCount;
    
    @Schema(description = "即将到期证件数量")
    private Integer expiringCount;
    
    @Schema(description = "待审核证件数量")
    private Integer pendingAuditCount;
}
