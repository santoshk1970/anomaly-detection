package com.grid.serde;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grid.events.SensorReading;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;
import java.time.Instant;

public class JsonDeserializationSchema implements DeserializationSchema<SensorReading> {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public SensorReading deserialize(byte[] message) throws IOException {
        JsonNode node = mapper.readTree(message);
        
        SensorReading reading = new SensorReading();
        reading.setMeterId(node.get("meter_id").asText());
        reading.setTimestamp(Instant.ofEpochMilli(node.get("timestamp").asLong()));
        reading.setVoltage(node.get("voltage").asDouble());
        reading.setCurrent(node.get("current").asDouble());
        reading.setTemperature(node.get("temperature").asDouble());
        
        JsonNode statusNode = node.get("status");
        if (statusNode != null && !statusNode.isNull()) {
            reading.setStatus(statusNode.asText());
        }
        
        return reading;
    }
    
    @Override
    public boolean isEndOfStream(SensorReading nextElement) {
        return false;
    }
    
    @Override
    public TypeInformation<SensorReading> getProducedType() {
        return TypeInformation.of(SensorReading.class);
    }
}
