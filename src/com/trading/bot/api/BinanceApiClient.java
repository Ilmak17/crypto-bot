package com.trading.bot.api;

public interface BinanceApiClient {

    Double getPrice();

    String getOrderBook(String symbol, int limit);
}
