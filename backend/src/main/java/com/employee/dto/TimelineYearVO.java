package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TimelineYearVO {

    private Integer year;

    private Integer birthdayOrder;

    private String tenureDisplay;

    private Integer wishCount;

    private String employeeMessage;

    private Long employeeMessageId;

    private Boolean canEditEmployeeMessage;

    private List<AdminMessageVO> adminMessages;

    private PartyInfoVO partyInfo;

    private Boolean isLeft;

    @Data
    public static class AdminMessageVO {
        private Long id;
        private Long adminId;
        private String adminName;
        private String content;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime createdAt;
        private Boolean canEdit;
    }

    @Data
    public static class PartyInfoVO {
        private Long partyId;
        private String theme;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime eventTime;
        private String location;
        private List<String> photoUrls;
    }
}
