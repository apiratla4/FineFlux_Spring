package com.pulse.fineflux.domain;


import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRegisterUpdateDTO {
    private String productName;
    private LocalDateTime dateTime;
    private double dipReadingProduct;
    private double dipReadingWater;
    private double netStockLiters;
    private double totalOpeningStock;
    private double receiptQuantityInLitres;
    private double closingStockInLitres;
    private double saleAsForTankStock;
    private double actualSalesAsPerMeter;
}
