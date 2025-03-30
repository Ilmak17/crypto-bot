package com.trading.bot.api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBookDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSerialization() throws JsonProcessingException {
        OrderBookDto.OrderEntry bid = new OrderBookDto.OrderEntry(25000.0, 1.5);
        OrderBookDto.OrderEntry ask = new OrderBookDto.OrderEntry(26000.0, 2.0);
        OrderBookDto orderBook = new OrderBookDto(123456L,
                Collections.singletonList(bid),
                Collections.singletonList(ask));

        String json = objectMapper.writeValueAsString(orderBook);

        assertTrue(json.contains("\"lastUpdateId\":123456"));
        assertTrue(json.contains("\"price\":25000.0"));
        assertTrue(json.contains("\"quantity\":1.5"));
    }

    @Test
    void testDeserialization() throws JsonProcessingException {
        String json = """
                {
                  "lastUpdateId": 789012,
                  "bids": [
                    {"price": 27000.0, "quantity": 1.0}
                  ],
                  "asks": [
                    {"price": 27500.0, "quantity": 1.2}
                  ]
                }
                """;

        OrderBookDto dto = objectMapper.readValue(json, OrderBookDto.class);

        assertEquals(789012L, dto.getLastUpdateId());
        assertEquals(1, dto.getBids().size());
        assertEquals(27000.0, dto.getBids().get(0).getPrice());
        assertEquals(1.0, dto.getBids().get(0).getQuantity());

        assertEquals(1, dto.getAsks().size());
        assertEquals(27500.0, dto.getAsks().get(0).getPrice());
        assertEquals(1.2, dto.getAsks().get(0).getQuantity());
    }
}