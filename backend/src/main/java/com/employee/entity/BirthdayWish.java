package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("birthday_wish")
public class BirthdayWish {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recipientId;

    private Long senderId;

    private String senderName;

    private String senderAvatar;

    private String content;

    private Integer likeCount;

    private Integer isSystem;

    private LocalDateTime createdAt;
}
