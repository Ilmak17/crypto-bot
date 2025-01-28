package com.trading.bot.api;

import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.api.mapper.OrderBookDtoMapper;

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

    @Override
    public Double getPrice() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BINANCE_BASE_URL))
                    .GET()
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = processRequest(request);

            return response.statusCode() == 200
                    ? Double.parseDouble(parsePriceFromJson(response.body()))
                    : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch price", e);
        }
    }

    @Override
    public OrderBookDto getOrderBook(String symbol, int limit) {
        String url = String.format("%s/api/v3/depth?symbol=%s&limit=%d", BINANCE_BASE_URL, symbol, limit);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return new OrderBookDtoMapper().toOrderBookDto(response.body());
            } else {
                throw new RuntimeException("Error: Response Code " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch order book", e);
        }
    }

    private HttpResponse<String> processRequest(HttpRequest request) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String parsePriceFromJson(String json) {
        String priceKey = "\"price\":\"";
        int startIndex = json.indexOf(priceKey) + priceKey.length();
        int endIndex = json.indexOf("\"", startIndex);

        return json.substring(startIndex, endIndex);
    }
}
