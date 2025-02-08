package com.trading.bot.api.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.bot.api.dto.OrderBookDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

public class OrderBookDtoMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(OrderBookDtoMapper.class);

    public OrderBookDto toOrderBookDto(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            long lastUpdateId = root.get("lastUpdateId").asLong();

            List<OrderBookDto.OrderEntry> bids = parseOrderEntries(root.get("bids"));
            List<OrderBookDto.OrderEntry> asks = parseOrderEntries(root.get("asks"));

            return new OrderBookDto(lastUpdateId, bids, asks);
        } catch (IOException e) {
            logger.error("Error parsing JSON into OrderBookDto", e);
            throw new RuntimeException(e);
        }
    }

    private List<OrderBookDto.OrderEntry> parseOrderEntries(JsonNode entriesNode) {
        List<OrderBookDto.OrderEntry> entries = new ArrayList<>();
        if (isNull(entriesNode) || !entriesNode.isArray()) {
            return entries;
        }

        for (JsonNode entry : entriesNode) {
            double price = entry.get(0).asDouble();
            double quantity = entry.get(1).asDouble();
            entries.add(new OrderBookDto.OrderEntry(price, quantity));
        }
        return entries;
    }
}
