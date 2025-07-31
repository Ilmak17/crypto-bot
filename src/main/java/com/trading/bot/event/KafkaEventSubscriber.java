package com.trading.bot.event;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.function.Consumer;

public class KafkaEventSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventSubscriber.class);

    private final KafkaConsumer<String, String> consumer;
    private final Consumer<String> eventHandler;

    private static final String GROUP = "trading-bot-group";
    private static final String KAFKA_BOOTSTRAP_SERVERS = "KAFKA_BOOTSTRAP_SERVERS";

    public KafkaEventSubscriber(String topic, Consumer<String> eventHandler) {
        this.eventHandler = eventHandler;

        Dotenv dotenv = Dotenv.load();
        String bootstrapServers = dotenv.get(KAFKA_BOOTSTRAP_SERVERS, "localhost:9092");

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(topic));
    }

    public void listen() {
        new Thread(() -> {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        logger.info("Received Kafka Event: {}", record.value());
                        eventHandler.accept(record.value());
                    } catch (Exception e) {
                        logger.error("Error while handling Kafka event: {}", record.value(), e);
                    }
                }
                consumer.commitSync();
            }
        }).start();
    }
}
