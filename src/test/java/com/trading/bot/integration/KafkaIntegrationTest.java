package com.trading.bot.integration;

import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.model.enums.Topic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaIntegrationTest {

    @Container
    public KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    private KafkaEventPublisher publisher;

    @BeforeAll
    void setup() {
        kafka.start();

        System.setProperty("KAFKA_BOOTSTRAP_SERVERS", kafka.getBootstrapServers());
        publisher = new KafkaEventPublisher(kafka.getBootstrapServers());
    }

    @AfterAll
    void tearDown() {
        publisher.close();
    }

    @Test
    void testPublishAndConsume() {
        String testMessage = "Integration Test Message";
        String topic = Topic.ORDER_EVENTS.getTopicName();

        publisher.publish(Topic.ORDER_EVENTS, testMessage);

        KafkaConsumer<String, String> consumer = getStringStringKafkaConsumer();
        consumer.subscribe(Collections.singleton(topic));

        ConsumerRecord<String, String> received = null;
        long start = System.currentTimeMillis();

        while ((System.currentTimeMillis() - start) < 5000) {
            var records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                received = record;
                break;
            }
            if (received != null) break;
        }

        assertNotNull(received, "Message was not received");
        assertEquals(testMessage, received.value());
        consumer.close();
    }

    private @NotNull KafkaConsumer<String, String> getStringStringKafkaConsumer() {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new KafkaConsumer<>(consumerProps);
    }
}
