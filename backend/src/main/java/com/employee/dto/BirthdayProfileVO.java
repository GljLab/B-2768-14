package com.employee.dto;

import lombok.Data;

import java.util.List;

@Data
public class BirthdayProfileVO {

    private Integer totalWishes;
    private Integer totalParties;
    private List<BirthdayWishVO> yearWishes;
    private List<BirthdayPartyVO> parties;
    private GrowthDataVO growthData;
    private List<TimelineYearVO> timeline;
}
