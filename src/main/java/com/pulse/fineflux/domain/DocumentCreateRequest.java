
package com.pulse.fineflux.domain;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class DocumentCreateRequest {

    @NotBlank
    public String documentType;

    @NotBlank
    public String organizationId;

    @NotBlank
    public String issuingAuthority;

    @PastOrPresent
    public LocalDate issuedDate;

    @FutureOrPresent
    public LocalDate expiryDate;

    @PositiveOrZero
    public Integer renewalPeriodDays;

    @NotBlank
    public String responsibleParty;

    @NotBlank
    public String fileUrl;

    public String notes;
}
