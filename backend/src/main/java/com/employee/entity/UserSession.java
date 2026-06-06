package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_session")
public class UserSession {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Integer userType;
    
    private Long userId;
    
    private String token;
    
    private String ipAddress;
    
    private String deviceType;
    
    private LocalDateTime loginTime;
    
    private LocalDateTime expireTime;
    
    private Integer isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
