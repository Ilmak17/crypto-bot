package com.trading.bot.integration;

import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.model.Order;
import com.trading.bot.model.enums.*;
import com.trading.bot.service.ExchangerServiceBean;
import com.trading.bot.api.BinanceApiClient;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExchangerServiceKafkaIntegrationTest {

    @Container
    private final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    private ExchangerServiceBean exchangerService;
    private KafkaEventPublisher publisher;
    private KafkaConsumer<String, String> consumer;

    @BeforeAll
    void setUp() {
        kafka.start();
        String bootstrapServers = kafka.getBootstrapServers();
        System.setProperty("KAFKA_BOOTSTRAP_SERVERS", bootstrapServers);

        BinanceApiClient apiClient = mock(BinanceApiClient.class);
        when(apiClient.getPrice(Symbol.BTCUSDT)).thenReturn(20000.0);

        publisher = new KafkaEventPublisher(bootstrapServers);
        exchangerService = new ExchangerServiceBean(Symbol.BTCUSDT, apiClient, publisher);

        consumer = createKafkaConsumer(bootstrapServers);
        consumer.subscribe(Collections.singleton(Topic.ORDER_EVENTS.getTopicName()));
    }

    @AfterAll
    void tearDown() {
        publisher.close();
        consumer.close();
        kafka.stop();
    }

    @Test
    void testOrderMatchingViaKafka() throws InterruptedException {
        Order buyOrder = new Order(1L, OrderType.BUY, 0.5, 21000.0, OrderStatus.NEW, OrderSourceType.BOT);
        Order sellOrder = new Order(2L, OrderType.SELL, 0.5, 19000.0, OrderStatus.NEW, OrderSourceType.BOT);

        publisher.publish(Topic.ORDER_EVENTS, buyOrder.toString());
        Thread.sleep(100); // ждём Kafka consumer
        publisher.publish(Topic.ORDER_EVENTS, sellOrder.toString());

        boolean matched = false;
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 5000) {
            var records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                if (record.value().contains("Matched: BUY")) {
                    matched = true;
                    break;
                }
            }
            if (matched) break;
        }

        assertTrue(matched, "Orders were not matched via Kafka.");
    }

    private KafkaConsumer<String, String> createKafkaConsumer(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "exchange-integration-test");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        return new KafkaConsumer<>(props);
    }
}