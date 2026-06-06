package com.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("employee")
public class Employee {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String email;
    
    private String phone;
    
    private Long departmentId;
    
    private String department;
    
    private String position;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdayDate;
    
    private Integer status;
    
    @JsonIgnore
    private String password;
    
    private Integer isActive;
    
    private String avatar;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Integer lockStatus;
    
    private LocalDateTime lockTime;
    
    private LocalDateTime unlockTime;
}
