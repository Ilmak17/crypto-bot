package com.trading.bot.service;

import com.trading.bot.events.EventBus;
import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderType;

import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;

public class ExchangerServiceBean implements ExchangerService {
    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;
    private final EventBus eventBus;

    public ExchangerServiceBean() {
        buyOrders = new PriorityQueue<>((o1, o2) -> Double.compare(o2.getPrice(), o1.getPrice()));
        sellOrders = new PriorityQueue<>(Comparator.comparingDouble(Order::getPrice));
        eventBus = new EventBus();
    }

    @Override
    public void placeOrder(Order order) {
        Optional.of(order)
                .map(Order::getType)
                .filter(type -> type == OrderType.BUY)
                .ifPresentOrElse(type -> buyOrders.add(order),
                        () -> sellOrders.add(order));

        eventBus.publish("ORDER_PLACED", String.format("New order: %s", order));

        executeOrders();
    }

    @Override
    public void executeOrders() {
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            Order buyOrder = buyOrders.peek();
            Order sellOrder = sellOrders.peek();
            if (buyOrder.getPrice() < sellOrder.getPrice()) {
                break;
            }

            double quantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
            buyOrder.fill(quantity);
            sellOrder.fill(quantity);

            eventBus.publish("ORDER_FILLED",
                    String.format("Matched: BUY %.6f @ %.2f, SELL %.6f @ %.2f",
                            quantity, buyOrder.getPrice(), quantity, sellOrder.getPrice()));

            if (buyOrder.isFilled()) buyOrders.poll();
            if (sellOrder.isFilled()) sellOrders.poll();
        }
    }

    @Override
    public boolean cancelOrder(Long orderId) {
        boolean removed = buyOrders.removeIf(order -> order.getId().equals(orderId))
                || sellOrders.removeIf(order -> order.getId().equals(orderId));
        if (removed) {
            eventBus.publish("ORDER_CANCELLED", String.format("Order cancelled: %s", orderId));
        }

        return removed;
    }

    @Override
    public void getOrderBook() {
        System.out.println("Buy Orders: ");
        buyOrders.forEach(order -> System.out.printf("ID: %s, Price: %.2f, Quantity: %.6f, Status: %s%n",
                order.getId(), order.getPrice(), order.getQuantity(), order.getStatus()));
        System.out.println("Sell Orders: ");
        sellOrders.forEach(order -> System.out.printf("ID: %s, Price: %.2f, Quantity: %.6f, Status: %s%n",
                order.getId(), order.getPrice(), order.getQuantity(), order.getStatus()));
    }
}
