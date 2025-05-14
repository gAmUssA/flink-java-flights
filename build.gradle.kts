plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.avast.gradle.docker-compose") version "0.16.11"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.7.0"
    id("application")
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
    maven {
        url = uri("https://repository.apache.org/content/repositories/snapshots/")
    }
}

// Define Flink version to use
val flinkVersion = "1.20.1"
val kafkaVersion = "4.0.0"
val avroVersion = "1.11.3"
val confluentVersion = "7.9.1"

dependencies {
    // Flink core dependencies
    implementation("org.apache.flink:flink-streaming-java:${flinkVersion}")
    implementation("org.apache.flink:flink-clients:${flinkVersion}")
    implementation("org.apache.flink:flink-table-api-java-bridge:${flinkVersion}")
    implementation("org.apache.flink:flink-table-planner-loader:${flinkVersion}")
    implementation("org.apache.flink:flink-connector-kafka:3.4.0-1.20")
    
    // Avro and Schema Registry
    implementation("org.apache.flink:flink-avro:${flinkVersion}")
    implementation("org.apache.flink:flink-avro-confluent-registry:${flinkVersion}")
    implementation("org.apache.avro:avro:${avroVersion}")
    implementation("io.confluent:kafka-schema-registry-client:${confluentVersion}")
    
    // Kafka client
    implementation("org.apache.kafka:kafka-clients:${kafkaVersion}")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    
    // Testing
    testImplementation("org.apache.flink:flink-test-utils:${flinkVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("com.example.FlinkFlightStreamingJob")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "com.example.FlinkFlightStreamingJob")
    }
}

tasks.shadowJar {
    archiveBaseName.set("flink-flight-demo")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
}

// Docker Compose configuration
dockerCompose {
    useComposeFiles.set(listOf("docker-compose.yaml"))
    startedServices.set(listOf("kafka", "schema-registry"))
    captureContainersOutput.set(true)
}
