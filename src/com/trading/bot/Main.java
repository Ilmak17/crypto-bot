package com.trading.bot;

import com.trading.bot.api.BinanceApiClientBean;
import com.trading.bot.bots.DumbBotBean;
import com.trading.bot.bots.SmartBotBean;
import com.trading.bot.dao.BotDao;
import com.trading.bot.dao.InMemoryDaoBean;
import com.trading.bot.service.TradingSimulatorService;
import com.trading.bot.service.TradingSimulatorServiceBean;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        BinanceApiClientBean apiClient = new BinanceApiClientBean();

        BotDao botDao = new InMemoryDaoBean();
        botDao.add(1L, new DumbBotBean("DumbBot", 1000.0));
        botDao.add(2L, new SmartBotBean("SmartBot", 1000.0));

        TradingSimulatorService simulator = new TradingSimulatorServiceBean(apiClient, botDao);

        simulator.start();

        Thread.sleep(30000);

        simulator.stop();

        System.out.println("\nSimulation Results:");
        botDao.getAll().forEach(bot -> {
            bot.getBalance();
            if (bot instanceof SmartBotBean smartBot) {
                System.out.printf("SmartBot ");
                System.out.printf("Profit: %.2f USDT%n", smartBot.calculateProfit());
                System.out.printf(" Average Profit per Operation: %.2f USDT%n", smartBot.calculateAverageProfit());
                System.out.printf("Successful Trades: %d%n", smartBot.countSuccessfulTrades());
            } else if (bot instanceof DumbBotBean dumbBot) {
                System.out.printf("Profit: %.2f USDT%n", dumbBot.calculateProfit());
                System.out.printf("Average Profit per Operation: %.2f USDT%n", dumbBot.calculateAverageProfit());
                System.out.printf("Successful Trades: %d%n", dumbBot.countSuccessfulTrades());
            }
        });
    }
}
