package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MilestoneVO {

    private Long partyId;

    private String theme;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime eventTime;

    private Integer partyYear;

    private Integer partyMonth;

    private String location;

    private String highlights;

    private Integer checkedInCount;

    private Integer confirmedCount;

    private Integer totalParticipantCount;

    private List<String> topPhotos;
}
