// src/main/java/com/pulse/fineflux/domain/document/DocumentResponse.java
package com.pulse.fineflux.domain;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class DocumentResponse {
    public String id;
    public String organizationId;
    public String documentType;
    public String issuingAuthority;
    public LocalDate issuedDate;
    public LocalDate expiryDate;
    public Integer renewalPeriodDays;
    public String responsibleParty;
    public String fileUrl;
    public String notes;
}
