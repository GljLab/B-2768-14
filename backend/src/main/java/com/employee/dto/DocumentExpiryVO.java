package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "证件到期预警VO")
public class DocumentExpiryVO {
    
    @Schema(description = "证件ID")
    private Long id;
    
    @Schema(description = "员工ID")
    private Long employeeId;
    
    @Schema(description = "员工姓名")
    private String employeeName;
    
    @Schema(description = "证件类型名称")
    private String documentTypeName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "有效期至")
    private LocalDate expiryDate;
    
    @Schema(description = "到期状态: expired=已过期, expiring=即将到期")
    private String expiryStatus;
    
    @Schema(description = "剩余天数")
    private Integer daysRemaining;
}
