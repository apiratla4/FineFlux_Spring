package com.pulse.fineflux.domain;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponseDTO {
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
}
