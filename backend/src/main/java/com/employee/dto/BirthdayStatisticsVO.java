package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BirthdayStatisticsVO {

    private List<MonthCountVO> birthdayDistribution;
    private List<DeptCountVO> deptDistribution;
    private Integer yearWishCount;
    private List<RankVO> activeWishers;
    private List<RankVO> popularStars;
    private Integer yearPartyCount;
    private Double avgParticipationRate;
    private Double employeeMessageRate;
    private Double adminMessageCoverageRate;
    private List<ParticipationRateVO> participationRateTrend;
    private List<ActivePartyVO> topActiveParties;

    @Data
    public static class MonthCountVO {
        private String month;
        private Integer count;
    }

    @Data
    public static class DeptCountVO {
        private String department;
        private Integer count;
    }

    @Data
    public static class RankVO {
        private Long id;
        private String name;
        private String avatar;
        private Integer count;
    }

    @Data
    public static class ParticipationRateVO {
        private Integer year;
        private Double rate;
    }

    @Data
    public static class ActivePartyVO {
        private Long id;
        private String theme;
        private Integer checkinCount;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime eventTime;
    }
}
