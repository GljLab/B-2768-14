package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("birthday_party_participant")
public class BirthdayPartyParticipant {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long partyId;

    private Long employeeId;

    private String employeeName;

    private String department;

    private String avatar;

    private Integer participationStatus;

    private String remark;

    private Integer checkinStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkinTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
