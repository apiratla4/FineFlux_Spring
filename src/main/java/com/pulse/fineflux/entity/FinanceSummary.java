package com.pulse.fineflux.entity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "finance_summary")
public class FinanceSummary {
    @Id
    private String id;
    private String organizationId;
    private LocalDateTime createdAt;
    private double cashReceived;
    private double phonePay;
    private double creditCard;
    private double petrolInventory;
    private double deiselInventory;
    private double fPetrolInventory;
    private double cngInventory;
    private double twoTInventory; // note: can't start field name with digit!
    private double totalExpenses;
    private String description;
    private double total;
}
