package com.trading.bot;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.api.BinanceApiClientBean;

public class Main {
    public static void main(String[] args) {
        BinanceApiClient apiClient = new BinanceApiClientBean();

        System.out.println(apiClient.getPrice());
    }
}
