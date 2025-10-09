package com.pulse.fineflux.entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;
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
@Document(collection = "documents")
public class DocumentRecord {

    @Id
    private String id;

    @NotBlank
    private String organizationId;

    @NotBlank
    private String documentType;

    @NotBlank
    private String issuingAuthority;

    @PastOrPresent
    private LocalDate issuedDate;

    @FutureOrPresent
    private LocalDate expiryDate;

    @PositiveOrZero
    private Integer renewalPeriodDays;

    @NotBlank
    private String responsibleParty;

    @NotBlank
    private String fileUrl;

    private String notes;
}
