package com.trading.bot.bots;


import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.event.KafkaEventSubscriber;
import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderStatus;
import com.trading.bot.model.enums.OrderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

import static com.trading.bot.model.enums.Topic.ORDER_EVENTS;
import static com.trading.bot.model.enums.Topic.PRICE_UPDATES;

public class DumbBotBean implements Bot {
    private final String name;
    private double usdtBalance;
    private double btcBalance;
    private final Random random;
    private final List<Order> orderHistory;
    private final KafkaEventPublisher kafkaEventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(DumbBotBean.class);

    public DumbBotBean(String name, double usdtBalance) {
        this.name = name;
        this.usdtBalance = usdtBalance;
        this.btcBalance = 0.0;
        this.random = new Random();
        this.orderHistory = new java.util.ArrayList<>();
        this.kafkaEventPublisher = new KafkaEventPublisher();

        new KafkaEventSubscriber(PRICE_UPDATES.getTopicName(), this::onPriceUpdate).listen();
        new KafkaEventSubscriber(ORDER_EVENTS.getTopicName(), this::onOrderEvent).listen();
    }

    @Override
    public void performAction(double price) {
        int action = random.nextInt(3);
        switch (action) {
            case 0 -> logger.info("{} decided to skip.", name);
            case 1 -> placeOrder(price, OrderType.BUY);
            case 2 -> placeOrder(price, OrderType.SELL);
            default -> logger.warn("{} encountered an unexpected case.", name);
        }
    }

    @Override
    public void getBalance() {
        logger.info("{} Balance: {:.2f} USDT | {:.6f} BTC", name, usdtBalance, btcBalance);
    }

    @Override
    public List<Order> getOrderHistory() {
        return orderHistory;
    }

    @Override
    public Double getUsdtBalance() {
        return usdtBalance;
    }

    @Override
    public Double getBtcBalance() {
        return btcBalance;
    }

    private void placeOrder(double price, OrderType type) {
        double amount = (type == OrderType.BUY)
                ? random.nextDouble(usdtBalance / price)
                : random.nextDouble(btcBalance);

        if (amount <= 0) return;

        if (type == OrderType.BUY) {
            usdtBalance -= amount * price;
            btcBalance += amount;
        } else {
            btcBalance -= amount;
            usdtBalance += amount * price;
        }

        Order order = new Order(
                System.nanoTime(), type, amount, price, OrderStatus.NEW, null
        );

        orderHistory.add(order);

        String orderMessage = String.format("%s placed %s order: %.6f BTC @ %.2f", name, type, amount, price);
        logger.info(orderMessage);
        kafkaEventPublisher.publish(ORDER_EVENTS, orderMessage);
    }

    private void onPriceUpdate(String message) {
        try {
            double price = Double.parseDouble(message.split(": ")[1]);
            logger.info("{} received price update: {}", name, price);
            performAction(price);
        } catch (Exception e) {
            logger.error("Failed to parse price update: {}", message, e);
        }
    }

    private void onOrderEvent(String message) {
        logger.info("{} received order update: {}", name, message);
    }
}
