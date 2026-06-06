package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("birthday_wish_like")
public class BirthdayWishLike {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long wishId;

    private Long employeeId;

    private LocalDateTime createdAt;
}
