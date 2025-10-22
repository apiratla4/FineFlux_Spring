package com.pulse.fineflux.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "profit_loss")
public class ProfitLoss {

    @Id
    private String id;

    private double cashReceived;
    private double inventoryValue;
    private double totalExpenses;
    private double profitLoss;
    private LocalDateTime calculatedAt;
}
