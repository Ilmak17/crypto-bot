package com.trading.bot.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BinanceApiClientBean implements BinanceApiClient {
    private static final String BINANCE_BASE_URL = "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT";
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
