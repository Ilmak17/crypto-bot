package com.trading.bot.model;


import com.trading.bot.model.enums.OrderSourceType;
import com.trading.bot.model.enums.OrderStatus;
import com.trading.bot.model.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class Order {
    private Long id;
    private OrderType type;
    private Double quantity;
    private Double price;
    private OrderStatus status;
    private OrderSourceType sourceType;

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
}
