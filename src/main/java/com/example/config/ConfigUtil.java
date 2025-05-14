package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Utility class for working with configuration.
 */
public class ConfigUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);
    
    private ConfigUtil() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Creates a ConfigurationManager based on system properties or environment variables.
     * 
     * @return A configured ConfigurationManager
     */
    public static ConfigurationManager createConfigurationManager() {
        boolean useCloudConfig = isCloudMode();
        LOG.info("Creating ConfigurationManager in {} mode", useCloudConfig ? "cloud" : "local");
        return new ConfigurationManager(useCloudConfig);
    }
    
    /**
     * Determines if the application should run in cloud mode based on system properties
     * or environment variables.
     * 
     * @return True if cloud mode should be used, false otherwise
     */
    public static boolean isCloudMode() {
        // Check system property first
        String mode = System.getProperty("config.mode");
        if (mode != null) {
            return "cloud".equalsIgnoreCase(mode);
        }
        
        // Check environment variable
        mode = System.getenv("CONFIG_MODE");
        if (mode != null) {
            return "cloud".equalsIgnoreCase(mode);
        }
        
        // Default to local mode
        return false;
    }
    
    /**
     * Creates a ConfigurationManager with the specified mode.
     * 
     * @param useCloudConfig If true, uses cloud configuration; otherwise uses local configuration
     * @return A configured ConfigurationManager
     */
    public static ConfigurationManager createConfigurationManager(boolean useCloudConfig) {
        LOG.info("Creating ConfigurationManager in {} mode", useCloudConfig ? "cloud" : "local");
        return new ConfigurationManager(useCloudConfig);
    }
    
    /**
     * Gets Flink configuration properties based on the current configuration.
     * 
     * @param config The ConfigurationManager to use
     * @return Flink configuration properties
     */
    public static Properties getFlinkKafkaProperties(ConfigurationManager config) {
        Properties properties = new Properties();
        
        // Copy Kafka consumer properties
        properties.putAll(config.getKafkaConsumerProperties());
        
        // Add Schema Registry properties
        Properties srProps = config.getSchemaRegistryProperties();
        for (String key : srProps.stringPropertyNames()) {
            properties.put(key, srProps.getProperty(key));
        }
        
        return properties;
    }
}
