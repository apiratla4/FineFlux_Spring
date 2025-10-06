package com.pulse.fineflux.domain;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SalesResponseDTO {
    private String id;
    private String organizationId;
    private LocalDateTime dateTime;
    private String productName;
    private String guns;
    private String employeeId;
    private double openingStock;
    private double closingStock;
    private double testingTotal;
    private double salesInLiters;
    private double price;
    private float salesInRupees;
}
