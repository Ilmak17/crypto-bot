package com.trading.bot.service;

import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderType;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ExchangerServiceBean implements ExchangerService {
    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;

    public ExchangerServiceBean() {
        buyOrders = new PriorityQueue<>((o1, o2) -> Double.compare(o2.getPrice(), o1.getPrice()));
        sellOrders = new PriorityQueue<>(Comparator.comparingDouble(Order::getPrice));
    }

    @Override
    public void placeOrder(Order order) {
        if (order.getType() == OrderType.BUY) {
            buyOrders.add(order);
        } else {
            sellOrders.add(order);
        }
        executeOrders();
    }

    @Override
    public void executeOrders() {
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            Order buyOrder = buyOrders.peek();
            Order sellOrder = sellOrders.peek();

            if (buyOrder.getPrice() >= sellOrder.getPrice()) {
                double quantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
                buyOrder.fill(quantity);
                sellOrder.fill(quantity);

                System.out.printf("Matched: BUY %.6f @ %.2f, SELL %.6f @ %.2f%n",
                        quantity, buyOrder.getPrice(), quantity, sellOrder.getPrice());

                if (buyOrder.isFilled()) buyOrders.poll();
                if (sellOrder.isFilled()) sellOrders.poll();
            } else {
                break;
            }
        }
    }

    @Override
    public void getOrderBook() {
        System.out.println("Buy Orders: " );
        buyOrders.forEach(order -> System.out.printf("ID: %s, Price: %.2f, Quantity: %.6f, Status: %s%n",
                order.getId(), order.getPrice(), order.getQuantity(), order.getStatus()));
        System.out.println("Sell Orders: ");
        sellOrders.forEach(order -> System.out.printf("ID: %s, Price: %.2f, Quantity: %.6f, Status: %s%n",
                order.getId(), order.getPrice(), order.getQuantity(), order.getStatus()));
    }

    @Override
    public boolean cancelOrder(Long orderId) {
        return buyOrders.removeIf(order -> order.getId().equals(orderId))
                || sellOrders.removeIf(order -> order.getId().equals(orderId));
    }
}
