package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("one_on_one_meeting")
public class OneOnOneMeeting {
    @TableId(type = IdType.AUTO)
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
    
    private Integer employeeConfirm;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
