package com.trading.bot.integration;

import com.trading.bot.api.BinanceApiClient;
import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.model.Order;
import com.trading.bot.model.enums.OrderSourceType;
import com.trading.bot.model.enums.OrderStatus;
import com.trading.bot.model.enums.OrderType;
import com.trading.bot.model.enums.Symbol;
import com.trading.bot.service.ExchangerServiceBean;
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
import java.util.UUID;

import static com.trading.bot.model.enums.Topic.ORDER_EVENTS;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExchangerServiceIntegrationTest {
    @Container
    public KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    private KafkaEventPublisher publisher;
    private KafkaConsumer<String, String> consumer;

    private ExchangerServiceBean exchangerService;

    @BeforeAll
    void setup() {
        kafka.start();

        String bootstrapServers = kafka.getBootstrapServers();
        publisher = new KafkaEventPublisher(bootstrapServers);
        consumer = createConsumer(bootstrapServers);
        consumer.subscribe(Collections.singleton(ORDER_EVENTS.getTopicName()));

        BinanceApiClient mockApiClient = mock(BinanceApiClient.class);
        when(mockApiClient.getPrice(Symbol.BTCUSDT)).thenReturn(82000.0);

        exchangerService = new ExchangerServiceBean(
                Symbol.BTCUSDT,
                mockApiClient,
                publisher
        );
    }

    @AfterAll
    void tearDown() {
        if (publisher != null) publisher.close();
        if (consumer != null) consumer.close();
        exchangerService.stopAutoUpdate();
    }

    @Test
    void testOrderMatchingViaKafka() {
        waitForTopicReady(ORDER_EVENTS.getTopicName(), consumer);

        Order buyOrder = new Order(
                UUID.randomUUID().getMostSignificantBits(),
                OrderType.BUY, 0.01, 81000.0,
                OrderStatus.NEW, OrderSourceType.BOT
        );

        Order sellOrder = new Order(
                UUID.randomUUID().getMostSignificantBits(),
                OrderType.SELL, 0.01, 83000.0,
                OrderStatus.NEW, OrderSourceType.BOT
        );

        exchangerService.placeOrder(buyOrder);
        exchangerService.placeOrder(sellOrder);

        boolean matched = false;
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 5000) {
            var records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                if (record.value().contains("Matched")) {
                    matched = true;
                    break;
                }
            }
            if (matched) break;
        }

        assertTrue(matched, "Orders were not matched via Kafka.");
    }

    private void waitForTopicReady(String topic, KafkaConsumer<String, String> consumer) {
        long start = System.currentTimeMillis();
        boolean found = false;

        while (!found && System.currentTimeMillis() - start < 5000) {
            try {
                consumer.partitionsFor(topic);
                found = true;
            } catch (Exception ignored) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private KafkaConsumer<String, String> createConsumer(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "exchange-integration-test");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new KafkaConsumer<>(props);
    }
}