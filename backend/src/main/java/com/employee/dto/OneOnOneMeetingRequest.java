package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OneOnOneMeetingRequest {
    private Long id;
    private Long cycleId;
    private Long employeeId;
    private Long adminId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime meetingTime;
    
    private String meetingLocation;
    private Integer status;
    private String advantages;
    private String developAbilities;
    private String nextActions;
    private String developmentDirection;
}
