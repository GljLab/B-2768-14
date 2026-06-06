package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BirthdayWallEmployeeVO {

    private Long id;
    private String name;
    private String department;
    private String avatar;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdayDate;
    @JsonFormat(pattern = "MM-dd")
    private String birthdayDisplay;
    private Integer hireYears;
    private Boolean isToday;
}
