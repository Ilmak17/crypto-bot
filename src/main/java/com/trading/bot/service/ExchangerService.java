package com.trading.bot.service;


import com.trading.bot.bots.Bot;
import com.trading.bot.model.Order;

public interface ExchangerService {
    void registerBot(Bot bot);
    void startBots();
    void placeOrder(Order order);
    void executeOrders();
    boolean cancelOrder(Long orderId);
    void stopAutoUpdate();
}
