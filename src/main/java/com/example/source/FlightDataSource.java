package com.example.source;

import com.example.config.ConfigurationManager;
import com.example.model.FlightEvent;
import com.example.model.FlightStatus;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

/**
 * Utility class for creating Kafka sources for flight data.
 */
public class FlightDataSource {
    private static final Logger LOG = LoggerFactory.getLogger(FlightDataSource.class);
    
    private FlightDataSource() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Creates a Kafka source for flight events.
     *
     * @param config The configuration manager
     * @return A configured Kafka source for flight events
     */
    public static KafkaSource<String> createKafkaSource(ConfigurationManager config) {
        String topic = config.getFlightsTopic();
        String bootstrapServers = config.getProperty("bootstrap.servers");
        
        LOG.info("Creating Kafka source for topic {} with bootstrap servers {}", 
                topic, bootstrapServers);
        
        // Create a Kafka source
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId("flink-flight-processor")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(new SimpleStringSchema()))
                .build();
        
        // We need to create a new builder with cloud properties if in cloud mode
        if (config.isCloudMode()) {
            LOG.info("Configuring Kafka source for cloud mode");
            Properties properties = new Properties();
            properties.setProperty("security.protocol", config.getProperty("security.protocol"));
            properties.setProperty("sasl.mechanism", config.getProperty("sasl.mechanism"));
            properties.setProperty("sasl.jaas.config", config.getProperty("sasl.jaas.config"));
            
            // Schema Registry auth for Confluent Cloud
            String credentialsSource = config.getProperty("schema.registry.basic.auth.credentials.source");
            String userInfo = config.getProperty("schema.registry.basic.auth.user.info");
            if (credentialsSource != null && userInfo != null) {
                properties.setProperty("schema.registry.basic.auth.credentials.source", credentialsSource);
                properties.setProperty("schema.registry.basic.auth.user.info", userInfo);
            }
            
            // Create a new source with the properties
            source = KafkaSource.<String>builder()
                    .setBootstrapServers(bootstrapServers)
                    .setTopics(topic)
                    .setGroupId("flink-flight-processor")
                    .setStartingOffsets(OffsetsInitializer.earliest())
                    .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(new SimpleStringSchema()))
                    .setProperties(properties)
                    .build();
        }
        
        return source;
    }
    
    /**
     * Creates a DataStream of FlightEvents from a Kafka source.
     *
     * @param env The StreamExecutionEnvironment
     * @param config The configuration manager
     * @return A DataStream of FlightEvents
     */
    public static DataStream<FlightEvent> createFlightEventStream(
            StreamExecutionEnvironment env, ConfigurationManager config) {
        
        // Create a Kafka source
        KafkaSource<String> source = createKafkaSource(config);
        
        // Create a DataStream from the Kafka source
        return env.fromSource(source, WatermarkStrategy.noWatermarks(), "kafka-source")
                .map(json -> parseFlightEvent(json))
                .name("flight-events-source")
                .uid("flight-events-source-uid");
    }
    
    /**
     * Creates a watermark strategy for flight events.
     * This strategy uses the scheduled departure time as the event timestamp.
     *
     * @return A watermark strategy for flight events
     */
    public static WatermarkStrategy<FlightEvent> createWatermarkStrategy() {
        return WatermarkStrategy
                .<FlightEvent>forBoundedOutOfOrderness(Duration.ofMinutes(5))
                .withTimestampAssigner((event, timestamp) -> event.getScheduledDeparture());
    }
    
    /**
     * Parses a JSON string into a FlightEvent.
     * For demonstration purposes, this creates a dummy FlightEvent.
     *
     * @param json The JSON string to parse
     * @return A FlightEvent
     */
    public static FlightEvent parseFlightEvent(String json) {
        // This is just a placeholder implementation
        // In a real implementation, this would parse the JSON string to a FlightEvent
        FlightEvent event = new FlightEvent();
        event.setFlightNumber("AA123")
                .setAirline("American Airlines")
                .setOrigin("LAX")
                .setDestination("JFK")
                .setStatus(FlightStatus.SCHEDULED)
                .setScheduledDeparture(System.currentTimeMillis())
                .setEventTimestamp(System.currentTimeMillis());
        return event;
    }
}
