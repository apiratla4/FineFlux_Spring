package com.pulse.fineflux.domain;


import lombok.*;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductResponseDTO {
    private String id;
    private String organizationId;
    private String productName;
    private Double price;
    private Boolean status;
    private BigDecimal tankCapacity;
    private String description;
    private String supplier;
    private BigDecimal currentLevel;
    private String metric;
}