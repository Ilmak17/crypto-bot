package com.trading.bot;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.api.BinanceApiClientBean;
import com.trading.bot.bots.Bot;
import com.trading.bot.bots.DumbBot;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        BinanceApiClient apiClient = new BinanceApiClientBean();

        Bot bot = new DumbBot("DumbBot_1", 1000.0);

        for (int i = 0; i < 5; i++) {
            System.out.println("\n--- Iteration " + (i + 1) + " ---");

            double currentPrice = apiClient.getPrice();
            System.out.printf("Current BTC/USDT price: %.2f%n", currentPrice);
            Thread.sleep(5000);
            bot.performAction(currentPrice);

            bot.getBalance();
        }
    }
}
