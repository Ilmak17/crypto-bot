package com.trading.bot.service;

import com.trading.bot.model.Order;

public interface ExchangerService {
    void placeOrder(Order order);
    void executeOrders();
    void getOrderBook();
    boolean cancelOrder(Long orderId);

    void initializeOrderBook(String symbol, int limit);
}
