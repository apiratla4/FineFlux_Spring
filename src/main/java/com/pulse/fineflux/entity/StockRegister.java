package com.pulse.fineflux.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "stockregister")
public class StockRegister {
    @Id
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
    private double stockVariation; // actualSalesAsPerMeter - saleAsForTankStock
}
