package com.grid;

import com.grid.model.AnomalyEventPojo;
import com.grid.events.SensorReading;
import com.grid.functions.AnomalyDetectorFunction;
import com.grid.functions.MovingAverageFunction;
import com.grid.serde.JsonSerializationSchema;
import com.grid.sinks.AnomalyS3ParquetSink;
import com.grid.sinks.InfluxDbSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import com.grid.serde.JsonDeserializationSchema;

/**
 * Main class for the Anomaly Detection Flink job.
 */
public class AnomalyDetectionJob {

    public static void main(String[] args) throws Exception {
        // Set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Enable checkpointing for MinIO sink (every 10 seconds)
        env.enableCheckpointing(10000);

        // Kafka and Schema Registry properties
        final String bootstrapServers = "kafka:9092";
        final String schemaRegistryUrl = "http://schema-registry:8080/apis/registry/v2";
        final String topic = "sensor-readings";
        final String groupId = "anomaly-detector-group";

        // Configure the Kafka source (back to working version)
        KafkaSource<SensorReading> source = KafkaSource.<SensorReading>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new JsonDeserializationSchema())
                .build();

        // Add the source to the environment
        DataStream<SensorReading> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        // Process the stream to calculate moving average and detect anomalies
        DataStream<AnomalyEventPojo> anomalyStream = stream
                .keyBy(SensorReading::getMeterId)
                .process(new MovingAverageFunction())
                .process(new AnomalyDetectorFunction(2.5)); // Using default threshold of 2.5

        // Configure the Kafka sink for anomalies
        KafkaSink<AnomalyEventPojo> anomalySink = KafkaSink.<AnomalyEventPojo>builder()
                .setBootstrapServers(bootstrapServers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("anomalies")
                        .setValueSerializationSchema(new JsonSerializationSchema<>(AnomalyEventPojo.class))
                        .build())
                .build();

        // Add the anomaly sink to the stream
        anomalyStream.sinkTo(anomalySink).name("Anomaly Kafka Sink");

        // Add the MinIO sink for long-term storage (temporarily disabled)
        // anomalyStream
        //     .map(anomaly -> new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(anomaly))
        //     .sinkTo(AnomalyS3ParquetSink.create("s3://anomalies/"))
        //     .name("Anomaly MinIO Sink");

        // Add the InfluxDB sink for anomalies
        // anomalyStream.addSink(new InfluxDbSink()).name("InfluxDB Sink");

        // Print the anomaly stream to the console for debugging
        anomalyStream.print().name("Anomaly Stream");

        // Execute the Flink job
        env.execute("Real-Time Smart Grid Anomaly Detection");
    }
}
