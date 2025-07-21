package com.example.kafka.producer;

import com.example.kafka.config.KafkaConfig;
import com.example.kafka.model.LogEntry;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * LogProducer sends LogEntry objects to a Kafka topic
 */
public class LogProducer {

    private static final Logger logger = LoggerFactory.getLogger(LogProducer.class);
    private final KafkaProducer<String, LogEntry> producer;
    private final String topicName;

    public LogProducer() {
        this(KafkaConfig.LOG_TOPIC);
    }

    public LogProducer(String topicName) {
        this.topicName = topicName;
        Properties props = KafkaConfig.createProducerConfig();
        this.producer = new KafkaProducer<>(props);
        logger.info("LogProducer initialized for topic: {}", topicName);
    }

    /**
     * sends a log entry to Kafka asynchronously
     *
     * @param logEntry the log entry to send
     * @return Future<RecordMetadata> for async handling
     */
    public Future<RecordMetadata> sendLogEntry(LogEntry logEntry) {
        return sendLogEntry(null, logEntry);
    }

    /**
     * sends a log entry to Kafka with a specific key asynchronously
     *
     * @param key the message key (can be null)
     * @param logEntry the log entry to send
     * @return Future<RecordMetadata> for async handling
     */
    public Future<RecordMetadata> sendLogEntry(String key, LogEntry logEntry) {
        if (logEntry == null) {
            throw new IllegalArgumentException("LogEntry cannot be null");
        }

        ProducerRecord<String, LogEntry> record = new ProducerRecord<>(topicName, key, logEntry);

        return producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                logger.debug("Log entry sent successfully: topic={}, partition={}, offset={}",
                           metadata.topic(), metadata.partition(), metadata.offset());
            } else {
                logger.error("Failed to send log entry: {}", exception.getMessage(), exception);
            }
        });
    }

    /**
     * sends a log entry synchronously (blocks until completion)
     *
     * @param logEntry the log entry to send
     * @throws Exception if sending fails
     */
    public void sendLogEntrySync(LogEntry logEntry) throws Exception {
        sendLogEntry(logEntry).get();
    }

    /**
     * flushes any pending messages and closes the producer
     */
    public void close() {
        try {
            logger.info("Flushing and closing LogProducer...");
            producer.flush();
            producer.close();
            logger.info("LogProducer closed successfully");
        } catch (Exception e) {
            logger.error("Error closing LogProducer: {}", e.getMessage(), e);
        }
    }
}
