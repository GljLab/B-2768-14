package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("document_history")
public class DocumentHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long documentId;
    
    private Long employeeId;
    
    private Long documentTypeId;
    
    private String documentNumber;
    
    private String frontImage;
    
    private String backImage;
    
    private String issueAuthority;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    private Integer isPermanent;
    
    private String school;
    
    private String major;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate graduationDate;
    
    private String certificateName;
    
    private String extraInfo;
    
    private Integer auditStatus;
    
    private String auditReason;
    
    private Long auditorId;
    
    private LocalDateTime auditTime;
    
    private Integer version;
    
    private LocalDateTime replacedAt;
    
    private LocalDateTime createdAt;
}
