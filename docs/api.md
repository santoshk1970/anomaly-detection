# API Documentation

## Overview

This document describes the APIs and interfaces available in the Smart Grid Anomaly Detection Pipeline.

## Data Models

### Sensor Reading

**Topic**: `sensor-readings`

**Format**: JSON

```json
{
  "meterId": "string",
  "timestamp": "number",
  "voltage": "number",
  "current": "number",
  "temperature": "number",
  "status": "string|null"
}
```

**Fields**:
- `meterId` (string): Unique identifier for the smart meter
- `timestamp` (number): Unix timestamp in milliseconds
- `voltage` (number): Voltage reading in volts
- `current` (number): Current reading in amperes
- `temperature` (number): Temperature reading in Celsius
- `status` (string|null): Status indicator (normal/warning/error) or null

**Example**:
```json
{
  "meterId": "meter-0001",
  "timestamp": 1706729400000,
  "voltage": 245.5,
  "current": 15.2,
  "temperature": 25.0,
  "status": "normal"
}
```

### Anomaly Event

**Topic**: `anomalies`

**Format**: JSON

```json
{
  "anomalyType": "string",
  "severity": "string",
  "originalEvent": {
    "meterId": "string",
    "timestamp": "number",
    "voltage": "number",
    "current": "number",
    "temperature": "number",
    "status": "string|null"
  },
  "thresholdUsed": "number",
  "deviation": "number"
}
```

**Fields**:
- `anomalyType` (string): Type of anomaly detected
  - `voltage_spike`: Sudden voltage increase
  - `voltage_drop`: Sudden voltage decrease
  - `equipment_failure`: Complete failure indication
- `severity` (string): Severity level
  - `INFO`: Informational
  - `WARNING`: Warning level
  - `ERROR`: Error level
  - `CRITICAL`: Critical level
- `originalEvent` (object): The original sensor reading that triggered the anomaly
- `thresholdUsed` (number): The threshold value that was exceeded
- `deviation` (number): The amount by which the threshold was exceeded

**Example**:
```json
{
  "anomalyType": "voltage_spike",
  "severity": "WARNING",
  "originalEvent": {
    "meterId": "meter-0001",
    "timestamp": 1706729400000,
    "voltage": 280.0,
    "current": 10.0,
    "temperature": 25.0,
    "status": null
  },
  "thresholdUsed": 250.0,
  "deviation": 30.0
}
```

## Kafka Topics

### Input Topic: sensor-readings

**Configuration**:
- **Partitions**: 3 (configurable)
- **Replication Factor**: 1 (development), 3 (production)
- **Retention**: 7 days (configurable)
- **Cleanup Policy**: delete

**Producer Configuration**:
```properties
bootstrap.servers=kafka:9092
key.serializer=org.apache.kafka.common.serialization.StringSerializer
value.serializer=org.apache.kafka.common.serialization.StringSerializer
acks=all
retries=3
batch.size=16384
linger.ms=5
```

**Consumer Configuration**:
```properties
bootstrap.servers=kafka:9092
group.id=anomaly-detection-group
key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
auto.offset.reset=earliest
enable.auto.commit=false
```

### Output Topic: anomalies

**Configuration**:
- **Partitions**: 3 (configurable)
- **Replication Factor**: 1 (development), 3 (production)
- **Retention**: 30 days (configurable)
- **Cleanup Policy**: delete

## Flink Job Configuration

### Job Parameters

**Main Class**: `com.grid.AnomalyDetectionJob`

**Configuration Parameters**:
```java
// Anomaly detection thresholds
private static final double ANOMALY_THRESHOLD = 250.0;
private static final int WINDOW_SIZE = 10;

// Kafka configuration
String bootstrapServers = "kafka:9092";
String inputTopic = "sensor-readings";
String outputTopic = "anomalies";

// Checkpointing
env.enableCheckpointing(10000); // 10 seconds
env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
```

### Parallelism

**Default Parallelism**: 2

**Scaling Configuration**:
```bash
# Set parallelism via command line
docker exec flink-jobmanager flink run -c com.grid.AnomalyDetectionJob \
  -p 4 \
  /opt/flink/lib/anomaly-detection-1.0.0.jar
```

## REST Endpoints (Future)

### Health Check

**Endpoint**: `GET /health`

**Response**:
```json
{
  "status": "healthy",
  "timestamp": "2024-01-28T13:27:00Z",
  "version": "1.0.0",
  "components": {
    "kafka": "healthy",
    "flink": "healthy",
    "minio": "healthy"
  }
}
```

### Metrics

**Endpoint**: `GET /metrics`

**Response**:
```json
{
  "eventsProcessed": 150000,
  "anomaliesDetected": 1250,
  "processingLatency": 45.2,
  "throughput": 2500.5,
  "errorRate": 0.001
}
```

### Configuration

**Endpoint**: `GET /config`

**Response**:
```json
{
  "anomalyThreshold": 250.0,
  "windowSize": 10,
  "parallelism": 2,
  "checkpointInterval": 10000,
  "kafkaTopics": {
    "input": "sensor-readings",
    "output": "anomalies"
  }
}
```

## Monitoring APIs

### Flink REST API

**Base URL**: `http://localhost:8081`

**Endpoints**:
- `GET /jobs` - List all jobs
- `GET /jobs/{jobid}` - Get job details
- `GET /jobs/{jobid}/metrics` - Get job metrics
- `GET /taskmanagers` - List task managers

**Example**:
```bash
# Get job overview
curl http://localhost:8081/jobs

# Get specific job metrics
curl http://localhost:8081/jobs/112273f8ccaabe027a89ac0db27dfaa2/metrics?get=numRecordsInPerSecond
```

### Kafka Metrics

**JMX Endpoint**: `kafka:9092`

**Key Metrics**:
- `kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec`
- `kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec`
- `kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec`

### Prometheus Metrics

**Endpoint**: `http://localhost:9090/metrics`

**Custom Metrics**:
- `anomaly_detection_events_total`
- `anomaly_detection_anomalies_total`
- `anomaly_detection_processing_duration_ms`
- `anomaly_detection_error_total`

## Error Handling

### Error Response Format

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid sensor reading format",
    "details": {
      "field": "voltage",
      "reason": "Value must be numeric"
    },
    "timestamp": "2024-01-28T13:27:00Z"
  }
}
```

### Error Codes

| Code | Description | HTTP Status |
|------|-------------|-------------|
| `VALIDATION_ERROR` | Invalid data format | 400 |
| `PROCESSING_ERROR` | Internal processing failure | 500 |
| `KAFKA_ERROR` | Kafka connectivity issue | 503 |
| `STORAGE_ERROR` | Storage operation failure | 503 |

## SDK Examples

### Java Producer

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

Producer<String, String> producer = new KafkaProducer<>(props);

String sensorReading = "{\"meterId\":\"meter-0001\",\"timestamp\":1706729400000,\"voltage\":245.5,\"current\":15.2,\"temperature\":25.0,\"status\":\"normal\"}";

producer.send(new ProducerRecord<>("sensor-readings", "meter-0001", sensorReading));
producer.close();
```

### Python Producer

```python
from kafka import KafkaProducer
import json

producer = KafkaProducer(
    bootstrap_servers=['localhost:9092'],
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

sensor_reading = {
    "meterId": "meter-0001",
    "timestamp": 1706729400000,
    "voltage": 245.5,
    "current": 15.2,
    "temperature": 25.0,
    "status": "normal"
}

producer.send('sensor-readings', sensor_reading)
producer.flush()
producer.close()
```

### Java Consumer

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "anomaly-consumer");
props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

Consumer<String, String> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Arrays.asList("anomalies"));

while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
    for (ConsumerRecord<String, String> record : records) {
        System.out.printf("Anomaly detected: %s%n", record.value());
    }
}
```

## Testing

### Unit Tests

```java
@Test
public void testAnomalyDetection() {
    SensorReading reading = new SensorReading();
    reading.setMeterId("test-meter");
    reading.setVoltage(300.0); // Above threshold
    
    AnomalyDetectorFunction detector = new AnomalyDetectorFunction();
    detector.processElement(reading, null, new TestCollector());
    
    // Verify anomaly was detected
}
```

### Integration Tests

```java
@Test
public void testEndToEndProcessing() {
    // Send test data to Kafka
    // Wait for processing
    // Verify output in anomalies topic
    // Verify archival in MinIO
}
```

## Performance Considerations

### Throughput Optimization

1. **Batch Size**: Configure appropriate batch sizes for Kafka producers
2. **Compression**: Enable Snappy or LZ4 compression
3. **Partitioning**: Optimize partition count for parallelism
4. **Buffering**: Tune Flink network buffers

### Latency Optimization

1. **Checkpointing**: Balance between consistency and latency
2. **Network**: Minimize network hops
3. **Serialization**: Use efficient formats (Avro/Protobuf)
4. **Memory**: Allocate sufficient heap for Flink

### Resource Planning

| Component | CPU Cores | Memory | Storage |
|-----------|-----------|--------|---------|
| Kafka | 2-4 | 4-8GB | 100GB+ |
| Flink JobManager | 1-2 | 2-4GB | 10GB |
| Flink TaskManager | 2-4 | 4-8GB | 20GB |
| MinIO | 1-2 | 2-4GB | 500GB+ |
| Prometheus | 1 | 2GB | 50GB |
| Grafana | 1 | 1GB | 10GB |

---

**Note**: This API documentation will be expanded as new features and endpoints are added to the system.
