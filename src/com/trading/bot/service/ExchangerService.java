package com.trading.bot.service;

import com.trading.bot.model.Order;

public interface ExchangerService {
    void placeOrder(Order order);
    void executeOrder(Order order);
    void getOrderBook();
}
