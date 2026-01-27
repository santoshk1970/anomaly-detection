# Implementation Plan: Real-Time Smart Grid Anomaly Detection

**Branch**: `001-grid-anomaly-detection` | **Date**: 2026-01-24 | **Spec**: [./spec.md](./spec.md)

## Summary

This plan outlines the technical implementation for a real-time anomaly detection system for smart grid sensor data. The system will ingest a high-throughput stream of sensor readings, process them to detect anomalies using stateful analysis, and route data to appropriate sinks for alerting, dashboarding, and long-term archival, with comprehensive monitoring throughout.

## Technical Context

- **Languages**:
    - **Stream Processor**: Java 17 (or Scala 2.12) for Apache Flink jobs
    - **Data Generator**: Python 3.11 (simpler for scripting/testing)
- **Primary Dependencies**:
    - **Message Broker**: Apache Kafka 3.6+ (KRaft mode, no Zookeeper)
    - **Stream Processor**: Apache Flink 1.18+ (Java/Scala API)
    - **Schema Registry**: Apicurio Schema Registry (Apache 2.0 license, Avro support)
    - **Anomaly Storage**: InfluxDB 2.x OSS (Time-series DB, single-node for MVP)
    - **Archive Storage**: MinIO (S3-compatible object storage)
    - **Dashboarding**: Grafana
    - **Metrics**: Prometheus
- **Build Tools**:
    - **Flink Jobs**: Maven or Gradle
    - **Python Components**: Poetry or pip
- **Testing**: 
    - **Flink**: JUnit 5 + Flink TestHarness
    - **Python**: pytest
- **Target Platform**: Docker Compose for local development, Kubernetes for production.
- **Performance Goals**: 10,000 events/sec/core throughput.
- **Constraints**: <100ms p99 latency for anomaly detection.
- **Scale/Scope**: Initial setup to support 10,000 simulated smart meters.

### Technology Rationale

**Why Java/Scala for Flink?**
- **Production maturity**: Flink's Java/Scala APIs are battle-tested with extensive connector ecosystem (Kafka, InfluxDB, S3)
- **Performance**: Native JVM execution eliminates PyFlink's interop overhead, critical for meeting <100ms p99 latency (SR-006)
- **Stateful processing**: Robust support for complex windowing, moving averages, and checkpointing required by FR-002
- **Community & documentation**: Extensive examples, Stack Overflow support, and enterprise adoption

**Why Kafka KRaft mode?**
- **Simpler deployment**: Eliminates Zookeeper dependency (one less service to manage)
- **Future-proof**: Zookeeper is deprecated in Kafka 3.x+; KRaft is the modern metadata management approach

**Why Apicurio Schema Registry?**
- **Fully open-source**: Apache 2.0 license (vs. Confluent Community License restrictions)
- **Avro support**: Full compatibility with Avro schema evolution (backward/forward compatibility)
- **Portable**: Works with any Kafka distribution (AWS MSK, self-hosted, etc.)

**Why keep Python for data-generator?**
- **Rapid prototyping**: Easier to script sensor data simulation and inject anomalies for testing
- **Test flexibility**: pytest enables quick integration test authoring for end-to-end pipeline validation

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Stream-First Architecture**: Yes, the design is centered around Kafka and Flink for continuous stream processing.
- [x] **Backpressure & Flow Control**: Yes, Flink and Kafka clients provide this natively.
- [x] **Event Sourcing & Immutability**: Yes, Kafka will serve as the immutable log of events.
- [x] **Observability & Monitoring**: Yes, using Prometheus for metrics and Grafana for dashboarding.
- [x] **Fault Tolerance & Recovery**: Yes, Flink's checkpointing mechanism will be used for stateful recovery.
- [x] **Test-First**: Yes, integration tests will be planned for each component.
- [x] **Performance Requirements**: Yes, the chosen stack is capable of meeting the specified performance goals.

## Project Structure

### Documentation (this feature)

```text
specs/001-grid-anomaly-detection/
├── plan.md              # This file
├── spec.md              # The feature specification
├── research.md          # To be created
├── data-model.md        # To be created
├── quickstart.md        # To be created
├── contracts/           # To be created (for event schemas)
└── tasks.md             # To be created
```

### Source Code (repository root)

We will use a service-oriented structure, with each component in its own directory, managed via Docker Compose.

```text
.docker/
├── kafka/
├── flink/
├── schema-registry/
├── influxdb/
├── grafana/
└── prometheus/

data-generator/         # Python 3.11
├── src/
│   └── generator.py
├── tests/
├── pyproject.toml
└── Dockerfile

stream-processor/       # Java 17 (Flink jobs)
├── src/
│   └── main/java/
│       └── com/grid/
│           ├── AnomalyDetectionJob.java
│           └── functions/
├── pom.xml (or build.gradle)
├── tests/
└── Dockerfile

dashboards/
└── grafana-provisioning/

docker-compose.yml
```

**Structure Decision**: This structure isolates each component of the system, making it easy to develop, test, and scale them independently. The `stream-processor/` will be a Maven/Gradle project producing a Flink JAR that runs in the Flink container. The `data-generator/` remains Python for ease of simulation scripting.

## Complexity Tracking

### Known Implementation Risks (Mitigated)

**RESOLVED - Flink Python API Immaturity**: 
- **Original Risk**: PyFlink lacks mature connectors, has JVM interop overhead, limited stateful API support
- **Mitigation**: Switched to Java/Scala for Flink stream processor (plan.md updated 2026-01-25)
- **Impact**: Eliminates latency risk for SR-006 (<100ms p99), provides production-grade connector ecosystem

**ACCEPTED - InfluxDB OSS Single-Node Limitation**:
- **Risk**: InfluxDB OSS has no clustering/HA; single-node bottleneck at scale
- **Mitigation Strategy**: 
  - MVP uses single-node InfluxDB (sufficient for 10k events/sec write load)
  - Future migration path: TimescaleDB (PostgreSQL-based, full clustering) if horizontal scaling needed
- **Impact**: Low risk for initial deployment; documented migration path if scale exceeds capacity

**RESOLVED - Confluent Schema Registry Licensing**:
- **Original Risk**: Confluent Community License restricts managed cloud service usage
- **Mitigation**: Switched to Apicurio Schema Registry (Apache 2.0 license)
- **Impact**: Full portability to any Kafka deployment (AWS MSK, self-hosted, etc.)

**RESOLVED - Zookeeper Operational Complexity**:
- **Original Risk**: Zookeeper adds deployment overhead, is deprecated in Kafka 3.x+
- **Mitigation**: Use Kafka 3.6+ in KRaft mode (Zookeeper-free)
- **Impact**: Simpler deployment, fewer failure points, future-proof architecture

**ACCEPTED - MinIO AGPL License**:
- **Risk**: AGPL copyleft provisions require source disclosure if MinIO is modified and hosted as a service
- **Mitigation**: Use unmodified MinIO Docker images (standard usage, no AGPL obligations triggered)
- **Impact**: No risk for standard self-hosted deployment; migration to SeaweedFS (Apache 2.0) available if embedding in commercial SaaS

### Constitutional Compliance

No violations of the constitution are anticipated at this stage. All streaming principles are satisfied by the updated stack.
