package com.pulse.fineflux.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "sale_history")
@CompoundIndexes({
        // One create snapshot per saleId per org
        @CompoundIndex(name = "uniq_create_snapshot",
                def = "{'organizationId':1,'saleId':1,'mutationby':1}", unique = true),
        // Optional: fast lookup by saleId
        @CompoundIndex(name = "sale_hist_saleId_idx",
                def = "{'organizationId':1,'saleId':1}", unique = false)
})
public class SaleHistory {
    @Id
    private String id;
    private String organizationId;
    private String saleId; // stable reference to originating sale
    private LocalDateTime dateTime;
    private String productName;
    private String guns;
    private String empId;
    private double openingStock;
    private double closingStock;
    private double testingTotal;
    private double salesInLiters;
    private double price;
    private float salesInRupees;
    private double cashReceived;
    private double phonePay;
    private double creditCard;
    private double shortCollections;
    private double receivedTotal;
    // Audit
    private String mutationby;
    private LocalDateTime lastUpdated;
}
