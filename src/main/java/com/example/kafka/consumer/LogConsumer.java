package com.example.kafka.consumer;

import com.example.kafka.config.KafkaConfig;
import com.example.kafka.model.LogEntry;
import com.example.kafka.serialization.JsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LogConsumer receives and processes LogEntry objects from a Kafka topic
 */
public class LogConsumer {

    private static final Logger logger = LoggerFactory.getLogger(LogConsumer.class);
    private final KafkaConsumer<String, LogEntry> consumer;
    private final String topicName;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final String errorLogDir;

    public LogConsumer() {
        this(KafkaConfig.LOG_TOPIC);
    }

    public LogConsumer(String topicName) {
        this.topicName = topicName;
        this.errorLogDir = "error-logs";

        // create error log directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(errorLogDir));
        } catch (IOException e) {
            logger.error("Failed to create error log directory: {}", e.getMessage(), e);
        }

        Properties props = KafkaConfig.createConsumerConfig();

        // create consumer with custom deserializer
        this.consumer = new KafkaConsumer<>(
            props,
            new org.apache.kafka.common.serialization.StringDeserializer(),
            new JsonDeserializer<>(LogEntry.class)
        );

        logger.info("LogConsumer initialized for topic: {}", topicName);
    }

    /**
     * starts consuming messages from the Kafka topic
     */
    public void startConsuming() {
        running.set(true);
        consumer.subscribe(Collections.singletonList(topicName));

        logger.info("Starting log consumption from topic: {}", topicName);

        try {
            while (running.get()) {
                ConsumerRecords<String, LogEntry> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, LogEntry> record : records) {
                    processLogEntry(record);
                }

                // manual commit after processing all records in the batch
                if (!records.isEmpty()) {
                    consumer.commitAsync((offsets, exception) -> {
                        if (exception != null) {
                            logger.error("Failed to commit offsets: {}", exception.getMessage(), exception);
                        } else {
                            logger.debug("Successfully committed offsets: {}", offsets);
                        }
                    });
                }
            }
        } catch (Exception e) {
            logger.error("Error while consuming messages: {}", e.getMessage(), e);
        } finally {
            try {
                consumer.commitSync(); // final synchronous commit
            } catch (Exception e) {
                logger.error("Error during final commit: {}", e.getMessage(), e);
            }
            consumer.close();
            logger.info("LogConsumer stopped and closed");
        }
    }

    /**
     * processes a single log entry record
     */
    private void processLogEntry(ConsumerRecord<String, LogEntry> record) {
        try {
            LogEntry logEntry = record.value();

            // print all log entries to console
            System.out.printf("Received Log [Partition: %d, Offset: %d]: %s%n",
                            record.partition(), record.offset(), logEntry);

            // save ERROR level logs to file
            if (logEntry.getLevel() == LogEntry.LogLevel.ERROR ||
                logEntry.getLevel() == LogEntry.LogLevel.FATAL) {
                saveErrorLogToFile(logEntry);
            }

        } catch (Exception e) {
            logger.error("Error processing log entry from partition {} offset {}: {}",
                       record.partition(), record.offset(), e.getMessage(), e);
        }
    }

    /**
     * saves ERROR and FATAL level logs to a daily file
     */
    private void saveErrorLogToFile(LogEntry logEntry) {
        String fileName = String.format("%s/error-logs-%s.log",
                                       errorLogDir,
                                       LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        Path filePath = Paths.get(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath,
                                                            java.nio.file.StandardOpenOption.CREATE,
                                                            java.nio.file.StandardOpenOption.APPEND)) {
            writer.write(logEntry.toString());
            writer.newLine();
            writer.flush();

            logger.debug("Saved error log to file: {}", fileName);

        } catch (IOException e) {
            logger.error("Failed to save error log to file {}: {}", fileName, e.getMessage(), e);
        }
    }

    /**
     * stops the consumer gracefully
     */
    public void stopConsuming() {
        logger.info("Stopping log consumption...");
        running.set(false);
    }

    /**
     * checks if the consumer is currently running
     */
    public boolean isRunning() {
        return running.get();
    }
}
