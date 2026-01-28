package com.grid;

import com.grid.model.AnomalyEventPojo;
import com.grid.sinks.AnomalyS3ParquetSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineFormat;
import org.apache.flink.core.fs.Path;

/**
 * Simple demo job to test MinIO sink functionality
 */
public class MinIODemoJob {

    public static void main(String[] args) throws Exception {
        // Set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Disable checkpointing for now to test MinIO sink
        // env.enableCheckpointing(10000);

        // Create some sample data
        DataStream<AnomalyEventPojo> anomalyStream = env.fromElements(
            createSampleAnomaly("meter-0001", "voltage_spike", "WARNING"),
            createSampleAnomaly("meter-0002", "equipment_failure", "CRITICAL"),
            createSampleAnomaly("meter-0003", "voltage_spike", "ERROR")
        );

        // Add MinIO sink
        anomalyStream
            .map(anomaly -> {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    return mapper.writeValueAsString(anomaly);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "{\"error\": \"serialization failed\"}";
                }
            })
            .sinkTo(AnomalyS3ParquetSink.create("file:///opt/flink/anomalies-demo/"))
            .name("MinIO Demo Sink");

        // Print for debugging
        anomalyStream.print().name("Sample Anomalies");

        // Execute the Flink job
        env.execute("MinIO Demo Job");
    }

    private static AnomalyEventPojo createSampleAnomaly(String meterId, String type, String severity) {
        AnomalyEventPojo anomaly = new AnomalyEventPojo();
        anomaly.setAnomalyType(type);
        anomaly.setSeverity(severity);
        anomaly.setThresholdUsed(250.0);
        anomaly.setDeviation(30.0);
        
        // Create a simple original event
        com.grid.model.SensorReadingPojo original = new com.grid.model.SensorReadingPojo();
        original.setMeterId(meterId);
        original.setTimestamp(java.time.Instant.now());
        original.setVoltage(280.0);
        original.setCurrent(10.0);
        original.setTemperature(25.0);
        original.setStatus("FAILURE".equals(type) ? "FAILURE" : null);
        
        anomaly.setOriginalEvent(original);
        return anomaly;
    }
}
