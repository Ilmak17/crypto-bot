package com.trading.bot;

import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderType;
import com.trading.bot.service.ExchangerService;
import com.trading.bot.service.ExchangerServiceBean;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        ExchangerService exchanger = new ExchangerServiceBean();

        Order buyOrder1 = Order.Builder.newBuilder()
                .id(1L)
                .price(50000D)
                .quantity(1.5)
                .type(OrderType.BUY)
                .build();
        Order sellOrder1 = Order.Builder.newBuilder()
                .id(2L)
                .price(49900D)
                .quantity(0.5)
                .type(OrderType.SELL)
                .build();
        Order sellOrder2 = Order.Builder.newBuilder()
                .id(3L)
                .price(50000D)
                .quantity(1.0)
                .type(OrderType.SELL)
                .build();

        exchanger.placeOrder(buyOrder1);
        exchanger.placeOrder(sellOrder1);
        exchanger.placeOrder(sellOrder2);

        System.out.println("Order Book:");
        exchanger.getOrderBook();

        System.out.println("\nCancelling Order ID: 3");
        exchanger.cancelOrder(3L);

        System.out.println("\nOrder Book after cancellation:");
        exchanger.getOrderBook();
    }
}
