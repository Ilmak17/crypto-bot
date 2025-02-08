package com.trading.bot.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookDto {
    private long lastUpdateId;
    private List<OrderEntry> bids;
    private List<OrderEntry> asks;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderEntry {
        private double price;
        private double quantity;

        @Override
        public String toString() {
            return String.format("OrderEntry{price=%.2f, quantity=%.6f}", price, quantity);
        }
    }

    @Override
    public String toString() {
        return "OrderBookDto{" +
                "lastUpdateId=" + lastUpdateId +
                ", bids=" + bids +
                ", asks=" + asks +
                '}';
    }
}