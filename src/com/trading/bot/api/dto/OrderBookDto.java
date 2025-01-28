package com.trading.bot.api.dto;

import java.util.List;

public class OrderBookDto {
    private long lastUpdateId;
    private List<OrderEntry> bids;
    private List<OrderEntry> asks;

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

    @Override
    public String toString() {
        return "OrderBookDto{" +
                "lastUpdateId=" + lastUpdateId +
                ", bids=" + bids +
                ", asks=" + asks +
                '}';
    }

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
