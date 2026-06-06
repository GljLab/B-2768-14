package com.employee.dto;

import lombok.Data;

import java.util.List;

@Data
public class PosterDataVO {

    private String name;

    private String avatar;

    private Integer birthdayOrder;

    private String tenureDisplay;

    private Integer totalWishes;

    private Integer totalParties;

    private List<String> keywords;

    private String birthdayDate;

    private String companyName;
}
