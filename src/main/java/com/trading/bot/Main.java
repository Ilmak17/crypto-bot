package com.trading.bot;

import com.trading.bot.api.BinanceApiClientBean;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.model.enums.Symbol;

public class Main {
    public static void main(String[] args) {
        BinanceApiClientBean binanceClient = new BinanceApiClientBean();

        System.out.println("Fetching BTC/USDT price...");
        System.out.println("Current BTC/USDT price: " + binanceClient.getPrice();

        System.out.println("\nFetching Order Book...");
        OrderBookDto orderBook = binanceClient.getOrderBook(Symbol.BTCUSDT, 5);
        System.out.println("Order Book: " + orderBook);
    }
}
