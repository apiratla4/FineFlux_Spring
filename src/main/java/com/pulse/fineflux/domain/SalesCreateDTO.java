package com.pulse.fineflux.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SalesCreateDTO {
    private String organizationId;
    private String empId;
    private String productName;
    private double openingStock;
    private double closingStock;
    private double testingTotal;
    private double salesInLiters;
    private double price;
    private float salesInRupees;
    /**
     * Date and time for the sales entry (IST timezone).
     * If provided, this value will be used for the sale record.
     * If null, the system will use the current IST time automatically.
     * This allows manual date/time selection instead of auto-updating.
     */
    private LocalDateTime dateTime;
    private String guns;

}
