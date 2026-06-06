package com.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BirthdayWishRequest {

    @NotNull
    private Long recipientId;
    @NotBlank
    private String content;
}
