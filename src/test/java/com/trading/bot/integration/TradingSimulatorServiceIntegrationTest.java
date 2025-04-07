package com.trading.bot.integration;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.model.enums.Symbol;
import com.trading.bot.service.TradingSimulatorServiceBean;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static com.trading.bot.model.enums.Topic.PRICE_UPDATES;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TradingSimulatorServiceIntegrationTest {

    @Container
    public KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    private KafkaEventPublisher publisher;
    private KafkaConsumer<String, String> consumer;
    private TradingSimulatorServiceBean simulatorService;

    @BeforeAll
    void setup() {
        String bootstrapServers = kafka.getBootstrapServers();
        publisher = new KafkaEventPublisher(bootstrapServers);
        consumer = createConsumer(bootstrapServers);
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
    void testPricePublishingToKafka() throws InterruptedException {
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

    private KafkaConsumer<String, String> createConsumer(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "trading-simulator-test");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(props);
    }
}