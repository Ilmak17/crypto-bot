package com.trading.bot.service;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.api.BinanceApiClientBean;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderType;
import com.trading.bot.model.enums.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExchangerServiceBean implements ExchangerService {
    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final BinanceApiClient binanceApiClient;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final Logger logger = LoggerFactory.getLogger(ExchangerServiceBean.class);

    public ExchangerServiceBean() {
        buyOrders = new PriorityQueue<>((o1, o2) -> Double.compare(o2.getPrice(), o1.getPrice()));
        sellOrders = new PriorityQueue<>(Comparator.comparingDouble(Order::getPrice));
        kafkaEventPublisher = new KafkaEventPublisher("");
        binanceApiClient = new BinanceApiClientBean();
        startAutoUpdateOrderBook();
    }

    @Override
    public void placeOrder(Order order) {
        Optional.of(order)
                .map(Order::getType)
                .filter(type -> type == OrderType.BUY)
                .ifPresentOrElse(type -> buyOrders.add(order),
                        () -> sellOrders.add(order));

        kafkaEventPublisher.publish("ORDER_PLACED", String.format("New order: %s", order));

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

            kafkaEventPublisher.publish("ORDER_FILLED",
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
            kafkaEventPublisher.publish("ORDER_CANCELLED", String.format("Order cancelled: %s", orderId));
        }

        return removed;
    }

    @Override
    public void initializeOrderBook(String symbol, int limit) {
        OrderBookDto orderBook = binanceApiClient.getOrderBook(symbol, limit);

        logger.info(orderBook.toString());
    }

    @Override
    public void stopAutoUpdate() {
        scheduler.shutdown();
    }

    private void startAutoUpdateOrderBook() {
        scheduler.scheduleAtFixedRate(() ->
                        initializeOrderBook(Symbol.BTCUSDT.toString(), 10),
                0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void getOrderBook() {
        logger.info("Buy Orders: ");
        buyOrders.forEach(order -> logger.info(String.format("ID: %s, Price: %.2f, Quantity: %.6f, Status: %s%n",
                order.getId(), order.getPrice(), order.getQuantity(), order.getStatus())));
        System.out.println("Sell Orders: ");
        sellOrders.forEach(order -> logger.info(String.format("ID: %s, Price: %.2f, Quantity: %.6f, Status: %s%n",
                order.getId(), order.getPrice(), order.getQuantity(), order.getStatus())));
    }
}
