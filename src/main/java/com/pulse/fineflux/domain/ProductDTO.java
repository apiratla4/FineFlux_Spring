package com.pulse.fineflux.domain;


import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String productId; // auto-generated
    private String productName;
    private Double price;
    private Boolean status;
    private BigDecimal tankCapacity;
    private String description;
    private String supplier;
    private BigDecimal currentLevel;
    private String metric;
}
