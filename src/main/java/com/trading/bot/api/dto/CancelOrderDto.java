package com.trading.bot.api.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CancelOrderDto {
    private String symbol;
    private String orderId;
}
