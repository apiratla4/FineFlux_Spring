
package com.pulse.fineflux.domain;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class DocumentUpdateRequest {
    public String documentType;
    public String organizationId;
    public String issuingAuthority;
    public LocalDate issuedDate;
    public LocalDate expiryDate;
    public Integer renewalPeriodDays;
    public String responsibleParty;
    public String fileUrl;
    public String notes;
}
