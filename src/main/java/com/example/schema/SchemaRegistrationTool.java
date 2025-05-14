package com.example.schema;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Command-line tool for registering Avro schemas with the Schema Registry.
 */
public class SchemaRegistrationTool {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaRegistrationTool.class);

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: SchemaRegistrationTool <config-file>");
            System.exit(1);
        }

        String configFile = args[0];
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
            LOG.info("Loaded configuration from {}", configFile);

            SchemaRegistryManager manager = SchemaRegistryUtil.createSchemaRegistryManager(properties);
            
            // Register the Flight schema
            Schema flightSchema = SchemaRegistryUtil.registerFlightSchema(manager);
            LOG.info("Successfully registered Flight schema: {}", flightSchema.getFullName());
            
            LOG.info("Schema registration completed successfully");
        } catch (IOException e) {
            LOG.error("Error registering schemas", e);
            System.exit(1);
        }
    }
}
