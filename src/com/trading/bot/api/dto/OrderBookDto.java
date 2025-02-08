package com.trading.bot.api.dto;

import java.util.List;

public class OrderBookDto {
    private long lastUpdateId;
    private List<OrderEntry> bids;
    private List<OrderEntry> asks;

    public OrderBookDto() {
    }

    public OrderBookDto(long lastUpdateId, List<OrderEntry> bids, List<OrderEntry> asks) {
        this.lastUpdateId = lastUpdateId;
        this.bids = bids;
        this.asks = asks;
    }

    public long getLastUpdateId() {
        return lastUpdateId;
    }

    public void setLastUpdateId(long lastUpdateId) {
        this.lastUpdateId = lastUpdateId;
    }

    public List<OrderEntry> getBids() {
        return bids;
    }

    public void setBids(List<OrderEntry> bids) {
        this.bids = bids;
    }

    public List<OrderEntry> getAsks() {
        return asks;
    }

    public void setAsks(List<OrderEntry> asks) {
        this.asks = asks;
    }

    // Вложенный класс OrderEntry
    public static class OrderEntry {
        private double price;
        private double quantity;

        public OrderEntry() {
        }

        public OrderEntry(double price, double quantity) {
            this.price = price;
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return String.format("OrderEntry{price=%.2f, quantity=%.6f}", price, quantity);
        }
    }
}