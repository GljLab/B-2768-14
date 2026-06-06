package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("birthday_party_photo")
public class BirthdayPartyPhoto {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long partyId;

    private String photoUrl;

    private Long uploadedBy;

    private String uploadedByName;

    private Integer likeCount;

    private LocalDateTime createdAt;
}
