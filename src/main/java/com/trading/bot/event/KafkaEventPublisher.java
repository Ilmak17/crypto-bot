package com.trading.bot.event;

import com.trading.bot.model.enums.Topic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaEventPublisher {
    private final KafkaProducer<String, String> producer;

    public KafkaEventPublisher() {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }

    public void publish(Topic topic, String message) {
        producer.send(new ProducerRecord<>(topic.toString(), message));
        producer.flush();
    }

    public void close() {
        producer.close();
    }
}
