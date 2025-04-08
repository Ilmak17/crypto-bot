package com.trading.bot.integration;

import com.trading.bot.event.KafkaEventPublisher;
import com.trading.bot.model.enums.Topic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaIntegrationTest extends IntegrationSpec {

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

        KafkaConsumer<String, String> consumer = createConsumer(kafka.getBootstrapServers(), "test-group");
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
}
