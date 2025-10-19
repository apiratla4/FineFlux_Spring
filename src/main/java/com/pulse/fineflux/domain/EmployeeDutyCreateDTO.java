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
    private String organizationId;

    @NotBlank(message = "Employee ID is required")
    private String empId;

    @NotNull(message = "Duty date is required")
    private LocalDate dutyDate;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotEmpty(message = "At least one gun must be assigned")
    private List<String> gunIds;

    @NotBlank(message = "Shift start time is required")
    private String shiftStart;

    @NotBlank(message = "Shift end time is required")
    private String shiftEnd;

    private String status;
}
