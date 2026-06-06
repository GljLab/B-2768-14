package com.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "证件查询请求")
public class DocumentQueryRequest {
    
    @Schema(description = "员工姓名")
    private String employeeName;
    
    @Schema(description = "证件类型ID")
    private Long documentTypeId;
    
    @Schema(description = "审核状态: 0=待审核, 1=已通过, 2=已拒绝")
    private Integer auditStatus;
    
    @Schema(description = "有效期状态: expiring=即将到期, expired=已过期, valid=有效, permanent=长期有效")
    private String expiryStatus;
    
    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页条数", defaultValue = "10")
    private Integer pageSize = 10;
}
