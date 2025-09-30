package com.pulse.fineflux.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class InventoryLogCreateDTO {


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
