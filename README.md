# Smart Grid Anomaly Detection Pipeline

A real-time streaming pipeline for detecting anomalies in smart grid sensor data using Apache Flink and Kafka.

## Overview

This system processes high-volume sensor readings from electrical grid infrastructure, identifies potential equipment failures and voltage anomalies using statistical analysis, and provides real-time monitoring through a Grafana dashboard.

## Architecture

- **Data Generator**: Simulates smart grid sensor readings with configurable anomaly rates
- **Apache Kafka**: Message streaming platform for sensor data and anomaly events  
- **Apache Flink**: Stream processing engine with moving average anomaly detection
- **Kafka Exporter**: Exposes Kafka metrics for monitoring
- **Prometheus**: Metrics collection and storage
- **Grafana**: Real-time dashboard visualization

## Quick Start

```bash
# Start all services
docker-compose up -d

# Submit Flink job
docker exec flink-jobmanager flink run -c com.grid.AnomalyDetectionJob /opt/flink/lib/anomaly-detection-1.0.0.jar

# Access dashboard
# http://localhost:3000/d/c28cad80-4af5-4098-ac44-27b6ff30bcef/smart-grid-pipeline-health-working
```

## Features

- Real-time anomaly detection using statistical moving averages
- Configurable anomaly thresholds and detection windows
- JSON-based event serialization for compatibility
- Comprehensive monitoring with Prometheus metrics
- Interactive Grafana dashboard for pipeline health

## Dashboard

Monitor pipeline health, Kafka topic metrics, and service status through the Grafana dashboard displaying real-time streaming analytics and system performance indicators.
