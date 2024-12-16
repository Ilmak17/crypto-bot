package com.trading.bot.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BinanceApiClientBean implements BinanceApiClient {
    private static final String BINANCE_BASE_URL = "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT";

    @Override
    public String getPrice() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BINANCE_BASE_URL))
                    .GET()
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new RuntimeException("Error: Response Code " + response.statusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch price", e);
        }
    }
}
