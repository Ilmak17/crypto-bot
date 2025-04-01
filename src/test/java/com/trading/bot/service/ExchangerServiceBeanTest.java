package com.trading.bot.service;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.api.dto.OrderBookDto;
import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderSourceType;
import com.trading.bot.model.enums.OrderStatus;
import com.trading.bot.model.enums.OrderType;
import com.trading.bot.model.enums.Symbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class ExchangerServiceBeanTest {

    private KafkaEventPublisher kafkaEventPublisher;
    private ExchangerServiceBean exchangerService;

    @BeforeEach
    void setUp() {
        BinanceApiClient binanceApiClient = mock(BinanceApiClient.class);
        kafkaEventPublisher = mock(KafkaEventPublisher.class);

        OrderBookDto mockOrderBook = new OrderBookDto(
                1L,
                List.of(new OrderBookDto.OrderEntry(20000, 0.5)),
                List.of(new OrderBookDto.OrderEntry(20100, 0.5))
        );

        when(binanceApiClient.getOrderBook(Symbol.BTCUSDT, 10)).thenReturn(mockOrderBook);
        when(binanceApiClient.getPrice(Symbol.BTCUSDT)).thenReturn(20050.0);

        exchangerService = new ExchangerServiceBean(Symbol.BTCUSDT, binanceApiClient, kafkaEventPublisher);
    }

    @Test
    void testPlaceBuyOrder() {
        Order order = new Order(
                1L,
                OrderType.BUY,
                0.2,
                20200.0,
                OrderStatus.NEW,
                OrderSourceType.BOT
        );

        exchangerService.placeOrder(order);

        verify(kafkaEventPublisher, atLeastOnce()).publish(any(), contains("New order placed"));
    }

    @Test
    void testPlaceMatchingOrders() {
        Order buyOrder = new Order(
                1L,
                OrderType.BUY,
                0.5,
                21000.0,
                OrderStatus.NEW,
                OrderSourceType.BOT
        );

        Order sellOrder = new Order(
                2L,
                OrderType.SELL,
                0.5,
                20000.0,
                OrderStatus.NEW,
                OrderSourceType.BOT
        );

        exchangerService.placeOrder(buyOrder);
        exchangerService.placeOrder(sellOrder);

        verify(kafkaEventPublisher, atLeastOnce()).publish(any(), contains("Matched: BUY"));
    }

    @Test
    void testCancelOrder() {
        Order order = new Order(
                3L,
                OrderType.BUY,
                0.1,
                19900.0,
                OrderStatus.NEW,
                OrderSourceType.BOT
        );

        exchangerService.placeOrder(order);

        boolean result = exchangerService.cancelOrder(3L);

        verify(kafkaEventPublisher).publish(any(), contains("Order cancelled"));
        assert(result);
    }
}