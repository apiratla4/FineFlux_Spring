package com.pulse.fineflux.domain;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class FinanceSummaryResponseDTO {
    private String id;
    private String organizationId;
    private LocalDateTime createdAt;
    private double cashReceived;
    private double phonePay;
    private double creditCard;
    private double petrolInventory;
    private double dieselInventory;
    private double premiumPetrolInventory;
    private double cngInventory;
    private double twoTInventory;
    private double totalExpenses;
    private String description;
    private double total;
}
