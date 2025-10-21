package com.pulse.fineflux.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "employee_attendance")
public class EmployeeAttendance {
    @Id
    private String id;
    private String organizationId;
    private String empId;
    private String username;
    private String present;
    private String absent;
    private Double attendanceRate;
    private Double avgHours;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private LocalDateTime breakIn;
    private LocalDateTime breakOut;
    private Long breakTimeMins;
    private Long actuallyWorkingHoursMins;
    private Long workingMins;
    private Long shortTimeMins;
    private Long extraHoursMins;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
