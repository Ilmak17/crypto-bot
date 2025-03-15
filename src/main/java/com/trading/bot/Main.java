package com.trading.bot;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.api.BinanceApiClientBean;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.api.dto.PlaceOrderDto;
import com.trading.bot.model.enums.Symbol;

public class Main {
    public static void main(String[] args) {
        BinanceApiClientBean binanceClient = new BinanceApiClientBean();

        System.out.println("Fetching BTC/USDT price...");
        Double price = 84169D;
        System.out.println("Current BTC/USDT price: " + price);

        System.out.println("\nFetching Order Book...");
        OrderBookDto orderBook = binanceClient.getOrderBook(Symbol.BTCUSDT, 5);
        System.out.println("Order Book: " + orderBook);

        System.out.println("\nPlacing test order...");
        PlaceOrderDto orderDto = new PlaceOrderDto(
                Symbol.BTCUSDT.toString(),
                "BUY",
                "limit",
                0.001,
                price * 0.99
        );
        binanceClient.placeOrder(orderDto);

        System.out.println("\nCancelling order...");
        // binanceClient.cancelOrder(new CancelOrderDto(Symbol.BTCUSDT.toString(), "order_id"));
    }
}
