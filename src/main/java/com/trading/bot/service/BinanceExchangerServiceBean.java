package com.trading.bot.service;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.api.BinanceApiClientBean;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.bots.Bot;
import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderSourceType;
import com.trading.bot.model.enums.OrderStatus;
import com.trading.bot.model.enums.OrderType;
import com.trading.bot.model.enums.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.trading.bot.model.enums.Topic.ORDER_EVENTS;
import static com.trading.bot.model.enums.Topic.PRICE_UPDATES;

public class BinanceExchangerServiceBean implements ExchangerService {
    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final BinanceApiClient binanceApiClient;
    private final ScheduledExecutorService scheduler;
    private final List<Bot> bots;

    private static final Logger logger = LoggerFactory.getLogger(BinanceExchangerServiceBean.class);
    private final Symbol market;

    public BinanceExchangerServiceBean(Symbol market) {
        this.market = market;
        this.buyOrders = new PriorityQueue<>((o1, o2) -> Double.compare(o2.getPrice(), o1.getPrice()));
        this.sellOrders = new PriorityQueue<>(Comparator.comparingDouble(Order::getPrice));
        this.kafkaEventPublisher = new KafkaEventPublisher();
        this.binanceApiClient = new BinanceApiClientBean();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.bots = new ArrayList<>();

        startAutoUpdateOrderBook();
    }

    @Override
    public void registerBot(Bot bot) {
        bot.setExchangerService(this);
        bots.add(bot);
        logger.info("Bot registered: {}", bot.getClass().getSimpleName());
    }

    @Override
    public void startBots() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            double price = binanceApiClient.getPrice();
            kafkaEventPublisher.publish(PRICE_UPDATES, String.format("Market: %s, Price: %.2f", market, price));
            bots.forEach(bot -> bot.performAction(price));
        }, 0, 10, TimeUnit.SECONDS);
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

            if (buyOrder.getPrice() < sellOrder.getPrice()) {
                break;
            }

            double quantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
            buyOrder.fill(quantity);
            sellOrder.fill(quantity);

            kafkaEventPublisher.publish(ORDER_EVENTS,
                    String.format("Matched: BUY %.6f @ %.2f, SELL %.6f @ %.2f",
                            quantity, buyOrder.getPrice(), quantity, sellOrder.getPrice()));

            if (buyOrder.isFilled()) buyOrders.poll();
            if (sellOrder.isFilled()) sellOrders.poll();
        }
    }

    @Override
    public boolean cancelOrder(Long orderId) {
        Optional<Order> orderToCancel = buyOrders.stream()
                .filter(order -> order.getId().equals(orderId))
                .findFirst()
                .or(() -> sellOrders.stream().filter(order -> order.getId().equals(orderId)).findFirst());

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
}
