package com.pulse.fineflux.domain;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ExpenseResponseDTO {
    private String id;
    private String description;
    private double amount;
    private String categoryName;
    private LocalDate expenseDate;
    private LocalDateTime createdAt;
    private String organizationId;
    private String empId;
    private String employeeName;
}
