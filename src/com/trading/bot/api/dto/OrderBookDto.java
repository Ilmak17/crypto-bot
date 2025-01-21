package com.trading.bot.api.dto;

import java.util.List;

public class OrderBookDto {
    private long lastUpdateId;
    private List<OrderEntry> bids;
    private List<OrderEntry> asks;

    public static class OrderEntry {
        private final double price;
        private final double quantity;

        public OrderEntry(double price, double quantity) {
            this.price = price;
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return String.format("Price: %.2f, Quantity: %.6f", price, quantity);
        }
    }
}
