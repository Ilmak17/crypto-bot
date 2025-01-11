package com.trading.bot.service;

import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderType;

import java.util.ArrayList;
import java.util.List;

public class ExchangerServiceBean implements ExchangerService {
    private final List<Order> buyOrders = new ArrayList<>();
    private final List<Order> sellOrders = new ArrayList<>();

    @Override
    public void placeOrder(Order order) {
        if (order.getType().equals(OrderType.BUY)) {
            buyOrders.add(order);
        } else {
            sellOrders.add(order);
        }
    }

    @Override
    public void executeOrder(Order order) {

    }

    @Override
    public void getOrderBook() {
        System.out.println("Buy Orders:" );
        buyOrders.forEach(order -> System.out.printf("ID: %s, Price: %.2f, Quantity: %.6f, Status: %s%n",
                order.getId(), order.getPrice(), order.getQuantity(), order.getStatus()));
        System.out.println("Sell Orders:");
        sellOrders.forEach(order -> System.out.printf("ID: %s, Price: %.2f, Quantity: %.6f, Status: %s%n",
                order.getId(), order.getPrice(), order.getQuantity(), order.getStatus()));
    }
}
