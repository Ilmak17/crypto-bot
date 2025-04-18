package com.trading.bot.service;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.model.enums.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.trading.bot.model.enums.Topic.PRICE_UPDATES;

public class TradingSimulatorServiceBean implements TradingSimulatorService {
    private final BinanceApiClient binanceApiClient;
    private final ScheduledExecutorService scheduler;
    private final KafkaEventPublisher kafkaEventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(TradingSimulatorServiceBean.class);

    public TradingSimulatorServiceBean(BinanceApiClient binanceApiClient, KafkaEventPublisher kafkaEventPublisher) {
        this.binanceApiClient = binanceApiClient;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    @Override
    public void start() {
        logger.info("Starting trading...");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                double price = binanceApiClient.getPrice(Symbol.BTCUSDT);
                logger.info("Current BTC price: {}", price);
                kafkaEventPublisher.publish(PRICE_UPDATES, Symbol.BTCUSDT + ": " + price);
            } catch (Exception e) {
                logger.error("Error fetching price from Binance", e);
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        scheduler.shutdown();
        logger.info("Trading stopped.");
    }
}
