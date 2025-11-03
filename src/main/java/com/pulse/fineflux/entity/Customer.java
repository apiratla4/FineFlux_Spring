// src/main/java/com/pulse/fineflux/entity/Customer.java
package com.pulse.fineflux.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "customers")
@CompoundIndex(name = "unique_org_cust", def = "{'organizationId':1,'custId':1}", unique = true) // unique within org [web:13]
public class Customer {

    @Id
    private String id;

    @NotBlank
    private String organizationId;

    @NotBlank
    private String custId;

    @NotBlank
    private String customerName;

    @NotBlank
    private String customerVehicleNum;

    @NotBlank
    private String empId;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal amountBorrowed;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal totalBorrowedAmount;

    @PastOrPresent
    private LocalDate borrowDate;

    @FutureOrPresent
    private LocalDate dueDate;

    @NotNull
    private BorrowStatus status;

    // NEW: lifecycle flag for UI Active/InActive
    @NotNull
    private LifecycleStatus lifecycleStatus; // ACTIVE or INACTIVE [web:23]

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone")
    private String phoneNumber;

    @Email
    private String email;

    private String notes;

    private Address address;

    public enum BorrowStatus {
        PENDING, PARTIAL, PAID, OVERDUE
    }

    public enum LifecycleStatus {
        ACTIVE, INACTIVE
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

