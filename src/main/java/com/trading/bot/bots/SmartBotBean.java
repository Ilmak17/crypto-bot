package com.trading.bot.bots;

import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.event.KafkaEventSubscriber;
import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderStatus;
import com.trading.bot.model.enums.OrderType;
import com.trading.bot.service.ExchangerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.trading.bot.model.enums.Topic.ORDER_EVENTS;
import static com.trading.bot.model.enums.Topic.PRICE_UPDATES;

public class SmartBotBean implements Bot {
    private final String name;
    private double usdtBalance;
    private double btcBalance;
    private final List<Order> orderHistory;
    private final Queue<Double> priceHistory;
    private static final double COMMISSION = 0.001;
    private final Random random;
    private ExchangerService exchangerService;
    private final KafkaEventPublisher kafkaEventPublisher;

    private static final Logger logger = LoggerFactory.getLogger(SmartBotBean.class);

    public SmartBotBean(String name, double usdtBalance) {
        this.name = name;
        this.usdtBalance = usdtBalance;
        this.btcBalance = 0.0;
        this.orderHistory = new ArrayList<>();
        this.priceHistory = new LinkedList<>();
        this.random = new Random();
        this.kafkaEventPublisher = new KafkaEventPublisher();

        KafkaEventSubscriber priceSubscriber = new KafkaEventSubscriber(PRICE_UPDATES.getTopicName(), this::onPriceUpdate);
        priceSubscriber.listen();
    }

    @Override
    public void performAction(double price) {
        if (priceHistory.size() == 5) {
            priceHistory.poll();
        }
        priceHistory.offer(price);

        double tradeVolume = random.nextDouble(5000);
        boolean isTrendingUp = isTrendingUp(priceHistory);

        if (!isTrendingUp && usdtBalance > 0 && tradeVolume > 2500) {
            placeOrder(price, OrderType.BUY);
        } else if (isTrendingUp && btcBalance > 0 && tradeVolume > 2500) {
            placeOrder(price, OrderType.SELL);
        } else {
            logger.info("{} decided to skip.", name);
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

    @Override
    public void setExchangerService(ExchangerService exchangerService) {
        this.exchangerService = exchangerService;
    }

    private void placeOrder(double price, OrderType type) {
        double amount = (type == OrderType.BUY) ? usdtBalance * 0.5 / price : btcBalance * 0.5;
        if (amount <= 0) return;

        if (type == OrderType.BUY) {
            usdtBalance -= amount * price;
            btcBalance += amount * (1 - COMMISSION);
        } else {
            btcBalance -= amount;
            usdtBalance += amount * price * (1 - COMMISSION);
        }

        Order order = new Order(
                System.nanoTime(), type, amount, price, OrderStatus.NEW, null
        );

        orderHistory.add(order);
        exchangerService.placeOrder(order);

        String orderMessage = String.format("%s placed %s order: %.6f BTC @ %.2f", name, type, amount, price);
        logger.info(orderMessage);
        kafkaEventPublisher.publish(ORDER_EVENTS, orderMessage);
    }

    private boolean isTrendingUp(Queue<Double> priceHistory) {
        if (priceHistory.size() < 2) return false;

        double[] prices = priceHistory.stream().mapToDouble(Double::doubleValue).toArray();
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] <= prices[i - 1]) return false;
        }
        return true;
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
}