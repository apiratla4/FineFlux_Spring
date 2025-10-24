package com.pulse.fineflux.domain;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRegisterResponseDTO {
    private String id;
    private LocalDateTime dateTime;
    private String productName;
    private String organizationId;
    private double dipReadingProduct;
    private double dipReadingWater;
    private double netStockLiters;
    private double totalOpeningStock;
    private double receiptQuantityInLitres;
    private double closingStockInLitres;
    private double saleAsForTankStock;
    private double actualSalesAsPerMeter;
    private double stockVariation;
}
