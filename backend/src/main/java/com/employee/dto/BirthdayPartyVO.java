package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BirthdayPartyVO {

    private Long id;
    private String theme;
    private Integer partyYear;
    private Integer partyMonth;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime eventTime;
    private String location;
    private String flow;
    private String coverImage;
    private Integer status;
    private Long createdBy;
    private String createdByName;
    private Integer participantCount;
    private Integer attendCount;
    private Integer photoCount;
    private String highlights;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
}
