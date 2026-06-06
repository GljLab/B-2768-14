package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BirthdayPartyParticipantVO {

    private Long id;
    private Long partyId;
    private Long employeeId;
    private String employeeName;
    private String department;
    private String avatar;
    private Integer participationStatus;
    private String remark;
    private Integer checkinStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime checkinTime;
}
