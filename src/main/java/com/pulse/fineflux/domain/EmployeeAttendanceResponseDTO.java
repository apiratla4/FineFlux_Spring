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
public class EmployeeAttendanceResponseDTO {
    private String id;
    private String empId;
    private String username;
    private String organizationId;
    private String present;
    private String absent;
    private String attendanceRate;
    private String avgHours;
    private String checkIn;        // Changed to String
    private String checkOut;       // Changed to String
    private String actuallyWorkingHours;
    private String working;
    private String shortTime;
    private String extraHours;
    private String breakTime;
    private String description;
}
