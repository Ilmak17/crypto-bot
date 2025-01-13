package com.trading.bot.service;

import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderType;

import java.util.ArrayList;
import java.util.Comparator;
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
            sellOrders.sort(Comparator.comparing(Order::getPrice));
        }

        executeOrders();
    }

    @Override
    public void executeOrders() {
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            Order buyOrder = buyOrders.get(0);
            Order sellOrder = sellOrders.get(0);

            if (buyOrder.getPrice() >= sellOrder.getPrice()) {
                double quantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                buyOrder.fill(quantity);
                sellOrder.fill(quantity);

                System.out.printf("Matched: BUY %.6f @ %.2f, SELL %.6f @ %.2f%n",
                        quantity, buyOrder.getPrice(), quantity, sellOrder.getPrice());

                if (buyOrder.getQuantity() == 0) {
                    buyOrders.remove(0);
                }
                if (sellOrder.getQuantity() == 0) {
                    sellOrders.remove(0);
                }
            } else {
                break;
            }
        }
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
