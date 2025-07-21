package com.example.kafka;

import com.example.kafka.consumer.LogConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * main application for running the Log Consumer
 */
public class LogConsumerApp {

    private static final Logger logger = LoggerFactory.getLogger(LogConsumerApp.class);

    public static void main(String[] args) {
        LogConsumer consumer = new LogConsumer();

        // add shutdown hook to gracefully stop the consumer
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down LogConsumer...");
            consumer.stopConsuming();

            // wait a bit for graceful shutdown
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        System.out.println("=== Kafka Log Consumer ===");
        System.out.println("Starting log consumption...");
        System.out.println("- All logs will be printed to console");
        System.out.println("- ERROR and FATAL logs will be saved to files in 'error-logs/' directory");
        System.out.println("- Press Ctrl+C to stop gracefully");
        System.out.println();

        try {
            // start consuming messages (this will block until stopped)
            consumer.startConsuming();
        } catch (Exception e) {
            logger.error("Fatal error in consumer application: {}", e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
}
