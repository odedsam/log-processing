package com.example.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Kafka configuration utility class for Kraft mode (broker-only, no Zookeeper)
 */
public class KafkaConfig {

    // Kafka cluster configuration (adjust these values based on your setup)
    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String LOG_TOPIC = "application-logs";
    public static final String CONSUMER_GROUP_ID = "log-processing-group";

    /**
     * creates producer configuration for Kraft mode Kafka cluster
     */
    public static Properties createProducerConfig() {
        Properties props = new Properties();

        // bootstrap servers (Kraft mode - no Zookeeper needed)
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        // serialization
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                 "com.example.kafka.serialization.JsonSerializer");

        // producer performance and reliability settings
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // wait for all replicas
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        // enable idempotence to avoid duplicate messages
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // compression
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        return props;
    }

    /**
     * creates consumer configuration for Kraft mode Kafka cluster
     */
    public static Properties createConsumerConfig() {
        Properties props = new Properties();

        // bootstrap servers (Kraft mode)
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        // consumer group
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);

        // deserialization
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                 "com.example.kafka.serialization.JsonDeserializer");

        // consumer behavior
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // manual commit for better control

        // consumer performance settings
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);

        return props;
    }
}
