# Flink Java Flights Demo

A demonstration project showcasing Apache Flink 2.0 reading from Apache Kafka 4.0 to process real-time flight event data.

## Overview

This demo targets Java developers familiar with Kafka, Avro, and Schema Registry. It simulates a real-world flight data processing use case using an Avro schema for flight events. The demo is designed to be run locally via Docker and fits into a 45-minute conference breakout session.

## Key Features

- End-to-end streaming from Kafka (Avro+Schema Registry) to Flink to observable sinks
- DataStream API for low-level event processing
- Table API/Flink SQL for declarative querying
- Stateful processing for tracking and flagging delayed flights
- Local deployment with Docker Compose

## Prerequisites

- Java 17+
- Docker and Docker Compose
- Gradle

## Project Structure

```
flink-java-flights/
├── src/
│   ├── main/
│   │   ├── java/        # Java source code
│   │   └── resources/   # Configuration files
│   └── test/
│       └── java/        # Test code
├── build.gradle.kts     # Gradle build configuration
├── docker-compose.yaml  # Docker Compose configuration
└── Makefile            # Helper commands
```

## Getting Started

1. Clone the repository
2. Start the infrastructure:
   ```
   make docker-up
   ```
3. Build the project:
   ```
   make build
   ```
4. Run the application:
   ```
   make run
   ```
5. Stop the infrastructure:
   ```
   make docker-down
   ```

## Development

This project uses Gradle with Kotlin DSL for build configuration. The main components are:

- Kafka 4.0 in KRaft mode (no Zookeeper)
- Schema Registry for Avro schemas
- Flink 2.0 for stream processing

## License

[Your license here]
