package com.pulse.fineflux.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
public class InventoryLogResponseDTO {
    private String id;                   // <-- MongoDB id, required for deletion and row keys
    private String inventoryId;
    private String organizationId;
    private String productId;
    private String productName;
    private BigDecimal totalCapacity;
    private BigDecimal stockValue;
    private LocalDateTime lastUpdated;
    private String empId;
    private BigDecimal currentLevel;
    private String metric;
    private Boolean status;
    private BigDecimal tankCapacity;
    private double receiptQuantityInLitres;
    private String mutationby;
}
