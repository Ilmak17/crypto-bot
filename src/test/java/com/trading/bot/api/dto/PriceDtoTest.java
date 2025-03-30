package com.trading.bot.api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSerialization() throws JsonProcessingException {
        PriceDto dto = new PriceDto("BTCUSDT", 29500.50);

        String json = objectMapper.writeValueAsString(dto);

        assertTrue(json.contains("\"symbol\":\"BTCUSDT\""));
        assertTrue(json.contains("\"price\":29500.5"));
    }

    @Test
    void testDeserialization() throws JsonProcessingException {
        String json = """
                {
                  "symbol": "ETHUSDT",
                  "price": 1820.75
                }
                """;

        PriceDto dto = objectMapper.readValue(json, PriceDto.class);

        assertEquals("ETHUSDT", dto.getSymbol());
        assertEquals(1820.75, dto.getPrice());
    }
}