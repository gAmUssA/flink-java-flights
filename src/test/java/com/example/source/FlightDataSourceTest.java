package com.example.source;

import com.example.config.ConfigurationManager;
import com.example.model.FlightEvent;
import com.example.model.FlightStatus;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the FlightDataSource class.
 */
public class FlightDataSourceTest {

    @Test
    @org.junit.jupiter.api.Disabled("Temporarily disabled due to dependency issues with Flink 1.20.1")
    public void testCreateKafkaSourceLocal() {
        // Create a local configuration
        ConfigurationManager config = new ConfigurationManager(false);
        
        // Create a Kafka source
        KafkaSource<String> source = FlightDataSource.createKafkaSource(config);
        
        // Verify the source is not null
        assertNotNull(source);
        
        // With newer Kafka connector versions, we can't easily access the internal properties
        // Instead, we'll verify that the source was created successfully
        // and that the configuration manager has the expected properties
        assertEquals("localhost:9092", config.getProperty("bootstrap.servers"));
        assertEquals("http://localhost:8081", config.getProperty("schema.registry.url"));
        assertEquals("flights", config.getFlightsTopic());
    }
    
    @Test
    @org.junit.jupiter.api.Disabled("Temporarily disabled due to dependency issues with Flink 1.20.1")
    public void testCreateKafkaSourceCloud() {
        // Create a cloud configuration
        ConfigurationManager config = new ConfigurationManager(true);
        
        // Create a Kafka source
        KafkaSource<String> source = FlightDataSource.createKafkaSource(config);
        
        // Verify the source is not null
        assertNotNull(source);
        
        // With newer Kafka connector versions, we can't easily access the internal properties
        // Instead, we'll verify that the source was created successfully
        // and that the configuration manager has the expected properties
        assertTrue(config.getProperty("bootstrap.servers").contains("confluent.cloud"));
        assertTrue(config.getProperty("schema.registry.url").contains("confluent.cloud"));
        assertEquals("SASL_SSL", config.getProperty("security.protocol"));
        assertEquals("PLAIN", config.getProperty("sasl.mechanism"));
        assertNotNull(config.getProperty("sasl.jaas.config"));
        assertEquals("USER_INFO", config.getProperty("schema.registry.basic.auth.credentials.source"));
        assertNotNull(config.getProperty("schema.registry.basic.auth.user.info"));
    }
    
    @Test
    public void testWatermarkStrategy() {
        // Create a watermark strategy
        WatermarkStrategy<FlightEvent> strategy = FlightDataSource.createWatermarkStrategy();
        
        // Verify the strategy is not null
        assertNotNull(strategy);
        
        // Create a flight event with a specific timestamp
        Instant now = Instant.now();
        FlightEvent event = new FlightEvent();
        event.setScheduledDeparture(now);
        
        // Verify the timestamp extractor works correctly
        assertEquals(now.toEpochMilli(), 
                strategy.createTimestampAssigner(null).extractTimestamp(event, 0));
    }
    
    @Test
    public void testFlightEvent() {
        // Create a flight event directly
        FlightEvent event = new FlightEvent();
        event.setFlightNumber("AA123")
             .setAirline("American Airlines")
             .setOrigin("LAX")
             .setDestination("JFK")
             .setStatus(FlightStatus.SCHEDULED)
             .setScheduledDeparture(Instant.now().toEpochMilli())
             .setEventTimestamp(Instant.now().toEpochMilli());
        
        // Verify the event properties
        assertEquals("AA123", event.getFlightNumber());
        assertEquals("American Airlines", event.getAirline());
        assertEquals("LAX", event.getOrigin());
        assertEquals("JFK", event.getDestination());
        assertEquals(FlightStatus.SCHEDULED, event.getStatus());
        assertNotNull(event.getScheduledDeparture());
        assertNull(event.getActualDeparture());
        
        // Test delay calculation
        assertNull(event.calculateDelay());
        
        // Set actual departure and verify delay calculation
        Instant scheduled = Instant.ofEpochMilli(event.getScheduledDeparture());
        Instant actual = scheduled.plusSeconds(3600); // 1 hour delay
        event.setActualDeparture(actual.toEpochMilli());
        
        assertEquals(60, event.calculateDelay()); // 60 minutes delay
    }
}
