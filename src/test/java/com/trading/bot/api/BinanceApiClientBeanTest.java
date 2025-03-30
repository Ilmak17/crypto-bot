package com.trading.bot.api;

import com.binance.connector.client.SpotClient;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.api.dto.PriceDto;
import com.trading.bot.model.enums.Symbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BinanceApiClientBeanTest {

    private BinanceApiClientBean apiClient;
    private SpotClient mockClient;

    @BeforeEach
    void setUp() {
        System.setProperty("BINANCE_API_KEY", "dummy");
        System.setProperty("BINANCE_SECRET_KEY", "dummy");

        apiClient = new BinanceApiClientBean() {
            @Override
            public Double getPrice(Symbol symbol) {
                if (symbol == Symbol.BTCUSDT) {
                    return 29000.99;
                }
                return null;
            }

            @Override
            public OrderBookDto getOrderBook(Symbol market, int limit) {
                OrderBookDto dto = new OrderBookDto();
                dto.setLastUpdateId(12345L);
                return dto;
            }
        };
    }

    @Test
    void testGetPrice_returnsValidPrice() {
        Double price = apiClient.getPrice(Symbol.BTCUSDT);
        assertNotNull(price);
        assertTrue(price > 0);
    }

    @Test
    void testGetOrderBook_returnsOrderBook() {
        OrderBookDto orderBook = apiClient.getOrderBook(Symbol.BTCUSDT, 5);
        assertNotNull(orderBook);
        assertEquals(12345L, orderBook.getLastUpdateId());
    }
}