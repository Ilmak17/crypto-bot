package com.trading.bot.bots;

import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderStatus;
import com.trading.bot.model.enums.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DumbBotTest {

    private DumbBotBean bot;

    @BeforeEach
    void setUp() {
        bot = new DumbBotBean("TestBot", 1000.0);
    }

    @Test
    void testInitialBalance() {
        assertEquals(1000.0, bot.getUsdtBalance());
        assertEquals(0.0, bot.getBtcBalance());
    }

    @Test
    void testPlaceBuyOrder() {
        double price = 20000.0;
        bot.performAction(price);

        List<Order> history = bot.getOrderHistory();
        if (!history.isEmpty()) {
            Order order = history.get(0);
            assertEquals(OrderStatus.NEW, order.getStatus());
            assertTrue(order.getPrice() > 0);
            assertTrue(order.getQuantity() > 0);
            assertNotNull(order.getType());

            if (order.getType() == OrderType.BUY) {
                assertTrue(bot.getUsdtBalance() < 1000.0);
                assertTrue(bot.getBtcBalance() > 0.0);
            } else if (order.getType() == OrderType.SELL) {
                assertTrue(bot.getBtcBalance() <= 0.0);
            }
        }
    }

    @Test
    void testOrderHistoryGrows() {
        int previousSize = bot.getOrderHistory().size();
        bot.performAction(18000.0);
        int newSize = bot.getOrderHistory().size();

        assertTrue(newSize >= previousSize);
    }
}
