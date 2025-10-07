package com.pulse.fineflux.domain;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProfitLossResponseDTO {
    private String id;
    private double cashReceived;
    private double inventoryValue;
    private double totalExpenses;
    private double profitLoss;
    private LocalDateTime calculatedAt;
}
