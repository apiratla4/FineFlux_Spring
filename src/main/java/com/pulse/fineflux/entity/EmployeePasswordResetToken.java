package com.pulse.fineflux.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "employee_password_reset_tokens")
public class EmployeePasswordResetToken {
    @Id
    private String id;

    private String orgId;
    private String token;
    private String employeeId;
    private Instant expiryDate;
}
