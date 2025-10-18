// src/main/java/com/pulse/fineflux/entity/Employee.java
package com.pulse.fineflux.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;
import java.time.Instant;

@Setter
@Getter
@Document(collection = "employees")
@CompoundIndex(name = "org_username_unique_idx", def = "{'organizationId': 1, 'username': 1}", unique = true)
public class Employee {

    @Id
    private String id;

    @NotBlank
    private String organizationId;

    @NotBlank
    @Indexed(unique = true)
    private String empId;

    // ACTIVE or INACTIVE (stored in uppercase)
    @NotBlank
    private String status;

    @NotBlank
    private String role;

    @NotBlank
    private String department;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone")
    private String phoneNumber;

    @Email
    @NotBlank
    @Indexed(unique = true)
    private String emailId;

    @NotBlank
    private String username;

    @NotBlank
    private String passwordHash;
    private String profileImageUrl;
    private String gender; // e.g., Male, Female, Other, Prefer not to say
    @Min(0)
    private Double salary; // Employee salary

    @CreatedDate
    private Instant joinedDate;

    private ShiftTiming shiftTiming;
    private Address address;
    private EmergencyContact emergencyContact;

    public Employee() {}

    @Setter
    @Getter
    public static class ShiftTiming {
        @Pattern(regexp = "^[0-2][0-9]:[0-5][0-9]$", message = "Invalid start time")
        private String start;

        @Pattern(regexp = "^[0-2][0-9]:[0-5][0-9]$", message = "Invalid end time")
        private String end;

        public ShiftTiming() {}
    }

    @Setter
    @Getter
    public static class Address {
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String postalCode;
        private String country;

        public Address() {}
    }

    @Setter
    @Getter
    public static class EmergencyContact {
        @NotBlank
        private String name;

        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone")
        private String phone;

        private String relationship;

        public EmergencyContact() {}
    }
}
