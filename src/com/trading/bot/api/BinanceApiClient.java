package com.trading.bot.api;

import com.trading.bot.api.dto.OrderBookDto;

public interface BinanceApiClient {

    Double getPrice();

    OrderBookDto getOrderBook(String symbol, int limit);
}
