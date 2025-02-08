package com.trading.bot.api;

import com.trading.bot.api.dto.CancelOrderDto;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.api.dto.PlaceOrderDto;
import com.trading.bot.api.mapper.OrderBookDtoMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class BinanceApiClientBean implements BinanceApiClient {
    private static final String BINANCE_BASE_URL = "https://api.binance.com";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();

    private String secretKey = "";
    private String apiKey = "";

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

    @Override
    public void placeOrder(PlaceOrderDto dto) {
        try {
            long timestamp = System.currentTimeMillis();
            String query = String.format(
                    "symbol=%s&side=%s&type=%s&quantity=%.8f&price=%.2f&timeInForce=GTC&timestamp=%d",
                    dto.getSymbol(), dto.getSide(), dto.getType(), dto.getQuantity(), dto.getPrice(), timestamp
            );

            String signature = generateSignature(query, secretKey);
            String url = String.format("%s/api/v3/order?%s&signature=%s", BINANCE_BASE_URL, query, signature);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .header("X-MBX-APIKEY", apiKey)
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

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

            String signature = generateSignature(query, secretKey);
            String url = String.format("%s/api/v3/order?%s&signature=%s", BINANCE_BASE_URL, query, signature);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .header("X-MBX-APIKEY", apiKey)
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Cancel Order Response: " + response.body());
        } catch (Exception e) {
            throw new RuntimeException("Failed to cancel order", e);
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

    private static String generateSignature(String data, String secretKey) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKeySpec);
            byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC-SHA256 signature", e);
        }
    }
}
