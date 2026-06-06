package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "证件上传请求")
public class DocumentUploadRequest {
    
    @NotNull(message = "证件类型ID不能为空")
    @Schema(description = "证件类型ID")
    private Long documentTypeId;
    
    @Schema(description = "证件号码")
    private String documentNumber;
    
    @Schema(description = "正面图片路径")
    private String frontImage;
    
    @Schema(description = "反面图片路径")
    private String backImage;
    
    @Schema(description = "签发机关")
    private String issueAuthority;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "签发日期/获证日期")
    private LocalDate issueDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "有效期至")
    private LocalDate expiryDate;
    
    @Schema(description = "是否长期有效")
    private Integer isPermanent;
    
    @Schema(description = "毕业院校")
    private String school;
    
    @Schema(description = "专业")
    private String major;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "毕业时间")
    private LocalDate graduationDate;
    
    @Schema(description = "证书名称")
    private String certificateName;
}
