package com.trading.bot.api;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.trading.bot.api.dto.CancelOrderDto;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.api.dto.PlaceOrderDto;
import com.trading.bot.model.enums.Symbol;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.LinkedHashMap;


public class BinanceApiClientBean implements BinanceApiClient {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String BASE_URL = dotenv.get("BINANCE_BASE_URL");

    private static final String API_KEY = dotenv.get("BINANCE_API_KEY");
    private static final String SECRET_KEY = dotenv.get("BINANCE_SECRET_KEY");

    private final SpotClient client;

    public BinanceApiClientBean() {
        this.client = new SpotClientImpl(API_KEY, SECRET_KEY, BASE_URL);
    }
    public void getTestBalance() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", Symbol.BTCUSDT.toString());
        params.put("limit", 10);

        String response = client.createMarket().depth(params);
        System.out.println(response);
    }

    @Override
    public Double getPrice() {
        return 0.0;
    }

    @Override
    public OrderBookDto getOrderBook(Symbol market, int limit) {
        return null;
    }

    @Override
    public void placeOrder(PlaceOrderDto dto) {

    }

    @Override
    public void cancelOrder(CancelOrderDto dto) {

    }
}
