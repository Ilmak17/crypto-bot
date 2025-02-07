package com.trading.bot.event;

import java.time.Duration;
import java.util.Properties;

public class KafkaEventSubscriber {
    private final KafkaConsumer<String, String> consumer;

    public KafkaEventSubscriber(String topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "order-events-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
    }

    public void consume() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("Consumed message: key=%s, value=%s%n", record.key(), record.value());
            }
        }
    }
}
