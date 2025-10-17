package com.pulse.fineflux.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "employee_new_tasks")
public class EmployeeNewTask {
    @Id
    private String id;
    private String organizationId;
    private String taskTitle;
    private String description;
    private String priority;    // e.g., "Low", "Medium", "High"
    private String shift;         // e.g., "Morning", "Afternoon", "Night"
    private String assignedToEmpId; // employee.empId (unique)
    private String assignedToName;  // for UI convenience
    private LocalDate dueDate;
    private String status;        // "pending", "in-progress", "completed"
}
