package com.trading.bot.api;

import com.trading.bot.api.dto.CancelOrderDto;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.api.dto.PlaceOrderDto;
import com.trading.bot.model.enums.Symbol;

public interface BinanceApiClient {

    Double getPrice();

    OrderBookDto getOrderBook(Symbol market, int limit);

    void placeOrder(PlaceOrderDto dto);

    void cancelOrder(CancelOrderDto dto);
}
