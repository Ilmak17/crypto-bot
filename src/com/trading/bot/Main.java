package com.trading.bot;

import com.trading.bot.api.BinanceApiClientBean;
import com.trading.bot.bots.Bot;
import com.trading.bot.bots.DumbBotBean;
import com.trading.bot.dao.BotDao;
import com.trading.bot.dao.InMemoryDaoBean;

public class Main {
    public static void main(String[] args) {
        BotDao memory = new InMemoryDaoBean();

        memory.add(1L, new DumbBotBean("DumbBot_1", 1000.0));
        memory.add(2L, new DumbBotBean("DumbBot_2", 2000.0));

        BinanceApiClientBean client = new BinanceApiClientBean();
        double currentPrice = client.getPrice();

        System.out.println("\nPrice of BTCUSDT: " + currentPrice);

        System.out.println("\nPerforming action for bot1:");
        memory.get(1L).performAction(currentPrice);
        memory.remove(1L);

        System.out.println("\nRemaining bots:");
        memory.getAll().forEach(Bot::getBalance);
    }
}
