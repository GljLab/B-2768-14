package com.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class YearlyMessageRequest {

    @NotNull
    private Integer year;

    @NotBlank
    @Size(max = 200)
    private String content;
}
