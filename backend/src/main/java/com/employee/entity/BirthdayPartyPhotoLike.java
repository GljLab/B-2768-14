package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("birthday_party_photo_like")
public class BirthdayPartyPhotoLike {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long photoId;

    private Long employeeId;

    private LocalDateTime createdAt;
}
