# Configuration Guide

## Overview

This document provides comprehensive configuration details for all components of the Smart Grid Anomaly Detection Pipeline.

## Environment Variables

### General Configuration

```bash
# Project Configuration
PROJECT_NAME=smart-grid-anomaly-detection
VERSION=1.0.0
ENVIRONMENT=development

# Network Configuration
NETWORK_NAME=grid-network
```

### Kafka Configuration

```bash
# Kafka Broker
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181

# Topics
KAFKA_INPUT_TOPIC=sensor-readings
KAFKA_OUTPUT_TOPIC=anomalies
KAFKA_PARTITIONS=3
KAFKA_REPLICATION_FACTOR=1

# Producer Configuration
KAFKA_PRODUCER_ACKS=all
KAFKA_PRODUCER_RETRIES=3
KAFKA_PRODUCER_BATCH_SIZE=16384
KAFKA_PRODUCER_LINGER_MS=5

# Consumer Configuration
KAFKA_CONSUMER_GROUP_ID=anomaly-detection-group
KAFKA_CONSUMER_AUTO_OFFSET_RESET=earliest
KAFKA_CONSUMER_ENABLE_AUTO_COMMIT=false
```

### Flink Configuration

```bash
# Job Manager
FLINK_JOBMANAGER_RPC_ADDRESS=flink-jobmanager
FLINK_JOBMANAGER_RPC_PORT=6123
FLINK_JOBMANAGER_WEB_PORT=8081

# Task Manager
FLINK_TASKMANAGER_NUMBER_OF_TASK_SLOTS=4
FLINK_TASKMANAGER_MEMORY_PROCESS_SIZE=4096m

# Checkpointing
FLINK_CHECKPOINTING_INTERVAL=10000
FLINK_CHECKPOINTING_MODE=EXACTLY_ONCE
FLINK_CHECKPOINTING_TIMEOUT=600000
FLINK_CHECKPOINTING_MIN_PAUSE=500

# Parallelism
FLINK_PARALLELISM_DEFAULT=2
```

### MinIO Configuration

```bash
# MinIO Server
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin
MINIO_PORT=9000
MINIO_CONSOLE_PORT=9001

# S3 Configuration
S3_ENDPOINT=http://minio:9000
S3_ACCESS_KEY=minioadmin
S3_SECRET_KEY=minioadmin
S3_PATH_STYLE_ACCESS=true
S3_REGION=us-east-1

# Bucket Configuration
S3_BUCKET_ANOMALIES=anomalies
S3_BUCKET_ANOMALIES_DEMO=anomalies-demo
```

### Monitoring Configuration

```bash
# Prometheus
PROMETHEUS_PORT=9090
PROMETHEUS_RETENTION=15d

# Grafana
GRAFANA_PORT=3000
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin

# Kafka Exporter
KAFKA_EXPORTER_PORT=9308
```

## Configuration Files

### Docker Compose

**File**: `docker-compose.yml`

Key configuration sections:

```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: true

  flink-jobmanager:
    image: flink:1.18.1
    command: jobmanager
    environment:
      - JOB_MANAGER_RPC_ADDRESS=flink-jobmanager
    ports:
      - "8081:8081"

  flink-taskmanager:
    image: flink:1.18.1
    command: taskmanager
    environment:
      - JOB_MANAGER_RPC_ADDRESS=flink-jobmanager
    depends_on:
      - flink-jobmanager

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"
```

### Flink Configuration

**File**: `.docker/flink/flink-conf.yaml`

```yaml
# Job Manager Configuration
jobmanager.rpc.address: flink-jobmanager
jobmanager.rpc.port: 6123
jobmanager.memory.process.size: 1600m
jobmanager.execution.failover-strategy: region

# Task Manager Configuration
taskmanager.memory.process.size: 4096m
taskmanager.numberOfTaskSlots: 4
taskmanager.memory.network.fraction: 0.1
taskmanager.memory.managed.fraction: 0.4

# Parallelism
parallelism.default: 2

# Checkpointing
execution.checkpointing.interval: 10s
execution.checkpointing.mode: EXACTLY_ONCE
execution.checkpointing.timeout: 600000
execution.checkpointing.min-pause: 500
state.backend: filesystem
state.checkpoints.dir: file:///tmp/flink-checkpoints
state.savepoints.dir: file:///tmp/flink-savepoints

# Restart Strategy
restart-strategy: fixed-delay
restart-strategy.fixed-delay.attempts: 3
restart-strategy.fixed-delay.delay: 10s

# S3/MinIO Configuration
s3.endpoint: http://minio:9000
s3.path.style.access: true
s3.access-key: minioadmin
s3.secret-key: minioadmin
fs.s3a.endpoint: http://minio:9000
fs.s3a.path.style.access: true
fs.s3a.access.key: minioadmin
fs.s3a.secret.key: minioadmin
fs.s3a.connection.ssl.enabled: false
fs.s3a.impl: org.apache.hadoop.fs.s3a.S3AFileSystem
fs.s3a.aws.credentials.provider: org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider
fs.s3a.attempts.maximum: 3
fs.s3a.connection.establish.timeout: 5000
fs.s3a.connection.maximum.idle: 10000
fs.s3a.endpoint.region: us-east-1

# Web Frontend
web.submit.enable: true
web.cancel.enable: true

# JVM Options
env.java.opts: "--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"

# Blob Server
blob.server.port: 6124
query.server.port: 6125

# Metrics
metrics.reporter.prometheus.class: org.apache.flink.metrics.prometheus.PrometheusReporter
metrics.reporter.prometheus.port: 9999
metrics.reporter.prometheus.delete-gauges: true
metrics.reporter.prometheus.delete-counters: true
metrics.reporter.prometheus.delete-histograms: true
metrics.reporter.prometheus.delete-meters: true
```

### Kafka Configuration

**File**: `.docker/kafka/kafka.env`

```bash
# Kafka Broker Configuration
KAFKA_BROKER_ID=1
KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092
KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
KAFKA_AUTO_CREATE_TOPICS_ENABLE=true
KAFKA_DELETE_TOPIC_ENABLE=true

# Topic Configuration
KAFKA_TOPIC_SENSOR_READINGS=sensor-readings
KAFKA_TOPIC_ANOMALIES=anomalies
KAFKA_TOPIC_PARTITIONS=3
KAFKA_TOPIC_REPLICATION_FACTOR=1

# Performance Tuning
KAFKA_NUM_NETWORK_THREADS=3
KAFKA_NUM_IO_THREADS=8
KAFKA_SOCKET_SEND_BUFFER_BYTES=102400
KAFKA_SOCKET_RECEIVE_BUFFER_BYTES=102400
KAFKA_SOCKET_REQUEST_MAX_BYTES=104857600

# Log Configuration
KAFKA_LOG_RETENTION_HOURS=168
KAFKA_LOG_SEGMENT_BYTES=1073741824
KAFKA_LOG_RETENTION_CHECK_INTERVAL_MS=300000
```

### Prometheus Configuration

**File**: `.docker/prometheus/prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "alert_rules.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'flink-jobmanager'
    static_configs:
      - targets: ['flink-jobmanager:9999']

  - job_name: 'flink-taskmanager'
    static_configs:
      - targets: ['flink-taskmanager:9999']

  - job_name: 'kafka-exporter'
    static_configs:
      - targets: ['kafka-exporter:9308']

  - job_name: 'kafka-broker'
    static_configs:
      - targets: ['kafka:9308']
```

### Grafana Configuration

**File**: `.docker/grafana/provisioning/datasources/prometheus.yml`

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
```

## Application Configuration

### Flink Job Configuration

**File**: `stream-processor/src/main/resources/application.properties`

```properties
# Anomaly Detection Configuration
anomaly.threshold.voltage=250.0
anomaly.window.size=10
anomaly.severity.threshold.warning=20.0
anomaly.severity.threshold.error=50.0
anomaly.severity.threshold.critical=100.0

# Kafka Configuration
kafka.bootstrap.servers=kafka:9092
kafka.input.topic=sensor-readings
kafka.output.topic=anomalies
kafka.consumer.group.id=anomaly-detection-group

# Checkpointing Configuration
checkpointing.interval.ms=10000
checkpointing.timeout.ms=600000
checkpointing.min.pause.ms=500

# Storage Configuration
storage.s3.endpoint=http://minio:9000
storage.s3.bucket=anomalies
storage.s3.path.style.access=true
storage.local.path=/opt/flink/anomalies

# Monitoring Configuration
metrics.prometheus.port=9999
metrics.jmx.port=9998
```

### Data Generator Configuration

**File**: `data-generator/config/generator.properties`

```properties
# Generation Configuration
generation.rate.events.per.second=100
generation.anomaly.rate=0.05
generation.meters.count=100
generation.duration.minutes=60

# Data Configuration
data.voltage.min=220.0
data.voltage.max=260.0
data.current.min=5.0
data.current.max=25.0
data.temperature.min=15.0
data.temperature.max=35.0

# Kafka Configuration
kafka.bootstrap.servers=localhost:9092
kafka.topic=sensor-readings
kafka.producer.client.id=data-generator
```

## Performance Tuning

### Kafka Performance

```yaml
# Producer Performance
producer.batch.size: 32768
producer.linger.ms: 5
producer.compression.type: snappy
producer.acks: all
producer.retries: 3

# Consumer Performance
consumer.fetch.min.bytes: 1024
consumer.fetch.max.wait.ms: 500
consumer.max.poll.records: 1000
consumer.session.timeout.ms: 30000
```

### Flink Performance

```yaml
# Memory Configuration
taskmanager.memory.network.fraction: 0.1
taskmanager.memory.managed.fraction: 0.4
taskmanager.memory.framework.heap: 128m
taskmanager.memory.task.heap: 2048m

# Network Configuration
taskmanager.network.numberOfBuffers: 2048
taskmanager.network.memory.buffers-per-channel: 2
taskmanager.network.memory.floating-buffers-per-gate: 8

# Checkpointing Performance
execution.checkpointing.max-concurrent-checkpoints: 1
execution.checkpointing.min-pause: 500
state.backend.rocksdb.memory.managed: true
state.backend.rocksdb.memory.fixed-per-slot: true
```

### JVM Tuning

```bash
# Job Manager JVM
env.java.opts.jobmanager: "-Xms1024m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Task Manager JVM
env.java.opts.taskmanager: "-Xms4096m -Xmx4096m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
```

## Security Configuration

### SSL/TLS Configuration

```yaml
# Kafka SSL
ssl.keystore.location: /path/to/kafka.server.keystore.jks
ssl.keystore.password: password
ssl.key.password: password
ssl.truststore.location: /path/to/kafka.server.truststore.jks
ssl.truststore.password: password
ssl.protocol: TLSv1.2

# MinIO SSL
mc config host add local https://minio:9000 minioadmin minioadmin
```

### Authentication

```yaml
# Kafka SASL
sasl.enabled.mechanisms: PLAIN
sasl.mechanism.inter.broker.protocol: PLAIN
security.inter.broker.protocol: SASL_PLAINTEXT

# MinIO IAM
MINIO_IDENTITY_OPENID_CONFIG_URL: http://keycloak:8080/auth/realms/master/.well-known/openid-configuration
MINIO_IDENTITY_OPENID_CLAIM_NAME: policy
MINIO_IDENTITY_OPENID_CLIENT_ID: minio
```

## Monitoring Configuration

### Alert Rules

**File**: `.docker/prometheus/alert_rules.yml`

```yaml
groups:
  - name: smart-grid-alerts
    rules:
      - alert: HighAnomalyRate
        expr: rate(anomaly_detection_anomalies_total[5m]) > 10
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High anomaly detection rate"
          description: "Anomaly detection rate is {{ $value }} per second"

      - alert: FlinkJobDown
        expr: up{job="flink-jobmanager"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Flink JobManager is down"
          description: "Flink JobManager has been down for more than 1 minute"

      - alert: KafkaLag
        expr: kafka_consumer_lag_sum > 1000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Kafka consumer lag is high"
          description: "Consumer lag is {{ $value }} messages"
```

### Grafana Dashboard

**Dashboard JSON**: Available in Grafana import
- Dashboard ID: `c28cad80-4af5-4098-ac44-27b6ff30bcef`
- Panels: Pipeline health, Kafka metrics, Flink performance, resource usage

## Environment-Specific Configurations

### Development

```bash
# Reduced resource usage
FLINK_PARALLELISM_DEFAULT=1
KAFKA_PARTITIONS=1
KAFKA_REPLICATION_FACTOR=1

# Increased logging
LOG_LEVEL=DEBUG
```

### Staging

```bash
# Medium resource usage
FLINK_PARALLELISM_DEFAULT=2
KAFKA_PARTITIONS=3
KAFKA_REPLICATION_FACTOR=2

# Standard logging
LOG_LEVEL=INFO
```

### Production

```bash
# Full resource usage
FLINK_PARALLELISM_DEFAULT=4
KAFKA_PARTITIONS=6
KAFKA_REPLICATION_FACTOR=3

# Minimal logging
LOG_LEVEL=WARN

# Security enabled
SECURITY_ENABLED=true
SSL_ENABLED=true
```

## Troubleshooting Configuration

### Common Issues

1. **Kafka Connection Issues**
   ```bash
   # Check Kafka connectivity
   docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
   
   # Check Kafka logs
   docker logs kafka
   ```

2. **Flink Job Failures**
   ```bash
   # Check Flink logs
   docker logs flink-jobmanager
   docker logs flink-taskmanager
   
   # Check job status
   curl http://localhost:8081/jobs
   ```

3. **MinIO Storage Issues**
   ```bash
   # Check MinIO status
   docker logs minio
   
   # Verify bucket exists
   docker exec minio mc ls minio/
   ```

### Configuration Validation

```bash
# Validate Kafka configuration
docker exec kafka kafka-configs --bootstrap-server localhost:9092 --entity-type topics --describe

# Validate Flink configuration
docker exec flink-jobmanager flink config

# Validate MinIO configuration
docker exec minio mc admin info local
```

---

**Note**: Always test configuration changes in a development environment before applying to production. Monitor system performance after configuration updates.
