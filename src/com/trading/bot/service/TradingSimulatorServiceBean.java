package com.trading.bot.service;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.dao.BotDao;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TradingSimulatorServiceBean implements TradingSimulatorService {

    private final BinanceApiClient binanceApiClient;
    private final BotDao botDao;
    private final ScheduledExecutorService scheduler;

    public TradingSimulatorServiceBean(BinanceApiClient binanceApiClient, BotDao botDao) {
        this.binanceApiClient = binanceApiClient;
        this.botDao = botDao;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void start() {
        System.out.println("Starting trading");
        Runnable task = () -> {
            try {
                Double price = binanceApiClient.getPrice();
                System.out.printf("Current BTC/USDT price: %.2f%n", price);

                botDao.getAll().forEach(bot -> bot.performAction(price));
            } catch (Exception e) {
                System.err.println("Error during trading: " + e.getMessage());
            }
        };

        scheduler.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        System.out.println("Stopping trading");
        scheduler.shutdown();
    }
}
