package com.pulse.fineflux.domain;
import lombok.Data;

@Data
public class FinanceSummaryUpdateDTO {
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
