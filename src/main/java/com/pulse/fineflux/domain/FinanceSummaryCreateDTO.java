package com.pulse.fineflux.domain;

import lombok.Data;

@Data
public class FinanceSummaryCreateDTO {
    private String organizationId;
    private double cashReceived;
    private double phonePay;
    private double creditCard;
    private double petrolInventory;
    private double deiselInventory;
    private double fPetrolInventory;
    private double cngInventory;
    private double twoTInventory;
    private double totalExpenses;
    private String description;
    private double total;
}
