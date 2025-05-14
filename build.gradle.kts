plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.avast.gradle.docker-compose") version "0.16.11"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
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
val flinkVersion = "2.0.0"
val kafkaVersion = "4.0.0"
val avroVersion = "1.11.3"
val confluentVersion = "7.9.1"

dependencies {
    // Flink core dependencies
    implementation("org.apache.flink:flink-streaming-java:${flinkVersion}")
    implementation("org.apache.flink:flink-clients:${flinkVersion}")
    implementation("org.apache.flink:flink-table-api-java-bridge:${flinkVersion}")
    implementation("org.apache.flink:flink-table-planner-loader:${flinkVersion}")
    implementation("org.apache.flink:flink-connector-kafka:4.0.0-2.0")
    
    // Avro and Schema Registry
    implementation("org.apache.flink:flink-avro:${flinkVersion}")
    implementation("org.apache.flink:flink-avro-confluent-registry:${flinkVersion}")
    implementation("org.apache.avro:avro:${avroVersion}")
    implementation("io.confluent:kafka-schema-registry-client:${confluentVersion}")
    
    // Kafka client
    implementation("org.apache.kafka:kafka-clients:${kafkaVersion}")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    
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
    
    // Define application for running different main classes
    applicationDefaultJvmArgs = listOf("-Dlog4j.configurationFile=logback.xml")
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

// Avro configuration
avro {
    stringType.set("String")
    fieldVisibility.set("PRIVATE")
    outputCharacterEncoding.set("UTF-8")
}

// Copy Avro schema files to resources
tasks.register<Copy>("copyAvroSchemas") {
    from("src/main/avro")
    into("src/main/resources/avro")
    include("**/*.avsc")
}

tasks.named("processResources") {
    dependsOn("copyAvroSchemas")
}

// Create resources directory in test
tasks.register<Copy>("copyAvroSchemasToTest") {
    from("src/main/avro")
    into("src/test/resources/avro")
    include("**/*.avsc")
}

tasks.named("processTestResources") {
    dependsOn("copyAvroSchemasToTest")
}
