# Flink Flight Data Streaming Demo – Product Requirements Document (PRD)

## Overview

This PRD outlines a demo project showcasing **Apache Flink 2.0** reading from **Apache Kafka 4.0** to process real-time flight event data. The demo targets Java developers (familiar with Kafka, Avro, and Schema Registry) and will simulate a real-world flight data processing use case using the provided **`flight.avsc`** Avro schema. It is designed to be run locally (via Docker) and fit into a 45-minute conference breakout session. The demo will highlight key Flink streaming concepts:

* The DataStream API for efficient event processing
* The Table API/Flink SQL for declarative querying
* Testing best practices
* Stateful stream processing

## Goals

* **Demonstrate end-to-end streaming** from Kafka (Avro+Schema Registry) to Flink to observable sinks
* **Illustrate Flink’s APIs**: DataStream API (low-level) and Table API/Flink SQL (declarative)
* **Stateful processing & analytics**: Tracking and flagging delayed flights
* **Best practices**: Event time, schema registry, testing
* **Easy local deployment**: Docker Compose, optional Confluent Cloud
* **Educational value**: Clear, relatable use case; observable results; code examples for each core Flink concept

## Non-Goals

* Production-grade deployment (HA, security, fault tolerance)
* UI/dashboard frontend development
* Deep Kafka or Schema Registry introduction
* Comprehensive Flink 2.0 coverage (we'll use stable core APIs only)

## Architecture Overview

* **Kafka 4.0** as source (KRaft mode, Avro-encoded flight events)
* **Flink 2.0** application (running locally)

  * DataStream API: stateful delay detection
  * Table API/Flink SQL: aggregation queries (e.g., delay per airline)
* **Schema Registry** for Avro decoding
* **Sinks**: Kafka topics (e.g., `flight_alerts`), logs, optional file or console outputs
* **Deployment**: Docker Compose for Kafka + Schema Registry; Flink job runs from local IDE or JAR

## Functional Requirements

### Data Ingestion

* Kafka topic `flights` with Avro messages conforming to `flight.avsc`
* Schema Registry integration (Confluent format)
* Sample data generation tool (mock Kafka Avro producer)

### Stream Processing

**DataStream API Branch**

* keyBy flightNumber
* Stateful logic: register timer at scheduledDeparture
* If actualDeparture is not seen by timer time → delayed alert
* Calculate delay = actual - scheduled
* Output to log or Kafka `flight_alerts`

**Table API / SQL Branch**

* Create table over Kafka+Avro with Schema Registry
* SQL: COUNT flights per airline, AVG delay
* Output to console or Kafka sink

### Output / Observability

* Log important events (on-time/departed/delayed)
* Console output for SQL result stream
* Optional: Kafka sinks, Flink metrics, Grafana+Prometheus

### Testing & Quality

* Unit tests for delay logic
* Integration test with MiniCluster
* Use Gradle (`build.gradle.kts`) with Java 17+

## Dataset and Schema

* Provided `flight.avsc` schema with fields:

  * `flightNumber`, `airline`, `origin`, `destination`, `scheduledDeparture`, `actualDeparture`, `status`
* Mock data simulating multiple airlines, statuses, delays
* Events: scheduled, departed, canceled (some with missing actualDeparture)

## Technologies Used

* **Apache Flink 2.0** (DataStream, Table, SQL)
* **Kafka 4.0** (KRaft mode)
* **Confluent Schema Registry** (for Avro)
* **Gradle (Kotlin DSL)** for builds
* **Avro** for data serialization
* **Docker + Docker Compose** for infra
* **Java 17+**
* **cloud.properties** provided for Confluent Cloud integration (SASL\_SSL)

```
acks=all
bootstrap.servers=pkc-921jm.us-east-2.aws.confluent.cloud:9092
client.dns.lookup=use_all_dns_ips
environment=cloud
flink.api.key=TXG2N2X2OYIZM72N
flink.api.secret=Xhc8Bm1PQguZm39b2pT/hZDeGkK0ntFslofdZd8k+T6oHEFxVQQNtq2nLcYF3p6c
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username='QBMMO7KA5HMBSTGD' password='nSkeaZbHAV5whKz7QKNzuBC+VyGE5aQ+1leACXYivKXGtIsYOLzqLOhViCCGEUDc';
sasl.mechanism=PLAIN
schema.registry.basic.auth.credentials.source=USER_INFO
schema.registry.basic.auth.user.info=SUHRFKNHKOGKIAQQ:4RbKsEzTU74eeATRpXbpX8KTKH5p40LoXiIfzOSDtV03ZkzfP5vC51iuL0CI7+Y7
schema.registry.url=https://psrc-l622j.us-east-2.aws.confluent.cloud
security.protocol=SASL_SSL
session.timeout.ms=45000
topic.name=flights

```

## Developer Experience Goals

* Easy `docker-compose up`
* Run Flink job via `gradle run` or packaged fat JAR
* Modular, well-commented Java code
* Separate pipelines for DataStream and SQL
* Testable (unit + integration)
* Configurable for local or Confluent Cloud via `cloud.properties`

## Optional Enhancements

* Live dashboard (Grafana)
* CEP patterns for flight state transitions
* Schema evolution demo
* Real flight API integration
* Kubernetes/Flink Operator deployment
* Iceberg/Pinot/Paimon sink for historical queries

---

This PRD provides a structured and complete foundation for building a conference-grade Flink demo focused on real-time flight event processing using familiar tools for seasoned JVM developers.
