package com.trading.bot.model;

import com.trading.bot.model.enums.OrderSourceType;
import com.trading.bot.model.enums.OrderStatus;
import com.trading.bot.model.enums.OrderType;

public class Order {
    private Long id;
    private OrderType type;
    private Double quantity;
    private Double price;
    private OrderStatus status;
    private OrderSourceType sourceType;

    private Order(Builder builder) {
        id = builder.id;
        type = builder.type;
        quantity = builder.quantity;
        price = builder.price;
        status = builder.status;
    }

    public boolean isFromBinance() {
        return OrderSourceType.BINANCE.equals(sourceType);
    }

    public void fill(Double amount) {
        if (amount > quantity) {
            throw new IllegalArgumentException("Amount exceeds quantity");
        }
        quantity -= amount;
        if (quantity == 0) {
            status = OrderStatus.FILLED;
            return;
        }
        status = OrderStatus.PARTIALLY_FILLED;
    }

    public boolean isFilled() {
        return status == OrderStatus.FILLED;
    }

    public void cancel() {
        if (status == OrderStatus.FILLED) {
            return;
        }
        status = (quantity > 0)
                ? OrderStatus.PARTIALLY_FILLED_CANCELLED
                : OrderStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public static final class Builder {
        private Long id;
        private OrderType type;
        private Double quantity;
        private Double price;
        private OrderStatus status;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder id(Long val) {
            id = val;
            return this;
        }

        public Builder type(OrderType val) {
            type = val;
            return this;
        }

        public Builder quantity(Double val) {
            quantity = val;
            return this;
        }

        public Builder price(Double val) {
            price = val;
            return this;
        }

        public Builder status(OrderStatus val) {
            status = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
