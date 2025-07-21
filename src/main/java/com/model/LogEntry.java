package com.example.kafka.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * LogEntry represents a log message with timestamp, level, source, and message content.
 * this class uses Jackson annotations for JSON serialization/deserialization.
 */
public class LogEntry {

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime timestamp;

    @JsonProperty("level")
    private LogLevel level;

    @JsonProperty("source")
    private String source;

    @JsonProperty("message")
    private String message;

    @JsonProperty("thread")
    private String thread;

    // default constructor for Jackson
    public LogEntry() {}

    public LogEntry(LogLevel level, String source, String message) {
        this.timestamp = LocalDateTime.now();
        this.level = level;
        this.source = source;
        this.message = message;
        this.thread = Thread.currentThread().getName();
    }

    public LogEntry(LocalDateTime timestamp, LogLevel level, String source, String message, String thread) {
        this.timestamp = timestamp;
        this.level = level;
        this.source = source;
        this.message = message;
        this.thread = thread;
    }

    // getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s [%s:%s] %s",
            timestamp, level, source, thread, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogEntry logEntry = (LogEntry) o;
        return Objects.equals(timestamp, logEntry.timestamp) &&
               level == logEntry.level &&
               Objects.equals(source, logEntry.source) &&
               Objects.equals(message, logEntry.message) &&
               Objects.equals(thread, logEntry.thread);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, level, source, message, thread);
    }

    /**
     * enum representing different log levels
     */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }
}
