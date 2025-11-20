package com.pulse.fineflux.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDutyResponseDTO {

    private String id;
    private String organizationId;
    private String empId;
    private LocalDate dutyDate;
    private List<String> productIds;
    private List<String> gunIds;
    private String shiftStart;
    private String shiftEnd;
    private Double totalHours;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}