package com.trading.bot.service;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.model.enums.Symbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static com.trading.bot.model.enums.Topic.PRICE_UPDATES;
import static org.mockito.Mockito.*;

class TradingSimulatorServiceBeanTest {

    private BinanceApiClient binanceApiClient;
    private KafkaEventPublisher kafkaEventPublisher;
    private TradingSimulatorServiceBean tradingService;

    @BeforeEach
    void setUp() {
        binanceApiClient = mock(BinanceApiClient.class);
        kafkaEventPublisher = mock(KafkaEventPublisher.class);

        tradingService = new TradingSimulatorServiceBean(binanceApiClient, kafkaEventPublisher);
    }

    @Test
    void testStartPublishesPrice() throws InterruptedException {
        when(binanceApiClient.getPrice(Symbol.BTCUSDT)).thenReturn(42000.0);

        tradingService.start();

        TimeUnit.SECONDS.sleep(6);

        verify(binanceApiClient, atLeastOnce()).getPrice(Symbol.BTCUSDT);
        verify(kafkaEventPublisher, atLeastOnce())
                .publish(eq(PRICE_UPDATES), contains("BTCUSDT: 42000.0"));

        tradingService.stop();
    }
}