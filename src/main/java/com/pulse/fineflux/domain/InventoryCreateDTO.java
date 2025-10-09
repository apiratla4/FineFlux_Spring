package com.pulse.fineflux.domain;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCreateDTO {

    @NotBlank
    private String organizationId;
    @NotBlank
    private String productId;

    private String productName;
    private BigDecimal totalCapacity;
    private BigDecimal stockValue;
    private BigDecimal currentLevel;
    private Integer employeeId;
    private String metric;
    private Boolean status;
    private BigDecimal tankCapacity;
}
