package com.pulse.fineflux.domain;


import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

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
    private Date lastUpdated;
}