package com.pulse.fineflux.domain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeterSalesDayDynamicDTO {
    private String date;
    private Map<String, Double> openingReadingsPerGun;
    private Map<String, Double> salesInLitersPerGun;
    private double testingTotal;
    private double asPerMeterSale;
    private double cumulativeLiters;
}
