package com.trading.bot;

import com.trading.bot.api.BinanceApiClientBean;
import com.trading.bot.bots.Bot;
import com.trading.bot.bots.DumbBotBean;
import com.trading.bot.dao.BotDao;
import com.trading.bot.dao.InMemoryDaoBean;
import com.trading.bot.service.TradingSimulatorService;
import com.trading.bot.service.TradingSimulatorServiceBean;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        BinanceApiClientBean binanceApiClientBean = new BinanceApiClientBean();
        BotDao botDao = new InMemoryDaoBean();
        Bot bot1 = new DumbBotBean("Bot_1", 1000.0);
        Bot bot2 = new DumbBotBean("Bot_2", 4000.0);

        botDao.add(1L, bot1);
        botDao.add(2L, bot2);

        TradingSimulatorService tradingSimulatorService = new TradingSimulatorServiceBean(
                binanceApiClientBean, botDao
        );

        tradingSimulatorService.start();
        Thread.sleep(60000);
        tradingSimulatorService.stop();

        System.out.println("History of Transactions");
        botDao.getAll().forEach(Bot::getTransactionHistory);
    }
}
