package com.trading.bot.api;

import com.trading.bot.api.dto.CancelOrderDto;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.api.dto.PlaceOrderDto;
import com.trading.bot.api.mapper.OrderBookDtoMapper;
import com.trading.bot.api.mapper.PriceDtoMapper;
import com.trading.bot.api.util.BinanceSignature;
import com.trading.bot.model.enums.Symbol;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BinanceApiClientBean implements BinanceApiClient {
    private static final String BINANCE_BASE_URL = "https://api.binance.com";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();
    private String secretKey = "test";
    private String apiKey = "test";

    @Override
    public Double getPrice() {
        try {
            String url = String.format("%s/api/v3/ticker/price?symbol=BTCUSDT", BINANCE_BASE_URL);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = processRequest(request);

            if (response.statusCode() == 200) {
                return new PriceDtoMapper().toPriceDto(response.body()).getPrice();
            } else {
                throw new RuntimeException("Error: Response Code " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch price", e);
        }
    }

    @Override
    public OrderBookDto getOrderBook(Symbol market, int limit) {
        String url = String.format("%s/api/v3/depth?symbol=%s&limit=%d", BINANCE_BASE_URL, market, limit);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = processRequest(request);
            if (response.statusCode() == 200) {
                return new OrderBookDtoMapper().toOrderBookDto(response.body());
            } else {
                throw new RuntimeException("Error: Response Code " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch order book", e);
        }
    }

    @Override
    public void placeOrder(PlaceOrderDto dto) {
        try {
            long timestamp = System.currentTimeMillis();
            String query = String.format(
                    "symbol=%s&side=%s&type=%s&quantity=%.8f&price=%.2f&timeInForce=GTC&timestamp=%d",
                    dto.getSymbol(), dto.getSide(), dto.getType(), dto.getQuantity(), dto.getPrice(), timestamp
            );

            String signature = BinanceSignature.generateSignature(query, secretKey);
            String url = String.format("%s/api/v3/order?%s&signature=%s", BINANCE_BASE_URL, query, signature);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .header("X-MBX-APIKEY", apiKey)
                    .build();

            HttpResponse<String> response = processRequest(request);
            System.out.println("Order Response: " + response.body());
        } catch (Exception e) {
            throw new RuntimeException("Failed to place order", e);
        }
    }

    @Override
    public void cancelOrder(CancelOrderDto dto) {
        try {
            long timestamp = System.currentTimeMillis();
            String query = String.format("symbol=%s&orderId=%s&timestamp=%d", dto.getSymbol(),
                    dto.getOrderId(), timestamp);

            String signature = BinanceSignature.generateSignature(query, secretKey);
            String url = String.format("%s/api/v3/order?%s&signature=%s", BINANCE_BASE_URL, query, signature);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .header("X-MBX-APIKEY", apiKey)
                    .build();

            HttpResponse<String> response = processRequest(request);
            System.out.println("Cancel Order Response: " + response.body());
        } catch (Exception e) {
            throw new RuntimeException("Failed to cancel order", e);
        }
    }

    private HttpResponse<String> processRequest(HttpRequest request) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
