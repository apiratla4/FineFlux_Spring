// src/main/java/com/pulse/fineflux/entity/Customer.java
package com.pulse.fineflux.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

// Lombok (optional, remove if not using Lombok)
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "customers")
public class Customer {

    @Id
    private String id;

    @NotBlank
    private String customerName;

    @NotBlank
    private String customerVehicleNum;

    // Employee id of the staff who handled this borrower
    @NotBlank
    private String empId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amountBorrowed;

    @PastOrPresent
    private LocalDate borrowDate;

    @FutureOrPresent
    private LocalDate dueDate;

    @NotNull
    private BorrowStatus status;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone")
    private String phoneNumber;

    @Email
    private String email;

    private String notes;

    private Address address;

    public enum BorrowStatus {
        PENDING, PARTIAL, PAID, OVERDUE
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Address {
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
}
