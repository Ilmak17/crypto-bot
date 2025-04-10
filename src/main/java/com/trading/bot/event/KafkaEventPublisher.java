package com.trading.bot.event;

import com.trading.bot.model.enums.Topic;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaEventPublisher {
    private final KafkaProducer<String, String> producer;
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private static final String DEAD_LETTER_TOPIC = "dead-letter-topic";


    public KafkaEventPublisher(String bootstrapServers) {
        producer = getStringStringKafkaProducer(bootstrapServers);
    }

    public KafkaEventPublisher() {
        Dotenv dotenv = Dotenv.load();
        String bootstrapServers = dotenv.get("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        producer = getStringStringKafkaProducer(bootstrapServers);
    }

    @NotNull
    private KafkaProducer<String, String> getStringStringKafkaProducer(String bootstrapServers) {
        Properties props = new Properties();

        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<>(props);
    }

    public void publish(Topic topic, String message) {
        try {
            producer.send(new ProducerRecord<>(topic.getTopicName(), message));
        } catch (Exception e) {
            logger.error("Failed to send message to topic {}. Sending to DLQ.", topic.getTopicName(), e);
            producer.send(new ProducerRecord<>(DEAD_LETTER_TOPIC, message));
        }
    }

    public void close() {
        producer.flush();
        producer.close();
    }
}
