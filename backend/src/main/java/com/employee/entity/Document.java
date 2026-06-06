package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("document")
public class Document {
    @TableId(type = IdType.AUTO)
    private Long id;
    
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
    
    private Integer isArchived;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    private String documentTypeName;
    
    @TableField(exist = false)
    private String employeeName;
    
    @TableField(exist = false)
    private String typeCode;
}
