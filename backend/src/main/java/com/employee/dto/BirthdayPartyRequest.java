package com.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BirthdayPartyRequest {

    @NotBlank
    private String theme;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime eventTime;
    @NotBlank
    private String location;
    private String flow;
    private String coverImage;
    private String highlights;
}
