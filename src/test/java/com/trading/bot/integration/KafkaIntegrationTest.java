package com.trading.bot.integration;

import com.trading.bot.event.KafkaEventPublisher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaIntegrationTest {
    @Container
    public KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.5.0");

    private KafkaEventPublisher publisher;

    @BeforeAll
    void setup() {
        System.setProperty("KAFKA_BOOTSTRAP_SERVERS", kafka.getBootstrapServers());
        publisher = new KafkaEventPublisher(kafka.getBootstrapServers());
    }

    @AfterAll
    void tearDown() {
        publisher.close();
    }

}
