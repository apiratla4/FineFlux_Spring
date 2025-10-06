package com.pulse.fineflux.domain;


import lombok.Data;

@Data
public class SalesUpdateDTO {
    private double openingStock;
    private double closingStock;
    private double testingTotal;
    private double salesInLiters;
    private double price;
    private float salesInRupees;

}
