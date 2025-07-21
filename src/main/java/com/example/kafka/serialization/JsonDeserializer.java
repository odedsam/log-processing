package com.example.kafka.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Generic JSON deserializer for Kafka that uses Jackson ObjectMapper
 */
public class JsonDeserializer<T> implements Deserializer<T> {

    private static final Logger logger = LoggerFactory.getLogger(JsonDeserializer.class);
    private final ObjectMapper objectMapper;
    private final Class<T> targetType;

    public JsonDeserializer(Class<T> targetType) {
        this.targetType = targetType;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // no additional configuration needed
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return objectMapper.readValue(data, targetType);
        } catch (Exception e) {
            logger.error("Error deserializing JSON from topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Failed to deserialize JSON to object", e);
        }
    }

    @Override
    public void close() {
        // nothing to close
    }
}
