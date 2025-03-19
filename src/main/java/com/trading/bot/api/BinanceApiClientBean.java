package com.trading.bot.api;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.trading.bot.api.dto.CancelOrderDto;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.api.dto.PlaceOrderDto;
import com.trading.bot.api.mapper.OrderBookDtoMapper;
import com.trading.bot.model.enums.Symbol;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BinanceApiClientBean implements BinanceApiClient {
    private static final Dotenv dotenv = Dotenv.load();
    private static final Logger logger = LoggerFactory.getLogger(BinanceApiClientBean.class);

    private static final String BASE_URL = dotenv.get("BINANCE_BASE_URL");
    private static final String API_KEY = dotenv.get("BINANCE_API_KEY");
    private static final String SECRET_KEY = dotenv.get("BINANCE_SECRET_KEY");

    private static final SpotClient client;

    static {
        Dotenv dotenv = Dotenv.load();
        String baseUrl = dotenv.get("BINANCE_BASE_URL", "https://api.binance.com");
        String apiKey = dotenv.get("BINANCE_API_KEY");
        String secretKey = dotenv.get("BINANCE_SECRET_KEY");

        if (apiKey == null || secretKey == null) {
            throw new IllegalStateException("Missing Binance API credentials in .env file");
        }

        client = new SpotClientImpl(apiKey, secretKey, baseUrl);
        logger.info("Binance API Client initialized with base URL: {}", baseUrl);
    }

    @Override
    public Double getPrice() {
        Map<String, Object> map = new HashMap<>();
        map.put("symbol", Symbol.BTCUSDT.toString());

        String response = client.createMarket().tickerSymbol(map);

        return Double.parseDouble(response);
    }

    @Override
    public OrderBookDto getOrderBook(Symbol market, int limit) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", market.toString());
        params.put("limit", limit);

        String response = client.createMarket().depth(params);

        return new OrderBookDtoMapper().toOrderBookDto(response);
    }

    @Override
    public void placeOrder(PlaceOrderDto dto) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", dto.getSymbol());
        params.put("side", dto.getSide());
        params.put("type", dto.getType());
        params.put("quantity", dto.getQuantity());
        params.put("price", dto.getPrice());
        params.put("timeInForce", "GTC");

        String response = client.createTrade().newOrder(params);
        System.out.println("Testnet Order placed: " + response);
    }

    @Override
    public void cancelOrder(CancelOrderDto dto) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", dto.getSymbol());
        params.put("orderId", dto.getOrderId());

        String response = client.createTrade().cancelOrder(params);
        System.out.println("Testnet Order cancelled: " + response);
    }
}
