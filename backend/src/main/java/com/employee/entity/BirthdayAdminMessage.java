package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("birthday_admin_message")
public class BirthdayAdminMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long employeeId;

    private Long adminId;

    private String adminName;

    private Integer year;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
