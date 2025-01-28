package com.trading.bot.api.mapper;

import com.trading.bot.api.dto.OrderBookDto;

import java.util.ArrayList;
import java.util.List;

public class OrderBookDtoMapper {

    public OrderBookDto toOrderBookDto(String json) {
        long lastUpdateId = Long.parseLong(extractValue(json, "\"lastUpdateId\":", ","));

        OrderBookDto orderBook = new OrderBookDto();
        orderBook.setLastUpdateId(lastUpdateId);

        String bidsArray = extractArray(json, "\"bids\":");
        String asksArray = extractArray(json, "\"asks\":");

        List<OrderBookDto.OrderEntry> bids = parseOrderEntries(bidsArray);
        orderBook.setBids(bids);

        List<OrderBookDto.OrderEntry> asks = parseOrderEntries(asksArray);
        orderBook.setAsks(asks);

        return orderBook;
    }

    private String extractValue(String json, String key, String delimiter) {
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) {
            throw new IllegalArgumentException("Key not found: " + key);
        }
        int startIndex = keyIndex + key.length();
        int endIndex = json.indexOf(delimiter, startIndex);

        return json.substring(startIndex, endIndex).trim();
    }

    private String extractArray(String json, String key) {
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) {
            throw new IllegalArgumentException("Key not found: " + key);
        }
        int startIndex = json.indexOf("[", keyIndex);
        int endIndex = json.indexOf("]", startIndex);
        return json.substring(startIndex + 1, endIndex).trim();
    }

    private List<OrderBookDto.OrderEntry> parseOrderEntries(String arrayString) {
        List<OrderBookDto.OrderEntry> entries = new ArrayList<>();
        String[] elements = arrayString.split("],\\[");

        for (String element : elements) {
            element = element.replace("[", "").replace("]", "").replace("\"", "");
            String[] parts = element.split(",");

            double price = Double.parseDouble(parts[0]);
            double quantity = Double.parseDouble(parts[1]);
            entries.add(new OrderBookDto.OrderEntry(price, quantity));
        }
        return entries;
    }
}
