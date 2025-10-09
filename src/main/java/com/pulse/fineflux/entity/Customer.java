// src/main/java/com/pulse/fineflux/entity/Customer.java
package com.pulse.fineflux.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

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

    public boolean getAddress;
    @Id
    private String id;

    @NotBlank
    private String organizationId;

    // Business customer ID provided by UI (unique within org)
    @NotBlank
    private String custId;

    @NotBlank
    private String customerName;

    @NotBlank
    private String customerVehicleNum;

    @NotBlank
    private String empId;

    // Current outstanding debt
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal amountBorrowed;

    // Cumulative borrowed total for reporting/quick views
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal totalBorrowedAmount;

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
