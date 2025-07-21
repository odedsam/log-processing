package com.example.kafka.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Generic JSON serializer for Kafka that uses Jackson ObjectMapper
 */
public class JsonSerializer<T> implements Serializer<T> {

    private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);
    private final ObjectMapper objectMapper;

    public JsonSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // no additional configuration needed
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            logger.error("Error serializing object to JSON for topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    @Override
    public void close() {
        // nothing to close
    }
}
