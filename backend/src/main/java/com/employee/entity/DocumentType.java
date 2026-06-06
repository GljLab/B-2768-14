package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("document_type")
public class DocumentType {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String typeCode;
    
    private String typeName;
    
    private String icon;
    
    private Integer isRequired;
    
    private Integer hasExpiry;
    
    private Integer requireBothSides;
    
    private String fieldConfig;
    
    private Integer sortOrder;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
