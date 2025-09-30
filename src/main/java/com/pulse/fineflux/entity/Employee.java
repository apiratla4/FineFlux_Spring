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
// Example: make username unique per organization; comment out if you want global unique username
@CompoundIndex(name = "org_username_unique_idx", def = "{'organizationId': 1, 'username': 1}", unique = true)
public class Employee {

    @Id
    private String id; // MongoDB _id

    // Business key referencing Organization.organizationId (not Mongo _id)
    @NotBlank
    private String organizationId;

    // Business employee identifier distinct from Mongo _id (e.g., "EMP-0001")
    @NotBlank
    @Indexed(unique = true)
    private String empId;

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
    @Indexed(unique = true) // global unique email across orgs; change to compound if needed per-org
    private String emailId;

    @NotBlank
    // Remove the simple unique index because a compound unique (orgId+username) is defined above.
    // If you want global unique username instead, delete the @CompoundIndex and uncomment the next line.
    // @Indexed(unique = true)
    private String username;

    @NotBlank
    private String passwordHash;

    @CreatedDate
    private Instant joinedDate;

    private ShiftTiming shiftTiming;

    private Address address;

    private EmergencyContact emergencyContact;

    public Employee() {}

    @Setter
    @Getter
    public static class ShiftTiming {
        // 24h HH:mm (store as text or split into hour/minute)
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
