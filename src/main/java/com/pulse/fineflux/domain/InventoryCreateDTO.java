package com.pulse.fineflux.domain;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCreateDTO {

    @NotBlank
    private String organizationId;
    @NotBlank
    private String productId;
    private String empId;
    private String productName;
    private BigDecimal totalCapacity;
    private BigDecimal stockValue;
    private BigDecimal currentLevel;
    private String metric;
    private Boolean status;
    private BigDecimal tankCapacity;
    private LocalDateTime lastUpdated;
    private double receiptQuantityInLitres;
}
