package com.trading.bot;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.api.BinanceApiClientBean;

public class Main {
    public static void main(String[] args) {
        BinanceApiClientBean binanceApiClient = new BinanceApiClientBean();
        binanceApiClient.getTestBalance();
    }
}
