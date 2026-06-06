package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("birthday_yearly_message")
public class BirthdayYearlyMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long employeeId;

    private Integer year;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
