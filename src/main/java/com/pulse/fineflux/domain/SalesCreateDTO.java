package com.pulse.fineflux.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SalesCreateDTO {
    private String organizationId;
    private String empId;
    private String productName;       // <---- NEW FIELD
    private double openingStock;
    private double closingStock;
    private double testingTotal;
    private double salesInLiters;
    private double price;
    private float salesInRupees;
    private LocalDateTime dateTime;
    private String guns;

}
