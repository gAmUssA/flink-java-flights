package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager to handle both local and Confluent Cloud configurations.
 * Loads properties from either local.properties or cloud.properties files.
 */
public class ConfigurationManager {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManager.class);
    
    private static final String LOCAL_CONFIG_FILE = "local.properties";
    private static final String CLOUD_CONFIG_FILE = "cloud.properties";
    
    private final Properties properties = new Properties();
    private final boolean isCloudMode;
    
    /**
     * Creates a new ConfigurationManager.
     * 
     * @param useCloudConfig If true, loads cloud configuration; otherwise loads local configuration
     */
    public ConfigurationManager(boolean useCloudConfig) {
        this.isCloudMode = useCloudConfig;
        loadProperties();
    }
    
    /**
     * Loads properties from the appropriate configuration file.
     */
    private void loadProperties() {
        String configFile = isCloudMode ? CLOUD_CONFIG_FILE : LOCAL_CONFIG_FILE;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (input == null) {
                LOG.error("Unable to find {}", configFile);
                throw new RuntimeException("Configuration file not found: " + configFile);
            }
            properties.load(input);
            LOG.info("Loaded configuration from {}", configFile);
        } catch (IOException e) {
            LOG.error("Error loading properties: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
    
    /**
     * Gets a property value.
     * 
     * @param key Property key
     * @return Property value or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Gets a property value with a default.
     * 
     * @param key Property key
     * @param defaultValue Default value if property is not found
     * @return Property value or default if not found
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Gets all properties.
     * 
     * @return All properties
     */
    public Properties getProperties() {
        return new Properties(properties);
    }
    
    /**
     * Gets Kafka consumer properties based on the current configuration mode.
     * 
     * @return Kafka consumer properties
     */
    public Properties getKafkaConsumerProperties() {
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers", getProperty("bootstrap.servers"));
        
        // Common properties
        kafkaProps.put("group.id", "flink-flight-consumer");
        kafkaProps.put("auto.offset.reset", "earliest");
        kafkaProps.put("enable.auto.commit", "false");
        
        // Add cloud-specific properties if in cloud mode
        if (isCloudMode) {
            kafkaProps.put("security.protocol", getProperty("security.protocol"));
            kafkaProps.put("sasl.mechanism", getProperty("sasl.mechanism"));
            kafkaProps.put("sasl.jaas.config", getProperty("sasl.jaas.config"));
        }
        
        return kafkaProps;
    }
    
    /**
     * Gets Kafka producer properties based on the current configuration mode.
     * 
     * @return Kafka producer properties
     */
    public Properties getKafkaProducerProperties() {
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers", getProperty("bootstrap.servers"));
        
        // Common properties
        kafkaProps.put("acks", isCloudMode ? getProperty("acks", "all") : "all");
        kafkaProps.put("retries", "3");
        
        // Add cloud-specific properties if in cloud mode
        if (isCloudMode) {
            kafkaProps.put("security.protocol", getProperty("security.protocol"));
            kafkaProps.put("sasl.mechanism", getProperty("sasl.mechanism"));
            kafkaProps.put("sasl.jaas.config", getProperty("sasl.jaas.config"));
        }
        
        return kafkaProps;
    }
    
    /**
     * Gets Schema Registry properties based on the current configuration mode.
     * 
     * @return Schema Registry properties
     */
    public Properties getSchemaRegistryProperties() {
        Properties srProps = new Properties();
        srProps.put("schema.registry.url", getProperty("schema.registry.url"));
        
        if (isCloudMode) {
            srProps.put("basic.auth.credentials.source", 
                    getProperty("schema.registry.basic.auth.credentials.source"));
            srProps.put("basic.auth.user.info", 
                    getProperty("schema.registry.basic.auth.user.info"));
        }
        
        return srProps;
    }
    
    /**
     * Gets the topic name for flight events.
     * 
     * @return Flight events topic name
     */
    public String getFlightsTopic() {
        return getProperty("topic.flights", "flights");
    }
    
    /**
     * Gets the topic name for flight alerts.
     * 
     * @return Flight alerts topic name
     */
    public String getFlightAlertsTopic() {
        return getProperty("topic.flight_alerts", "flight_alerts");
    }
    
    /**
     * Checks if the configuration is in cloud mode.
     * 
     * @return True if in cloud mode, false if in local mode
     */
    public boolean isCloudMode() {
        return isCloudMode;
    }
}
