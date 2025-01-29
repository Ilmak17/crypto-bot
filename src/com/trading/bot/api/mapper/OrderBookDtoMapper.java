package com.trading.bot.api.mapper;

import com.trading.bot.api.dto.OrderBookDto;

import java.util.ArrayList;
import java.util.List;

public class OrderBookDtoMapper {

    public OrderBookDto toOrderBookDto(String json) {
        long lastUpdateId = Long.parseLong(extractValue(json));

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

    private String extractValue(String json) {
        int keyIndex = json.indexOf("\"lastUpdateId\":");
        int startIndex = keyIndex + "\"lastUpdateId\":".length();
        int endIndex = json.indexOf(",", startIndex);

        return json.substring(startIndex, endIndex).trim();
    }

    private String extractArray(String json, String key) {
        int keyIndex = json.indexOf(key);

        int startIndex = json.indexOf("[", keyIndex);
        int endIndex = json.indexOf("]", startIndex);

        return json.substring(startIndex + 1, endIndex).trim();
    }

    private List<OrderBookDto.OrderEntry> parseOrderEntries(String arrayString) {
        List<OrderBookDto.OrderEntry> entries = new ArrayList<>();

        if (arrayString.isEmpty()) {
            return entries;
        }

        arrayString = arrayString.trim().replace("\"", "");

        String[] elements = arrayString.split("\\],\\[");

        for (String element : elements) {
            element = element.replace("[", "").replace("]", "");
            OrderBookDto.OrderEntry entry = parseOrderEntry(element);
            entries.add(entry);
        }

        return entries;
    }

    private OrderBookDto.OrderEntry parseOrderEntry(String element) {
        String[] parts = element.split(",");

        double price = Double.parseDouble(parts[0]);
        double quantity = Double.parseDouble(parts[1]);
        return new OrderBookDto.OrderEntry(price, quantity);
    }
}
