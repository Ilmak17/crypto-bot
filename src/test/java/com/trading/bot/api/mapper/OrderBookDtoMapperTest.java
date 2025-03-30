package com.trading.bot.api.mapper;

import com.trading.bot.api.dto.OrderBookDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookDtoMapperTest {

    private final OrderBookDtoMapper mapper = new OrderBookDtoMapper();

    @Test
    void testToOrderBookDto_validJson() {
        String json = """
                {
                  "lastUpdateId": 123456789,
                  "bids": [
                    ["29500.12", "0.5"],
                    ["29450.00", "1.2"]
                  ],
                  "asks": [
                    ["29600.00", "0.3"],
                    ["29650.00", "2.0"]
                  ]
                }
                """;

        OrderBookDto dto = mapper.toOrderBookDto(json);

        assertEquals(123456789L, dto.getLastUpdateId());

        List<OrderBookDto.OrderEntry> bids = dto.getBids();
        assertEquals(2, bids.size());
        assertEquals(29500.12, bids.get(0).getPrice());
        assertEquals(0.5, bids.get(0).getQuantity());

        List<OrderBookDto.OrderEntry> asks = dto.getAsks();
        assertEquals(2, asks.size());
        assertEquals(29600.00, asks.get(0).getPrice());
        assertEquals(0.3, asks.get(0).getQuantity());
    }

    @Test
    void testToOrderBookDto_emptyBidsAsks() {
        String json = """
                {
                  "lastUpdateId": 42,
                  "bids": [],
                  "asks": []
                }
                """;

        OrderBookDto dto = mapper.toOrderBookDto(json);

        assertEquals(42L, dto.getLastUpdateId());
        assertTrue(dto.getBids().isEmpty());
        assertTrue(dto.getAsks().isEmpty());
    }
}