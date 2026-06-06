package com.employee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PartyParticipationRequest {

    @NotNull
    private Integer participationStatus;
    private String remark;
}
