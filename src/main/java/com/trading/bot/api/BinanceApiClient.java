package com.trading.bot.api;

import com.trading.bot.api.dto.CancelOrderDto;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.api.dto.PlaceOrderDto;

public interface BinanceApiClient {

    Double getPrice();

    OrderBookDto getOrderBook(String symbol, int limit);

    void placeOrder(PlaceOrderDto dto);

    void cancelOrder(CancelOrderDto dto);
}
