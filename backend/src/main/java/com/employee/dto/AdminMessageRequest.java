package com.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminMessageRequest {

    @NotNull
    private Long employeeId;

    @NotNull
    private Integer year;

    @NotBlank
    @Size(max = 500)
    private String content;
}
