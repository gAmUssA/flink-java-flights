package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Command-line tool for testing and demonstrating configuration management.
 */
public class ConfigurationTool {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationTool.class);

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: ConfigurationTool <local|cloud>");
            System.exit(1);
        }

        String mode = args[0].toLowerCase();
        boolean useCloudConfig = "cloud".equals(mode);
        
        try {
            ConfigurationManager config = new ConfigurationManager(useCloudConfig);
            LOG.info("Configuration loaded in {} mode", useCloudConfig ? "cloud" : "local");
            
            // Display basic configuration
            System.out.println("\n=== Basic Configuration ===");
            System.out.println("Mode: " + (useCloudConfig ? "Cloud" : "Local"));
            System.out.println("Bootstrap Servers: " + config.getProperty("bootstrap.servers"));
            System.out.println("Schema Registry URL: " + config.getProperty("schema.registry.url"));
            System.out.println("Flights Topic: " + config.getFlightsTopic());
            System.out.println("Flight Alerts Topic: " + config.getFlightAlertsTopic());
            
            // Display Kafka consumer properties
            System.out.println("\n=== Kafka Consumer Properties ===");
            Properties consumerProps = config.getKafkaConsumerProperties();
            consumerProps.stringPropertyNames().stream()
                .sorted()
                .forEach(key -> System.out.println(key + ": " + 
                    (key.contains("password") || key.contains("secret") ? "********" : consumerProps.getProperty(key))));
            
            // Display Schema Registry properties
            System.out.println("\n=== Schema Registry Properties ===");
            Properties srProps = config.getSchemaRegistryProperties();
            srProps.stringPropertyNames().stream()
                .sorted()
                .forEach(key -> System.out.println(key + ": " + 
                    (key.contains("auth.user.info") ? "********" : srProps.getProperty(key))));
            
            LOG.info("Configuration display complete");
        } catch (Exception e) {
            LOG.error("Error displaying configuration", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
