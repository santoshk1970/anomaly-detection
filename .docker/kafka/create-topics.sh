#!/bin/bash
# This script is executed inside the Kafka container on startup

# Wait for Kafka to be ready
/usr/bin/cub kafka-ready -b localhost:9092 1 60

# Create topics
kafka-topics --create --if-not-exists --topic sensor-readings \
  --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

kafka-topics --create --if-not-exists --topic anomalies \
  --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

kafka-topics --create --if-not-exists --topic dead-letter-queue \
  --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

echo "Kafka topics created!"
