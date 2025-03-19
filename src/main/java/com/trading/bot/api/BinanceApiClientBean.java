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
        try {
            Map<String, Object> params = createSymbolParams(Symbol.BTCUSDT);
            String response = client.createMarket().tickerSymbol(params);
            Double price = Double.parseDouble(response);
            logger.info("Fetched BTC price: {}", price);
            return price;
        } catch (Exception e) {
            logger.error("Failed to fetch BTC price", e);
            return null;
        }
    }

    @Override
    public OrderBookDto getOrderBook(Symbol market, int limit) {
        try {
            LinkedHashMap<String, Object> params = createOrderBookParams(market, limit);
            String response = client.createMarket().depth(params);
            OrderBookDto orderBook = new OrderBookDtoMapper().toOrderBookDto(response);
            logger.info("Fetched order book for {}: {}", market, orderBook);
            return orderBook;
        } catch (Exception e) {
            logger.error("Failed to fetch order book for {}", market, e);
            return null;
        }
    }

    @Override
    public void placeOrder(PlaceOrderDto dto) {
        try {
            LinkedHashMap<String, Object> params = createOrderParams(dto);
            String response = client.createTrade().newOrder(params);
            logger.info("Order placed: {}", response);
        } catch (Exception e) {
            logger.error("Failed to place order: {}", dto, e);
        }
    }

    @Override
    public void cancelOrder(CancelOrderDto dto) {
        try {
            LinkedHashMap<String, Object> params = createCancelOrderParams(dto);
            String response = client.createTrade().cancelOrder(params);
            logger.info("Order cancelled: {}", response);
        } catch (Exception e) {
            logger.error("Failed to cancel order: {}", dto, e);
        }
    }

    private Map<String, Object> createSymbolParams(Symbol symbol) {
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", symbol.toString());

        return params;
    }

    private LinkedHashMap<String, Object> createOrderBookParams(Symbol market, int limit) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", market.toString());
        params.put("limit", limit);

        return params;
    }

    private LinkedHashMap<String, Object> createOrderParams(PlaceOrderDto dto) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", dto.getSymbol());
        params.put("side", dto.getSide());
        params.put("type", dto.getType());
        params.put("quantity", dto.getQuantity());
        params.put("price", dto.getPrice());
        params.put("timeInForce", "GTC");

        return params;
    }

    private LinkedHashMap<String, Object> createCancelOrderParams(CancelOrderDto dto) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", dto.getSymbol());
        params.put("orderId", dto.getOrderId());

        return params;
    }
}
