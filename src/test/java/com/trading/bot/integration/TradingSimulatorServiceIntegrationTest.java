package com.trading.bot.integration;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.model.enums.Symbol;
import com.trading.bot.service.TradingSimulatorServiceBean;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;

import static com.trading.bot.model.enums.Topic.PRICE_UPDATES;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TradingSimulatorServiceIntegrationTest extends IntegrationSpec{

    @Container
    public KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    private KafkaEventPublisher publisher;
    private TradingSimulatorServiceBean simulatorService;

    private static final String GROUP_ID = "trading-simulator-test";

    @BeforeAll
    void setup() {
        String bootstrapServers = kafka.getBootstrapServers();
        publisher = new KafkaEventPublisher(bootstrapServers);
        consumer = createConsumer(bootstrapServers, GROUP_ID);
        consumer.subscribe(Collections.singleton(PRICE_UPDATES.getTopicName()));

        BinanceApiClient apiClient = mock(BinanceApiClient.class);
        when(apiClient.getPrice(Symbol.BTCUSDT)).thenReturn(99999.99);

        simulatorService = new TradingSimulatorServiceBean(apiClient, publisher);
        simulatorService.start();
    }

    @AfterAll
    void tearDown() {
        simulatorService.stop();
        if (publisher != null) publisher.close();
        if (consumer != null) consumer.close();
    }

    @Test
    void testPricePublishingToKafka() {
        boolean received = false;
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 6000) {
            var records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                if (record.value().contains("BTCUSDT: 99999.99")) {
                    received = true;
                    break;
                }
            }
            if (received) break;
        }

        assertTrue(received, "Price update was not published to Kafka.");
    }
}