
package com.pulse.fineflux.domain;

import jakarta.validation.constraints.*;

public class OrganizationCreateRequest {
    @NotBlank public String organizationId;
    @NotBlank public String organizationName;
    public String gstNumber;
    @NotBlank public String address1;
    public String address2;
    @NotBlank public String city;
    @NotBlank public String state;
    @NotBlank public String country;
    @NotBlank public String postalCode;
    @Pattern(regexp="^\\+?[0-9]{7,15}$") public String phoneNumber;
    @Email public String email;
    public String licenseNumber;
    @NotBlank public String ownerFirstName;
    @NotBlank public String ownerLastName;
}
