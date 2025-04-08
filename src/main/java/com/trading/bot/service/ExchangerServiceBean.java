package com.trading.bot.service;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.event.KafkaEventSubscriber;
import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderSourceType;
import com.trading.bot.model.enums.OrderStatus;
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

import static com.trading.bot.model.enums.Topic.ORDER_EVENTS;
import static com.trading.bot.model.enums.Topic.PRICE_UPDATES;

public class ExchangerServiceBean implements ExchangerService {
    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final BinanceApiClient binanceApiClient;
    private final ScheduledExecutorService scheduler;

    private static final Logger logger = LoggerFactory.getLogger(ExchangerServiceBean.class);
    private final Symbol market;

    public ExchangerServiceBean(Symbol market, BinanceApiClient binanceApiClient, KafkaEventPublisher kafkaEventPublisher) {
        this.market = market;
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.binanceApiClient = binanceApiClient;

        this.buyOrders = new PriorityQueue<>((o1, o2) -> Double.compare(o2.getPrice(), o1.getPrice()));
        this.sellOrders = new PriorityQueue<>(Comparator.comparingDouble(Order::getPrice));
        this.scheduler = Executors.newScheduledThreadPool(1);

        startAutoUpdateOrderBook();
        new KafkaEventSubscriber(ORDER_EVENTS.getTopicName(), this::processOrder).listen();
    }

    @Override
    public void placeOrder(Order order) {
        if (order.getType() == OrderType.BUY) {
            buyOrders.add(order);
        } else {
            sellOrders.add(order);
        }

        kafkaEventPublisher.publish(ORDER_EVENTS, String.format("New order placed: %s", order));
        executeOrders();
    }

    @Override
    public void executeOrders() {
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            Order buyOrder = buyOrders.peek();
            Order sellOrder = sellOrders.peek();

            double marketPrice = binanceApiClient.getPrice(market);

            if (buyOrder.getPrice() < sellOrder.getPrice() || buyOrder.getPrice() < marketPrice) {
                logger.info("No matching orders. Market price: {}", marketPrice);
                break;
            }

            double quantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());

            buyOrder.fill(quantity);
            sellOrder.fill(quantity);

            kafkaEventPublisher.publish(ORDER_EVENTS,
                    String.format("Matched: BUY %.6f @ %.2f, SELL %.6f @ %.2f (Market: %.2f)",
                            quantity, buyOrder.getPrice(), quantity, sellOrder.getPrice(), marketPrice));

            logger.info("Order filled: BUY {} BTC @ {}, SELL {} BTC @ {}",
                    quantity, buyOrder.getPrice(), quantity, sellOrder.getPrice());

            if (buyOrder.isFilled()) buyOrders.poll();
            if (sellOrder.isFilled()) sellOrders.poll();
        }
    }

    @Override
    public boolean cancelOrder(Long orderId) {
        Optional<Order> orderToCancel = buyOrders.stream()
                .filter(order -> orderId.equals(order.getId()))
                .findFirst()
                .or(() -> sellOrders.stream()
                        .filter(order -> orderId.equals(order.getId()))
                        .findFirst());

        if (orderToCancel.isPresent()) {
            orderToCancel.get().cancel();
            buyOrders.remove(orderToCancel.get());
            sellOrders.remove(orderToCancel.get());
            kafkaEventPublisher.publish(ORDER_EVENTS,
                    String.format("Order cancelled: %s", orderId));
            return true;
        }

        return false;
    }

    @Override
    public void stopAutoUpdate() {
        scheduler.shutdown();
    }

    private void startAutoUpdateOrderBook() {
        scheduler.scheduleAtFixedRate(this::initializeOrderBook, 0, 10, TimeUnit.SECONDS);
    }

    private void initializeOrderBook() {
        OrderBookDto orderBook = binanceApiClient.getOrderBook(market, 10);

        buyOrders.clear();
        sellOrders.clear();

        orderBook.getBids().forEach(bid -> buyOrders.add(new Order(
                null, OrderType.BUY, bid.getQuantity(), bid.getPrice(), OrderStatus.NEW, OrderSourceType.BINANCE
        )));
        orderBook.getAsks().forEach(ask -> sellOrders.add(new Order(
                null, OrderType.SELL, ask.getQuantity(), ask.getPrice(), OrderStatus.NEW, OrderSourceType.BINANCE
        )));

        kafkaEventPublisher.publish(PRICE_UPDATES, "Updated order book: " + orderBook);
        logger.info("Order Book initialized: {}", orderBook);
    }

    private void processOrder(String message) {
        try {
            Order order = Order.fromString(message);
            logger.info("Received order from Kafka: {}", order);
            placeOrder(order);
        } catch (Exception e) {
            logger.error("Failed to process order: {}", message, e);
        }
    }
}
