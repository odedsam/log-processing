package com.example.kafka;

import com.example.kafka.model.LogEntry;
import com.example.kafka.producer.LogProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * main application for running the Log Producer
 */
public class LogProducerApp {

    private static final Logger logger = LoggerFactory.getLogger(LogProducerApp.class);

    public static void main(String[] args) {
        LogProducer producer = new LogProducer();

        // add shutdown hook to clean up resources
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down LogProducer...");
            producer.close();
        }));

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Kafka Log Producer ===");
        System.out.println("Commands:");
        System.out.println("  1 - Send sample log entries");
        System.out.println("  2 - Send custom log entry");
        System.out.println("  3 - Start automatic log generation");
        System.out.println("  4 - Send mixed level logs (demo)");
        System.out.println("  q - Quit");
        System.out.println();

        boolean running = true;
        ScheduledExecutorService autoLogService = null;

        while (running) {
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "1":
                    sendSampleLogs(producer);
                    break;

                case "2":
                    sendCustomLog(producer, scanner);
                    break;

                case "3":
                    if (autoLogService == null || autoLogService.isShutdown()) {
                        autoLogService = startAutomaticLogGeneration(producer);
                        System.out.println("Started automatic log generation. Press Enter to stop...");
                        scanner.nextLine();
                        autoLogService.shutdown();
                        System.out.println("Stopped automatic log generation.");
                    } else {
                        System.out.println("Automatic log generation is already running!");
                    }
                    break;

                case "4":
                    sendMixedLevelLogs(producer);
                    break;

                case "q":
                case "quit":
                case "exit":
                    running = false;
                    break;

                default:
                    System.out.println("Unknown command: " + command);
            }
        }

        if (autoLogService != null && !autoLogService.isShutdown()) {
            autoLogService.shutdown();
        }

        producer.close();
        scanner.close();
        System.out.println("Producer application terminated.");
    }

    private static void sendSampleLogs(LogProducer producer) {
        System.out.println("Sending sample log entries...");

        LogEntry[] sampleLogs = {
            new LogEntry(LogEntry.LogLevel.INFO, "UserService", "User login successful for user: john.doe"),
            new LogEntry(LogEntry.LogLevel.WARN, "DatabasePool", "Connection pool is running low: 2 connections remaining"),
            new LogEntry(LogEntry.LogLevel.ERROR, "PaymentService", "Failed to process payment for order #12345"),
            new LogEntry(LogEntry.LogLevel.DEBUG, "CacheManager", "Cache hit rate: 85.2% for the last hour"),
            new LogEntry(LogEntry.LogLevel.INFO, "OrderService", "New order created: #12346"),
        };

        try {
            for (LogEntry logEntry : sampleLogs) {
                producer.sendLogEntrySync(logEntry);
                System.out.println("Sent: " + logEntry);
                Thread.sleep(100); // small delay between sends
            }
            System.out.println("All sample logs sent successfully!");
        } catch (Exception e) {
            logger.error("Error sending sample logs: {}", e.getMessage(), e);
            System.out.println("Error sending logs: " + e.getMessage());
        }
    }

    private static void sendCustomLog(LogProducer producer, Scanner scanner) {
        System.out.println("\n=== Custom Log Entry ===");

        System.out.print("Enter log level (TRACE, DEBUG, INFO, WARN, ERROR, FATAL): ");
        String levelStr = scanner.nextLine().trim().toUpperCase();
        LogEntry.LogLevel level;

        try {
            level = LogEntry.LogLevel.valueOf(levelStr);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid log level. Using INFO as default.");
            level = LogEntry.LogLevel.INFO;
        }

        System.out.print("Enter source/component name: ");
        String source = scanner.nextLine().trim();
        if (source.isEmpty()) {
            source = "CustomApp";
        }

        System.out.print("Enter log message: ");
        String message = scanner.nextLine().trim();
        if (message.isEmpty()) {
            message = "Custom log message";
        }

        try {
            LogEntry customLog = new LogEntry(level, source, message);
            producer.sendLogEntrySync(customLog);
            System.out.println("Custom log sent: " + customLog);
        } catch (Exception e) {
            logger.error("Error sending custom log: {}", e.getMessage(), e);
            System.out.println("Error sending custom log: " + e.getMessage());
        }
    }

    private static ScheduledExecutorService startAutomaticLogGeneration(LogProducer producer) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Random random = new Random();

        String[] sources = {"WebServer", "DatabaseService", "AuthService", "PaymentGateway",
                           "NotificationService", "FileProcessor", "ApiGateway", "CacheLayer"};

        String[] infoMessages = {
            "Request processed successfully",
            "User session created",
            "Data synchronization completed",
            "Configuration loaded",
            "Health check passed"
        };

        String[] warnMessages = {
            "Response time exceeded threshold",
            "Memory usage is high",
            "Connection pool nearly exhausted",
            "Deprecated API endpoint used",
            "Rate limit approaching"
        };

        String[] errorMessages = {
            "Database connection failed",
            "Authentication token expired",
            "File processing failed",
            "External service unavailable",
            "Validation error occurred"
        };

        scheduler.scheduleAtFixedRate(() -> {
            try {
                String source = sources[random.nextInt(sources.length)];
                LogEntry.LogLevel level;
                String message;

                int levelChoice = random.nextInt(100);
                if (levelChoice < 60) { // 60% INFO
                    level = LogEntry.LogLevel.INFO;
                    message = infoMessages[random.nextInt(infoMessages.length)];
                } else if (levelChoice < 85) { // 25% WARN
                    level = LogEntry.LogLevel.WARN;
                    message = warnMessages[random.nextInt(warnMessages.length)];
                } else { // 15% ERROR
                    level = LogEntry.LogLevel.ERROR;
                    message = errorMessages[random.nextInt(errorMessages.length)];
                }

                LogEntry logEntry = new LogEntry(level, source, message);
                producer.sendLogEntry(logEntry);
                System.out.printf("[AUTO] %s%n", logEntry);

            } catch (Exception e) {
                logger.error("Error in automatic log generation: {}", e.getMessage(), e);
            }
        }, 0, 2, TimeUnit.SECONDS);

        return scheduler;
    }

    private static void sendMixedLevelLogs(LogProducer producer) {
        System.out.println("Sending mixed level logs for demonstration...");

        LogEntry[] mixedLogs = {
            new LogEntry(LogEntry.LogLevel.INFO, "StartupService", "Application started successfully"),
            new LogEntry(LogEntry.LogLevel.DEBUG, "ConfigLoader", "Loading configuration from config.properties"),
            new LogEntry(LogEntry.LogLevel.INFO, "DatabaseService", "Database connection established"),
            new LogEntry(LogEntry.LogLevel.WARN, "MemoryMonitor", "Memory usage at 75% - consider cleanup"),
            new LogEntry(LogEntry.LogLevel.ERROR, "EmailService", "SMTP server connection timeout"),
            new LogEntry(LogEntry.LogLevel.FATAL, "SecurityService", "Critical security breach detected - shutting down"),
            new LogEntry(LogEntry.LogLevel.INFO, "UserService", "Password reset email sent to user@example.com"),
            new LogEntry(LogEntry.LogLevel.ERROR, "FileService", "Unable to write to log file - disk full"),
            new LogEntry(LogEntry.LogLevel.WARN, "RateLimiter", "API rate limit exceeded for client IP 192.168.1.100"),
            new LogEntry(LogEntry.LogLevel.INFO, "AuditService", "User logout recorded for session ABC123")
        };

        try {
            for (int i = 0; i < mixedLogs.length; i++) {
                producer.sendLogEntrySync(mixedLogs[i]);
                System.out.printf("Sent (%d/%d): %s%n", i + 1, mixedLogs.length, mixedLogs[i]);
                Thread.sleep(500); // half second delay between sends
            }
            System.out.println("\nAll mixed level logs sent! Check consumer for ERROR logs saved to file.");
        } catch (Exception e) {
            logger.error("Error sending mixed level logs: {}", e.getMessage(), e);
            System.out.println("Error sending logs: " + e.getMessage());
        }
    }
}

