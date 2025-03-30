package com.trading.bot.model;

import com.trading.bot.model.enums.OrderSourceType;
import com.trading.bot.model.enums.OrderStatus;
import com.trading.bot.model.enums.OrderType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    void testFillFully() {
        Order order = Order.builder()
                .id(1L)
                .type(OrderType.BUY)
                .quantity(1.0)
                .price(100.0)
                .status(OrderStatus.NEW)
                .sourceType(OrderSourceType.BOT)
                .build();

        order.fill(1.0);

        assertEquals(0.0, order.getQuantity());
        assertEquals(OrderStatus.FILLED, order.getStatus());
    }

    @Test
    void testFillPartially() {
        Order order = Order.builder()
                .id(2L)
                .type(OrderType.SELL)
                .quantity(2.0)
                .price(150.0)
                .status(OrderStatus.NEW)
                .sourceType(OrderSourceType.BOT)
                .build();

        order.fill(1.0);

        assertEquals(1.0, order.getQuantity());
        assertEquals(OrderStatus.PARTIALLY_FILLED, order.getStatus());
    }

    @Test
    void testFillTooMuchThrows() {
        Order order = Order.builder()
                .id(3L)
                .type(OrderType.BUY)
                .quantity(1.0)
                .price(200.0)
                .status(OrderStatus.NEW)
                .sourceType(OrderSourceType.BOT)
                .build();

        assertThrows(IllegalArgumentException.class, () -> order.fill(2.0));
    }

    @Test
    void testCancelFilledOrder() {
        Order order = Order.builder()
                .id(4L)
                .type(OrderType.BUY)
                .quantity(0.0)
                .price(100.0)
                .status(OrderStatus.FILLED)
                .sourceType(OrderSourceType.BOT)
                .build();

        order.cancel();

        assertEquals(OrderStatus.FILLED, order.getStatus());
    }

    @Test
    void testCancelPartialOrder() {
        Order order = Order.builder()
                .id(5L)
                .type(OrderType.SELL)
                .quantity(0.5)
                .price(120.0)
                .status(OrderStatus.PARTIALLY_FILLED)
                .sourceType(OrderSourceType.BOT)
                .build();

        order.cancel();

        assertEquals(OrderStatus.PARTIALLY_FILLED_CANCELLED, order.getStatus());
    }

    @Test
    void testToStringAndFromString() {
        Order original = Order.builder()
                .id(123L)
                .type(OrderType.BUY)
                .quantity(1.234)
                .price(50000.0)
                .status(OrderStatus.NEW)
                .sourceType(OrderSourceType.BOT)
                .build();

        String str = original.toString();
        Order parsed = Order.fromString(str);

        assertEquals(original, parsed);
    }
}
