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

    public static Order fromString(String orderString) {
        try {
            String[] parts = orderString.split(",");
            if (parts.length != 6) {
                throw new IllegalArgumentException("Invalid order format");
            }

            return Order.builder()
                    .id(Long.parseLong(parts[0]))
                    .type(OrderType.valueOf(parts[1]))
                    .quantity(Double.parseDouble(parts[2]))
                    .price(Double.parseDouble(parts[3]))
                    .status(OrderStatus.valueOf(parts[4]))
                    .sourceType(OrderSourceType.valueOf(parts[5]))
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse order: " + orderString, e);
        }
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%.6f,%.2f,%s,%s",
                id, type, quantity, price, status, sourceType);
    }
}
