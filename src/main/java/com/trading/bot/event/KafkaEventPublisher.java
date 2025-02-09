package com.trading.bot.event;

import com.trading.bot.model.enums.OrderEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaEventPublisher {
//    private final KafkaProducer<String, String> producer;
//    private final String topic;

    public KafkaEventPublisher(String topic) {
//        this.topic = topic;
//
//        Properties props = new Properties();
//        props.put("bootstrap.servers", "localhost:9092");
//        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        producer = new KafkaProducer<>(props);
    }

    public void publish(OrderEvent key, String message) {
//        producer.send(new ProducerRecord<>(topic, key, message));
    }

    public void close() {
//        producer.close();
    }
}
