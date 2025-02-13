package com.trading.bot.api.dto;


import lombok.Data;

@Data
public class PlaceOrderDto {
    private String symbol;
    private String side;
    private String type;
    private double quantity;
    private double price;
}
