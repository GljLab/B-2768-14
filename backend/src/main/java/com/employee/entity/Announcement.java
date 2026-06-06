package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private Long categoryId;
    
    private String content;
    
    private Integer importance;
    
    private Integer isTop;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    private Integer isForceConfirm;
    
    private String attachments;
    
    private Integer publishStatus;
    
    private Long publisherId;
    
    private String publisherName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
    
    private Integer viewCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    private String categoryName;
    
    @TableField(exist = false)
    private String categoryCode;
    
    @TableField(exist = false)
    private Integer isRead;
    
    @TableField(exist = false)
    private Integer isConfirmed;
    
    @TableField(exist = false)
    private Integer readCount;
    
    @TableField(exist = false)
    private Integer totalCount;
    
    @TableField(exist = false)
    private List<AnnouncementTarget> targets;
}
