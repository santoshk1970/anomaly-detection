# Tasks: Real-Time Smart Grid Anomaly Detection

**Input**: Design documents from `/specs/001-grid-anomaly-detection/`
**Prerequisites**: plan.md (Java/Scala Flink architecture), spec.md (user stories P1-P3)

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create root docker-compose.yml file in repository root
- [X] T002 [P] Create directory structure: .docker/, data-generator/, stream-processor/, dashboards/
- [X] T003 [P] Initialize Python project in data-generator/ with pyproject.toml for Poetry
- [X] T004 [P] Initialize Java Maven project in stream-processor/ with pom.xml (or Gradle with build.gradle)
- [X] T005 [P] Create Dockerfile for data-generator/ (Python 3.11 base image)
- [X] T006 [P] Create Dockerfile for stream-processor/ (Java 17 + Flink base image)
- [X] T007 [P] Create .dockerignore files for data-generator/ and stream-processor/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core streaming infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T010 [P] Add Kafka service (KRaft mode, no Zookeeper) to docker-compose.yml with configuration in .docker/kafka/
- [ ] T011 [P] Add Apicurio Schema Registry service to docker-compose.yml with configuration in .docker/schema-registry/
- [ ] T012 [P] Add Flink (JobManager and TaskManager) services to docker-compose.yml with configuration in .docker/flink/
- [ ] T013 [P] Add Prometheus service to docker-compose.yml with base prometheus.yml config in .docker/prometheus/
- [ ] T014 [P] Add Grafana service to docker-compose.yml with provisioning config in .docker/grafana/
- [ ] T015 [P] Add InfluxDB 2.x OSS service to docker-compose.yml with configuration in .docker/influxdb/
- [ ] T016 [P] Add MinIO service to docker-compose.yml with configuration in .docker/minio/
- [ ] T017 Verify all services start up correctly via docker-compose up and check service health endpoints
- [ ] T018 [P] Create Kafka topics: sensor-readings, anomalies, dead-letter-queue using Kafka CLI or init script
- [ ] T019 [P] Configure Flink checkpointing parameters in .docker/flink/flink-conf.yaml (frequency: 10s or 10k events)
- [ ] T020 [P] Create MinIO bucket for data archival with date partitioning configuration

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Real-Time Anomaly Alerting (Priority: P1) 🎯 MVP

**Goal**: Ingest sensor data stream, detect anomalies using statistical analysis, and publish to Kafka topic and InfluxDB

**Independent Test**: Inject simulated anomaly event (voltage > 2.5σ from moving avg) into sensor-readings topic and verify anomaly appears in both anomalies topic and InfluxDB within 250ms

### Schema Definition for User Story 1

- [ ] T100 [P] [US1] Define SensorReading Avro schema in specs/001-grid-anomaly-detection/contracts/sensor-reading.avsc with fields: meter_id, timestamp, voltage, current, temperature
- [ ] T101 [P] [US1] Define AnomalyEvent Avro schema in specs/001-grid-anomaly-detection/contracts/anomaly-event.avsc with fields: anomaly_type, severity (enum: INFO/WARNING/ERROR/CRITICAL), original_event, threshold_used, deviation
- [ ] T102 [US1] Register SensorReading and AnomalyEvent schemas with Apicurio Schema Registry via REST API or CLI

### Data Generator for User Story 1

- [ ] T110 [P] [US1] Implement sensor data generator in data-generator/src/generator.py to produce simulated SensorReading events with configurable anomaly injection rate
- [ ] T111 [US1] Add Kafka producer logic in data-generator/src/generator.py using confluent-kafka-python with Avro serialization
- [ ] T112 [US1] Add CLI arguments for data-generator: --event-rate, --anomaly-rate, --duration, --kafka-bootstrap-servers
- [ ] T113 [P] [US1] Add pytest test in data-generator/tests/test_generator.py to validate schema compliance

### Flink Stream Processor for User Story 1

- [ ] T120 [P] [US1] Create AnomalyDetectionJob.java main class in stream-processor/src/main/java/com/grid/AnomalyDetectionJob.java with Flink StreamExecutionEnvironment setup
- [ ] T121 [US1] Add Kafka source configuration in AnomalyDetectionJob.java to consume from sensor-readings topic with Avro deserialization using Apicurio registry
- [ ] T122 [P] [US1] Implement MovingAverageFunction.java in stream-processor/src/main/java/com/grid/functions/MovingAverageFunction.java using Flink KeyedProcessFunction with 5-minute sliding window state
- [ ] T123 [P] [US1] Implement AnomalyDetectorFunction.java in stream-processor/src/main/java/com/grid/functions/AnomalyDetectorFunction.java to detect readings > configurable threshold (default: 2.5σ, range: 1.5-4.0σ)
- [ ] T124 [US1] Add keyed state management in MovingAverageFunction.java for per-meter_id moving statistics using ValueState
- [ ] T125 [US1] Implement equipment failure detection logic in AnomalyDetectorFunction.java for status field anomalies
- [ ] T126 [US1] Configure Flink checkpointing in AnomalyDetectionJob.java (every 10 seconds or 10k events, exactly-once mode)
- [ ] T127 [P] [US1] Implement dual-sink output: Kafka topic anomalies and InfluxDB in AnomalyDetectionJob.java using Flink SinkFunction or async I/O
- [ ] T128 [P] [US1] Add error handling and dead-letter queue routing in AnomalyDetectionJob.java for malformed events (FR-007)
- [ ] T129 [P] [US1] Implement exponential backoff retry logic in InfluxDB sink (initial: 100ms, max: 30s, retries: 3) per FR-008
- [ ] T130 [P] [US1] Add Prometheus metrics instrumentation in AnomalyDetectionJob.java for throughput, latency, error rates using Flink MetricGroup
- [ ] T131 [US1] Package Flink job as JAR using mvn clean package and deploy to Flink cluster via docker-compose

### Testing for User Story 1

- [ ] T140 [P] [US1] Create JUnit 5 test in stream-processor/src/test/java/com/grid/AnomalyDetectionJobTest.java using Flink MiniCluster for end-to-end pipeline test
- [ ] T141 [P] [US1] Add performance test to validate SR-001 (10k events/sec/core throughput) using Flink TestHarness
- [ ] T142 [P] [US1] Add latency test to validate SR-006 (p99 < 100ms) under simulated load

**Checkpoint**: User Story 1 is functional. Anomalies are being detected and written to Kafka topic and InfluxDB.

---

## Phase 4: User Story 2 - Pipeline Health Dashboard (Priority: P2)

**Goal**: Provide real-time observability into pipeline health via Grafana dashboard showing ingestion rate, latency, error counts, anomaly counts

**Independent Test**: Run pipeline under simulated 5k events/sec load and verify dashboard displays correct metrics for ingestion throughput, p99 latency, and anomaly counts with 5-second refresh

### Implementation for User Story 2

- [ ] T200 [P] [US2] Configure Flink to expose Prometheus metrics endpoint in .docker/flink/flink-conf.yaml (metrics.reporter.prom.class: PrometheusReporter)
- [ ] T201 [P] [US2] Configure Prometheus to scrape Flink metrics in .docker/prometheus/prometheus.yml (add Flink JobManager/TaskManager targets)
- [ ] T202 [P] [US2] Add Kafka exporter service to docker-compose.yml for Kafka broker metrics (ingestion rate, consumer lag)
- [ ] T203 [P] [US2] Configure Prometheus to scrape Kafka exporter and InfluxDB metrics in prometheus.yml
- [ ] T204 [US2] Create Grafana dashboard JSON in dashboards/grafana-provisioning/dashboards/pipeline-health.json with datasource configuration
- [ ] T205 [P] [US2] Add Grafana panel for Kafka ingestion rate (events/sec) querying Prometheus kafka_topic_partition_current_offset rate
- [ ] T206 [P] [US2] Add Grafana panel for Flink processing latency (p99) querying Prometheus flink_taskmanager_job_latency_source_to_sink_p99
- [ ] T207 [P] [US2] Add Grafana panel for events processed count querying Prometheus flink_taskmanager_job_task_numRecordsIn
- [ ] T208 [P] [US2] Add Grafana panel for error rates querying Prometheus flink_taskmanager_job_task_numRecordsOutErrors
- [ ] T209 [US2] Add Grafana panel for anomaly count over time querying InfluxDB measurement anomalies with time-series aggregation
- [ ] T210 [US2] Add Grafana panel for dead-letter queue message count querying Kafka anomalies topic offset lag
- [ ] T211 [US2] Configure Grafana dashboard auto-refresh to 5 seconds in pipeline-health.json

**Checkpoint**: Pipeline health dashboard is live in Grafana showing real-time metrics.

---

## Phase 5: User Story 3 - Historical Data Archiving (Priority: P3)

**Goal**: Archive all processed sensor readings to MinIO object store in Parquet format with date partitioning for long-term analysis

**Independent Test**: Run pipeline for 10 minutes processing 100k events, then query MinIO bucket to verify data exists in Parquet format partitioned by date (year=YYYY/month=MM/day=DD)

### Implementation for User Story 3

- [ ] T300 [P] [US3] Add Flink S3 (MinIO) sink dependency to stream-processor/pom.xml (flink-connector-aws-kinesis-streams or flink-s3-fs-hadoop)
- [ ] T301 [US3] Implement S3ParquetSink.java in stream-processor/src/main/java/com/grid/sinks/S3ParquetSink.java using Flink FileSink with Parquet writer
- [ ] T302 [US3] Configure MinIO endpoint and credentials in AnomalyDetectionJob.java environment variables (AWS_ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
- [ ] T303 [US3] Add partitioning logic in S3ParquetSink.java to partition by date using Flink BucketAssigner (format: year=YYYY/month=MM/day=DD)
- [ ] T304 [US3] Connect S3ParquetSink to sensor-readings stream in AnomalyDetectionJob.java as parallel sink alongside anomaly detection
- [ ] T305 [US3] Configure compaction policy for Parquet files in S3ParquetSink.java (rolling policy: size=128MB or time=15min)
- [ ] T306 [P] [US3] Add JUnit test in stream-processor/src/test/java/com/grid/sinks/S3ParquetSinkTest.java to validate partitioning and Parquet format

**Checkpoint**: All sensor data is being archived to MinIO in Parquet format with date partitioning.

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Documentation, testing, and operational readiness improvements

- [ ] T900 [P] Create quickstart.md in specs/001-grid-anomaly-detection/ with instructions to launch system via docker-compose and validate end-to-end flow
- [ ] T901 [P] Add integration test in data-generator/tests/test_integration.py to validate full pipeline: generate event → verify in InfluxDB and MinIO
- [ ] T902 [P] Document SensorReading and AnomalyEvent Avro schemas in specs/001-grid-anomaly-detection/contracts/ with example payloads
- [ ] T903 [P] Create data-model.md in specs/001-grid-anomaly-detection/ documenting SensorReading and AnomalyEvent entities with field types, constraints, and severity enum
- [ ] T904 [P] Add performance benchmark script in stream-processor/scripts/benchmark.sh to run SC-001 test (10k events/sec for 1 hour)
- [ ] T905 [P] Add disaster recovery test script to validate SC-004 (60s RTO, RPO=0) by killing Flink TaskManager and verifying checkpoint recovery
- [ ] T906 [P] Add backpressure validation test to verify SR-002 (producer blocks when consumer lags) using Flink backpressure monitoring API
- [ ] T907 Code cleanup: add logging, comments, and JavaDoc documentation to all Java classes in stream-processor/src/
- [ ] T908 Security hardening: review MinIO access policies, Kafka SASL/SSL configuration, and schema registry authentication

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-5)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Phase N)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Independent of US1 but benefits from US1 metrics
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Independent of US1/US2, archives same sensor-readings stream

### Within Each User Story

- **US1**: Schema definition → Data generator + Flink processor (parallel) → Testing
- **US2**: Metrics configuration (parallel tasks) → Dashboard creation → Validation
- **US3**: S3 sink implementation → Configuration → Integration with main job

### Parallel Opportunities

- All Setup tasks (T002-T007) marked [P] can run in parallel
- All Foundational tasks (T010-T020) marked [P] can run in parallel within Phase 2
- Once Foundational phase completes, all user stories (US1, US2, US3) can start in parallel if team capacity allows
- Within US1: Schema tasks (T100-T101), generator (T110), and Flink functions (T122-T123) can run in parallel
- Within US2: All dashboard panel tasks (T205-T210) can run in parallel after datasource config
- Polish tasks (T900-T908) can mostly run in parallel

---

## Parallel Example: User Story 1

```bash
# After schemas are registered (T102), launch in parallel:
Task T110: "Implement sensor data generator in data-generator/src/generator.py"
Task T120: "Create AnomalyDetectionJob.java main class"
Task T122: "Implement MovingAverageFunction.java"
Task T123: "Implement AnomalyDetectorFunction.java"

# After core Flink job is complete (T126), launch in parallel:
Task T127: "Implement dual-sink output (Kafka + InfluxDB)"
Task T128: "Add error handling and DLQ routing"
Task T129: "Implement exponential backoff retry logic"
Task T130: "Add Prometheus metrics instrumentation"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T007)
2. Complete Phase 2: Foundational (T010-T020) - CRITICAL blocking phase
3. Complete Phase 3: User Story 1 (T100-T142)
4. **STOP and VALIDATE**: Run integration test - inject anomaly, verify detection in <250ms
5. Deploy/demo MVP

**MVP Deliverable**: Real-time anomaly detection system processing 10k events/sec with anomaly alerts

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP: Anomaly detection live)
3. Add User Story 2 → Test independently → Deploy/Demo (Observability added)
4. Add User Story 3 → Test independently → Deploy/Demo (Data lake archival added)
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers after Foundational phase completes:

1. Team completes Setup (Phase 1) + Foundational (Phase 2) together
2. Once Foundational is done:
   - **Developer A**: User Story 1 (Anomaly detection - highest priority)
   - **Developer B**: User Story 2 (Dashboard - can mock metrics initially)
   - **Developer C**: User Story 3 (Archival - independent stream sink)
3. Stories complete and integrate independently without blocking each other

---

## Task Count Summary

- **Phase 1 (Setup)**: 7 tasks
- **Phase 2 (Foundational)**: 11 tasks
- **Phase 3 (User Story 1)**: 33 tasks (13 schema/generator, 12 Flink processor, 8 sinks/error handling, 3 tests)
- **Phase 4 (User Story 2)**: 12 tasks (metrics config + dashboard panels)
- **Phase 5 (User Story 3)**: 7 tasks (S3/MinIO archival sink)
- **Phase N (Polish)**: 9 tasks (documentation, testing, hardening)

**Total Tasks**: 79

**Parallel Opportunities**: 48 tasks marked [P] can run concurrently (within phase constraints)

**Independent Test Criteria**:
- **US1**: Inject anomaly → verify in Kafka topic + InfluxDB < 250ms
- **US2**: Run 5k events/sec → verify dashboard shows correct metrics
- **US3**: Process 100k events → verify Parquet files exist in MinIO with date partitions

**Suggested MVP Scope**: Phase 1 + Phase 2 + Phase 3 (User Story 1 only) = 51 tasks

---

## Notes

- [P] tasks = different files/components, no direct dependencies within phase
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable
- Java/Scala chosen for Flink to ensure production maturity and <100ms p99 latency (SR-006)
- Kafka KRaft mode eliminates Zookeeper complexity
- Apicurio Schema Registry ensures Apache 2.0 licensing and portability
- Commit after each task or logical group of related tasks
- Stop at any checkpoint to validate story independently before proceeding
