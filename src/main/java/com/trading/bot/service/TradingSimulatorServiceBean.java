package com.trading.bot.service;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.bots.Bot;
import com.trading.bot.event.KafkaEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.trading.bot.model.enums.Topic.PRICE_UPDATES;

public class TradingSimulatorServiceBean implements TradingSimulatorService {
    private final BinanceApiClient binanceApiClient;
    private final ExchangerServiceBean exchangerService;
    private final ScheduledExecutorService scheduler;
    private final List<Bot> bots;
    private final KafkaEventPublisher kafkaEventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(TradingSimulatorServiceBean.class);

    public TradingSimulatorServiceBean(BinanceApiClient binanceApiClient, ExchangerServiceBean exchangerService, List<Bot> bots) {
        this.binanceApiClient = binanceApiClient;
        this.exchangerService = exchangerService;
        this.bots = bots;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.kafkaEventPublisher = new KafkaEventPublisher();
    }

    @Override
    public void start() {
        logger.info("Starting trading...");

        bots.forEach(bot -> bot.setExchangerService(exchangerService));

        scheduler.scheduleAtFixedRate(() -> {
            try {
                double price = binanceApiClient.getPrice();
                logger.info("Current BTC price: {}", price);
                kafkaEventPublisher.publish(PRICE_UPDATES, "BTCUSDT: " + price);
                bots.forEach(bot -> bot.performAction(price));

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
