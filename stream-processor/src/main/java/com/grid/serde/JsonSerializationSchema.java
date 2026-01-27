package com.grid.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.serialization.SerializationSchema;

public class JsonSerializationSchema<T> implements SerializationSchema<T> {
    
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Class<T> clazz;
    
    public JsonSerializationSchema(Class<T> clazz) {
        this.clazz = clazz;
    }
    
    @Override
    public byte[] serialize(T element) {
        try {
            return mapper.writeValueAsString(element).getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize element", e);
        }
    }
}
