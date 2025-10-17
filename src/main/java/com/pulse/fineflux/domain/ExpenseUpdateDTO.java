package com.pulse.fineflux.domain;


import lombok.Data;
import java.time.LocalDate;

@Data
public class ExpenseUpdateDTO {
    private String description;
    private double amount;
    private String categoryName;
    private LocalDate expenseDate;
    private String organizationId;
}
