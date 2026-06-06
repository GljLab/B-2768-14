package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("birthday_message")
public class BirthdayMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recipientId;

    private Integer recipientType;

    private String title;

    private String content;

    private String type;

    private Long relatedId;

    private Integer isRead;

    private LocalDateTime readTime;

    private LocalDateTime createdAt;
}
