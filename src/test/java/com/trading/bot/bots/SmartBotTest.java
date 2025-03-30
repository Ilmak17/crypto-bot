package com.trading.bot.bots;

import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderStatus;
import com.trading.bot.model.enums.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmartBotTest {

    private SmartBotBean bot;

    @BeforeEach
    void setUp() {
        bot = new SmartBotBean("SmartTestBot", 10000.0);
    }

    @Test
    void testInitialBalances() {
        assertEquals(10000.0, bot.getUsdtBalance());
        assertEquals(0.0, bot.getBtcBalance());
    }

    @Test
    void testPerformBuyActionOnFallingTrend() {
        bot.performAction(29000.0);
        bot.performAction(28000.0);
        bot.performAction(27000.0);
        bot.performAction(26000.0);
        bot.performAction(25000.0);

        List<Order> history = bot.getOrderHistory();
        assertFalse(history.isEmpty(), "Order history should not be empty");

        Order order = history.get(0);
        assertEquals(OrderType.BUY, order.getType());
        assertEquals(OrderStatus.NEW, order.getStatus());
        assertTrue(order.getQuantity() > 0);
        assertTrue(order.getPrice() > 0);
    }

    @Test
    void testPerformSellActionOnRisingTrend() {
        bot.performAction(25000.0);
        Order buyOrder = bot.getOrderHistory().stream()
                .filter(o -> o.getType() == OrderType.BUY)
                .findFirst()
                .orElse(null);
        assertNotNull(buyOrder, "Should place a buy order first to get BTC");

        bot.performAction(26000.0);
        bot.performAction(27000.0);
        bot.performAction(28000.0);
        bot.performAction(29000.0);
        bot.performAction(30000.0);

        long sellCount = bot.getOrderHistory().stream()
                .filter(o -> o.getType() == OrderType.SELL)
                .count();

        assertTrue(sellCount > 0, "Should place at least one SELL order");
    }

    @Test
    void testOrderHistoryGrowth() {
        int before = bot.getOrderHistory().size();
        bot.performAction(27000.0);
        bot.performAction(26000.0);
        bot.performAction(25000.0);
        int after = bot.getOrderHistory().size();

        assertTrue(after >= before, "Order history should grow or stay same");
    }
}