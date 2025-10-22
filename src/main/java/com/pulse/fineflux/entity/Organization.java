
package com.pulse.fineflux.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;
import lombok.*;
/**
 * Business key 'organizationId' is distinct from Mongo _id.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "organizations")
public class Organization {

    @Id
    private String id; // MongoDB _id

    @NotBlank
    @Indexed(unique = true) // business key
    private String organizationId;

    @NotBlank
    private String organizationName;

    // GSTIN: 15-char format, uppercase A–Z and digits
    @Pattern(regexp = "\\d{2}[A-Z]{5}\\d{4}[A-Z][A-Z\\d]Z[A-Z\\d]", message = "Invalid GST Number")
    private String gstNumber;

    @NotBlank
    private String address1;

    private String address2;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String country;

    @NotBlank
    private String postalCode;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone")
    private String phoneNumber;

    @Email
    private String email;

    private String licenseNumber;

    @NotBlank
    private String ownerFirstName;

    @NotBlank
    private String ownerLastName;
}
