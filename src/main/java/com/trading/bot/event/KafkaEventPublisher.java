package com.trading.bot.event;

import com.trading.bot.model.enums.OrderEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaEventPublisher {
    private final KafkaProducer<String, String> producer;
    private final String topic;

    public KafkaEventPublisher(String topic) {
        this.topic = topic;

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }

    public void publish(OrderEvent key, String message) {
       producer.send(new ProducerRecord<>(topic, key.toString(), message));
    }

    public void close() {
        producer.close();
    }
}
