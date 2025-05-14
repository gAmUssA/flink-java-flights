package com.example.config;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ConfigurationManager class.
 */
public class ConfigurationManagerTest {

    @Test
    public void testLocalConfiguration() {
        ConfigurationManager config = new ConfigurationManager(false);
        
        // Test basic properties
        assertEquals("localhost:9092", config.getProperty("bootstrap.servers"));
        assertEquals("http://localhost:8081", config.getProperty("schema.registry.url"));
        assertEquals("flights", config.getProperty("topic.flights"));
        assertEquals("flight_alerts", config.getProperty("topic.flight_alerts"));
        assertEquals("local", config.getProperty("environment"));
        
        // Test default values
        assertEquals("default", config.getProperty("non.existent.property", "default"));
        
        // Test Kafka consumer properties
        Properties consumerProps = config.getKafkaConsumerProperties();
        assertEquals("localhost:9092", consumerProps.getProperty("bootstrap.servers"));
        assertEquals("flink-flight-consumer", consumerProps.getProperty("group.id"));
        assertEquals("earliest", consumerProps.getProperty("auto.offset.reset"));
        assertEquals("false", consumerProps.getProperty("enable.auto.commit"));
        
        // Test Kafka producer properties
        Properties producerProps = config.getKafkaProducerProperties();
        assertEquals("localhost:9092", producerProps.getProperty("bootstrap.servers"));
        assertEquals("all", producerProps.getProperty("acks"));
        assertEquals("3", producerProps.getProperty("retries"));
        
        // Test Schema Registry properties
        Properties srProps = config.getSchemaRegistryProperties();
        assertEquals("http://localhost:8081", srProps.getProperty("schema.registry.url"));
        
        // Test topic names
        assertEquals("flights", config.getFlightsTopic());
        assertEquals("flight_alerts", config.getFlightAlertsTopic());
        
        // Test mode
        assertFalse(config.isCloudMode());
    }

    @Test
    public void testCloudConfiguration() {
        ConfigurationManager config = new ConfigurationManager(true);
        
        // Test basic properties
        assertTrue(config.getProperty("bootstrap.servers").contains("confluent.cloud"));
        assertTrue(config.getProperty("schema.registry.url").contains("confluent.cloud"));
        assertEquals("cloud", config.getProperty("environment"));
        
        // Test Kafka consumer properties
        Properties consumerProps = config.getKafkaConsumerProperties();
        assertTrue(consumerProps.getProperty("bootstrap.servers").contains("confluent.cloud"));
        assertEquals("SASL_SSL", consumerProps.getProperty("security.protocol"));
        assertEquals("PLAIN", consumerProps.getProperty("sasl.mechanism"));
        assertNotNull(consumerProps.getProperty("sasl.jaas.config"));
        
        // Test Kafka producer properties
        Properties producerProps = config.getKafkaProducerProperties();
        assertTrue(producerProps.getProperty("bootstrap.servers").contains("confluent.cloud"));
        assertEquals("all", producerProps.getProperty("acks"));
        assertEquals("SASL_SSL", producerProps.getProperty("security.protocol"));
        
        // Test Schema Registry properties
        Properties srProps = config.getSchemaRegistryProperties();
        assertTrue(srProps.getProperty("schema.registry.url").contains("confluent.cloud"));
        assertEquals("USER_INFO", srProps.getProperty("basic.auth.credentials.source"));
        assertNotNull(srProps.getProperty("basic.auth.user.info"));
        
        // Test mode
        assertTrue(config.isCloudMode());
    }
    
    @Test
    public void testUtilityMethods() {
        // Test local config creation
        ConfigurationManager localConfig = ConfigUtil.createConfigurationManager(false);
        assertFalse(localConfig.isCloudMode());
        assertEquals("localhost:9092", localConfig.getProperty("bootstrap.servers"));
        
        // Test Flink Kafka properties
        Properties flinkProps = ConfigUtil.getFlinkKafkaProperties(localConfig);
        assertEquals("localhost:9092", flinkProps.getProperty("bootstrap.servers"));
        assertEquals("http://localhost:8081", flinkProps.getProperty("schema.registry.url"));
    }
}
