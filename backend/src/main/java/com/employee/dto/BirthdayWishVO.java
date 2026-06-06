package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BirthdayWishVO {

    private Long id;
    private Long recipientId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private Integer likeCount;
    private Integer isSystem;
    private Boolean hasLiked;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
}
