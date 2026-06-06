package com.employee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CheckinRequest {

    @NotNull
    private Long partyId;
    @NotNull
    private List<Long> employeeIds;
}
