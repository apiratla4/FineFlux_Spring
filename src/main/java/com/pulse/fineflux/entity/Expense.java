package com.pulse.fineflux.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "expense")
public class Expense {
    @Id
    private String id;
    private String description;
    private double amount;
    private String categoryName; // must exist in ExpenseCategory
    private LocalDate expenseDate;
    private LocalDateTime createdAt;
    private String organizationId;
    private String empId;
}
