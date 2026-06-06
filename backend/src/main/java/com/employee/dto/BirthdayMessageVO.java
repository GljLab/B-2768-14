package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BirthdayMessageVO {

    private Long id;
    private Long recipientId;
    private String title;
    private String content;
    private String type;
    private Long relatedId;
    private Integer isRead;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
}
