package com.trading.bot.api.mapper;

import com.trading.bot.api.dto.PriceDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PriceDtoMapperTest {

    private final PriceDtoMapper mapper = new PriceDtoMapper();

    @Test
    void testToPriceDtoValidJson() {
        String json = """
                {
                  "symbol": "BTCUSDT",
                  "price": 28999.55
                }
                """;

        PriceDto dto = mapper.toPriceDto(json);

        assertEquals("BTCUSDT", dto.getSymbol());
        assertEquals(28999.55, dto.getPrice());
    }

    @Test
    void testToPriceDtoMissingField() {
        String json = """
                {
                  "symbol": "BTCUSDT"
                }
                """;

        PriceDto dto = mapper.toPriceDto(json);

        assertEquals("BTCUSDT", dto.getSymbol());
        assertNull(dto.getPrice());
    }
}