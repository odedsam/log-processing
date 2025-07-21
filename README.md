# Kafka Log Processing System

A complete Java-based log processing system using Apache Kafka in KRaft mode (broker-only, no ZooKeeper). The system includes a log producer, consumer, and proper JSON serialization/deserialization using Jackson.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/kafka/
│   │       ├── LogProducerApp.java          # Producer main application
│   │       ├── LogConsumerApp.java          # Consumer main application
│   │       ├── config/
│   │       │   └── KafkaConfig.java         # Kafka configuration for KRaft mode
│   │       ├── model/
│   │       │   └── LogEntry.java            # Log entry model with Jackson annotations
│   │       ├── producer/
│   │       │   └── LogProducer.java         # Kafka log producer
│   │       ├── consumer/
│   │       │   └── LogConsumer.java         # Kafka log consumer
│   │       └── serialization/
│   │           ├── JsonSerializer.java      # Custom Kafka JSON serializer
│   │           └── JsonDeserializer.java    # Custom Kafka JSON deserializer
│   └── resources/
│       └── logback.xml                      # Logging configuration
├── error-logs/                             # Directory for error log files (created automatically)
├── logs/                                   # Directory for application logs (created automatically)
└── pom.xml                                 # Maven dependencies and build configuration
```

## Features

- **LogEntry Model**: Complete log entry class with timestamp, level, source, message, and thread information
- **JSON Serialization**: Custom Jackson-based serialization for Kafka messages
- **KRaft Mode Support**: Configured specifically for Kafka clusters running in KRaft mode (no ZooKeeper)
- **Producer Application**: Interactive console application for sending various types of log entries
- **Consumer Application**: Processes all log entries and saves ERROR/FATAL logs to daily files
- **Automatic Log Generation**: Built-in feature for generating realistic log entries automatically
- **Error Handling**: Comprehensive error handling and logging throughout the system
- **Graceful Shutdown**: Proper cleanup of resources when applications are terminated

## Prerequisites

1. **Java 11 or higher**
2. **Maven 3.6 or higher**
3. **Apache Kafka cluster running in KRaft mode**

## Kafka Setup (KRaft Mode)

If you need to set up a local Kafka cluster in KRaft mode:

```bash
# Download and extract Kafka
wget https://downloads.apache.org/kafka/2.8.2/kafka_2.13-2.8.2.tgz
tar -xzf kafka_2.13-2.8.2.tgz
cd kafka_2.13-2.8.2

# Generate cluster ID
./bin/kafka-storage.sh random-uuid

# Format storage directory (replace CLUSTER_ID with the generated UUID)
./bin/kafka-storage.sh format -t <CLUSTER_ID> -c config/kraft/server.properties

# Start Kafka server in KRaft mode
./bin/kafka-server-start.sh config/kraft/server.properties
```

## Building the Project

```bash
# Clone or download the project
git clone <repository-url>
cd kafka-log-processing

# Build the project
mvn clean compile

# Create the topic (optional - will be created automatically)
# Replace localhost:9092 with your Kafka bootstrap servers if different
kafka-topics.sh --create --topic application-logs --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

## Configuration

Update the Kafka configuration in `KafkaConfig.java` if your Kafka cluster is not running on `localhost:9092`:

```java
public static final String BOOTSTRAP_SERVERS = "your-kafka-server:9092";
```

## Running the Applications

### Option 1: Using Maven Exec Plugin

**Start the Consumer:**
```bash
mvn exec:java@run-consumer
```

**Start the Producer (in another terminal):**
```bash
mvn exec:java@run-producer
```

### Option 2: Using Java Directly

**Compile first:**
```bash
mvn compile
```

**Start the Consumer:**
```bash
mvn exec:java -Dexec.mainClass="com.example.kafka.LogConsumerApp"
```

**Start the Producer:**
```bash
mvn exec:java -Dexec.mainClass="com.example.kafka.LogProducerApp"
```

## Using the Producer Application

The producer offers several interactive options:

1. **Send Sample Logs** - Sends predefined log entries with various levels
2. **Send Custom Log Entry** - Create your own log entry interactively
3. **Start Automatic Log Generation** - Generates realistic logs every 2 seconds
4. **Send Mixed Level Logs** - Demonstrates all log levels including ERROR logs that will be saved to files
5. **Quit** - Exit the application

Example session:
```
=== Kafka Log Producer ===
Commands:
  1 - Send sample log entries
  2 - Send custom log entry
  3 - Start automatic log generation
  4 - Send mixed level logs (demo)
  q - Quit

Enter command: 4
Sending mixed level logs for demonstration...
Sent (1/10): [2025-01-15 10:30:15] INFO [StartupService:main] Application started successfully
...
```

## Consumer Behavior

The consumer automatically:
- **Prints all log entries** to the console with partition and offset information
- **Saves ERROR and FATAL logs** to daily files in the `error-logs/` directory
- **Uses manual commit** for better message processing reliability
- **Handles graceful shutdown** when interrupted (Ctrl+C)

Example consumer output:
```
=== Kafka Log Consumer ===
Starting log consumption...
- All logs will be printed to console
- ERROR and FATAL logs will be saved to files in 'error-logs/' directory
- Press Ctrl+C to stop gracefully

Received Log [Partition: 0, Offset: 15]: [2025-01-15 10:30:15] INFO [UserService:main] User login successful
Received Log [Partition: 1, Offset: 8]: [2025-01-15 10:30:16] ERROR [PaymentService:main] Payment processing failed
```

## Error Log Files

ERROR and FATAL level logs are automatically saved to files in the `error-logs/` directory:
- Filename format: `error-logs-YYYY-MM-DD.log`
- One file per day
- Contains only ERROR and FATAL level entries
- Files are created automatically when first error occurs

## Key Components

### LogEntry Class
- Represents a structured log message
- Uses Jackson annotations for JSON serialization
- Includes timestamp, log level, source, message, and thread information
- Supports all standard log levels: TRACE, DEBUG, INFO, WARN, ERROR, FATAL

### KafkaConfig Class
- Centralized configuration for Kafka clients
- Optimized for KRaft mode (no ZooKeeper references)
- Includes performance tuning settings
- Uses idempotent producer configuration

### Producer Features
- Asynchronous and synchronous sending options
- Callback-based error handling
- Proper resource cleanup
- Interactive console interface

### Consumer Features
- Automatic offset management with manual commits
- Dual processing: console output + file storage for errors
- Graceful shutdown handling
- Robust error handling and logging

## Troubleshooting

### Common Issues

1. **Connection refused error:**
   - Ensure Kafka is running and accessible at the configured bootstrap servers
   - Check that the topic exists or enable auto-creation

2. **Serialization errors:**
   - Verify Jackson dependencies are correctly loaded
   - Check LogEntry class structure matches expected JSON format

3. **Consumer group rebalancing:**
   - Normal behavior when multiple consumers join/leave
   - Monitor logs for excessive rebalancing

4. **File permission errors:**
   - Ensure write permissions for `error-logs/` directory
   - Check disk space availability

### Monitoring

The application uses SLF4J with Logback for comprehensive logging:
- Application logs: `logs/kafka-log-processor.log`
- Console output for real-time monitoring
- Configurable log levels in `logback.xml`

## Dependencies

- **Apache Kafka Client**: 3.6.1
- **Jackson**: 2.16.1 (Core, Databind, JSR310)
- **SLF4J + Logback**: For logging
- **Maven**: Build and dependency management

## License

This project is provided as an example implementation for educational and development purposes.
