// domain/EmployeeDuty.java
package com.pulse.fineflux.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "employee_duties")
public class EmployeeDuty {

    @Id
    private String id;
    private String organizationId;
    private String empId;
    private LocalDate dutyDate;
    private List<String> productIds;
    private List<String> gunIds;
    private String shiftStart;
    private String shiftEnd;
    private Double totalHours;
    private String status; // SCHEDULED, ACTIVE, COMPLETED, CANCELLED
    private Date createdAt;
    private Date updatedAt;

    // Calculate total hours based on shift times
    public void calculateTotalHours() {
        if (shiftStart != null && shiftEnd != null) {
            LocalTime start = LocalTime.parse(shiftStart);
            LocalTime end = LocalTime.parse(shiftEnd);

            Duration duration;
            if (end.isBefore(start)) {
                // Overnight shift
                duration = Duration.between(start, LocalTime.MAX)
                        .plus(Duration.between(LocalTime.MIN, end))
                        .plusMinutes(1);
            } else {
                duration = Duration.between(start, end);
            }

            this.totalHours = Math.round((duration.toMinutes() / 60.0) * 100.0) / 100.0;
        }
    }
}
