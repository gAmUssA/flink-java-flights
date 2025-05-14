package com.example.schema;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Utility class for working with Avro schemas and Schema Registry.
 */
public class SchemaRegistryUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaRegistryUtil.class);
    
    // Constants for schema subjects
    public static final String FLIGHT_SCHEMA_SUBJECT = "flights-value";
    public static final String FLIGHT_ALERT_SCHEMA_SUBJECT = "flight_alerts-value";
    
    // Constants for schema paths
    public static final String FLIGHT_SCHEMA_PATH = "avro/flight.avsc";
    
    private SchemaRegistryUtil() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Creates a SchemaRegistryManager from properties.
     * 
     * @param properties Properties containing schema registry configuration
     * @return A configured SchemaRegistryManager
     */
    public static SchemaRegistryManager createSchemaRegistryManager(Properties properties) {
        String schemaRegistryUrl = properties.getProperty("schema.registry.url", "http://localhost:8081");
        return new SchemaRegistryManager(schemaRegistryUrl);
    }
    
    /**
     * Registers the Flight schema with the Schema Registry.
     * 
     * @param manager SchemaRegistryManager to use
     * @return The registered Schema
     * @throws IOException if there's an error loading or registering the schema
     */
    public static Schema registerFlightSchema(SchemaRegistryManager manager) throws IOException {
        try {
            Schema schema = manager.loadSchemaFromClasspath(FLIGHT_SCHEMA_PATH);
            manager.registerSchema(FLIGHT_SCHEMA_SUBJECT, schema);
            LOG.info("Successfully registered Flight schema with subject: {}", FLIGHT_SCHEMA_SUBJECT);
            return schema;
        } catch (Exception e) {
            LOG.error("Failed to register Flight schema", e);
            throw new IOException("Failed to register Flight schema", e);
        }
    }
    
    /**
     * Creates a new GenericRecord for a Flight event.
     * 
     * @param schema The Flight Avro schema
     * @return An empty GenericRecord conforming to the Flight schema
     */
    public static GenericRecord createFlightRecord(Schema schema) {
        return new GenericData.Record(schema);
    }
}
