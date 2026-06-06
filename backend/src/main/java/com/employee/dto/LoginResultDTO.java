package com.employee.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginResultDTO {
    
    private boolean success;
    
    private String message;
    
    private boolean locked;
    
    private Integer remainingAttempts;
    
    private LocalDateTime unlockTime;
    
    private LoginResponse loginResponse;
    
    private boolean abnormalLogin;
    
    private String abnormalMessage;
    
    public static LoginResultDTO success(LoginResponse loginResponse) {
        LoginResultDTO result = new LoginResultDTO();
        result.setSuccess(true);
        result.setLoginResponse(loginResponse);
        return result;
    }
    
    public static LoginResultDTO successWithAbnormal(LoginResponse loginResponse, String abnormalMessage) {
        LoginResultDTO result = new LoginResultDTO();
        result.setSuccess(true);
        result.setLoginResponse(loginResponse);
        result.setAbnormalLogin(true);
        result.setAbnormalMessage(abnormalMessage);
        return result;
    }
    
    public static LoginResultDTO failure(String message, int remainingAttempts) {
        LoginResultDTO result = new LoginResultDTO();
        result.setSuccess(false);
        result.setMessage(message);
        result.setRemainingAttempts(remainingAttempts);
        return result;
    }
    
    public static LoginResultDTO locked(String message, LocalDateTime unlockTime) {
        LoginResultDTO result = new LoginResultDTO();
        result.setSuccess(false);
        result.setLocked(true);
        result.setMessage(message);
        result.setUnlockTime(unlockTime);
        return result;
    }
}
