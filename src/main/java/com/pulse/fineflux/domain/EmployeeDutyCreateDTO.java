package com.pulse.fineflux.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDutyCreateDTO {

    @NotBlank(message = "Organization ID is required")
    private String orgId;

    @NotBlank(message = "Employee ID is required")
    private String empId;

    @NotNull(message = "Duty date is required")
    private LocalDate dutyDate;

    @NotEmpty(message = "At least one product must be assigned")
    private List<String> products;

    @NotEmpty(message = "At least one gun must be assigned")
    private List<String> guns;

    @NotBlank(message = "Shift start time is required")
    private String shiftStart;

    @NotBlank(message = "Shift end time is required")
    private String shiftEnd;

    private String status;
}
