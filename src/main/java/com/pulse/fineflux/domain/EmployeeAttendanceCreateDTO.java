package com.pulse.fineflux.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAttendanceCreateDTO {
    private String organizationId;
    private String empId;
    private String username;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private LocalDateTime breakIn;
    private LocalDateTime breakOut;
    private String description;
}