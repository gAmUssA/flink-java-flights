package com.example.schema;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing Avro schemas and interacting with the Schema Registry.
 */
public class SchemaRegistryManager {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaRegistryManager.class);
    private final SchemaRegistryClient schemaRegistryClient;
    private final Map<String, Schema> schemaCache = new HashMap<>();

    /**
     * Creates a new SchemaRegistryManager with the specified Schema Registry URL.
     *
     * @param schemaRegistryUrl URL of the Schema Registry
     */
    public SchemaRegistryManager(String schemaRegistryUrl) {
        this.schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryUrl, 20);
    }

    /**
     * Loads an Avro schema from a file.
     *
     * @param schemaPath Path to the Avro schema file
     * @return The parsed Avro schema
     * @throws IOException If the schema file cannot be read
     */
    public Schema loadSchema(String schemaPath) throws IOException {
        if (schemaCache.containsKey(schemaPath)) {
            return schemaCache.get(schemaPath);
        }

        String schemaContent = new String(Files.readAllBytes(Paths.get(schemaPath)));
        Schema schema = new Schema.Parser().parse(schemaContent);
        schemaCache.put(schemaPath, schema);
        return schema;
    }

    /**
     * Loads an Avro schema from the classpath.
     *
     * @param resourcePath Path to the Avro schema resource
     * @return The parsed Avro schema
     * @throws IOException If the schema resource cannot be read
     */
    public Schema loadSchemaFromClasspath(String resourcePath) throws IOException {
        if (schemaCache.containsKey(resourcePath)) {
            return schemaCache.get(resourcePath);
        }

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        String schemaContent = new String(Files.readAllBytes(file.toPath()));
        Schema schema = new Schema.Parser().parse(schemaContent);
        schemaCache.put(resourcePath, schema);
        return schema;
    }

    /**
     * Registers an Avro schema with the Schema Registry.
     *
     * @param subject Subject name for the schema
     * @param schema  The Avro schema to register
     * @return The schema ID assigned by the Schema Registry
     * @throws IOException       If there's an error communicating with the Schema Registry
     * @throws RestClientException If the Schema Registry returns an error
     */
    public int registerSchema(String subject, Schema schema) throws IOException, RestClientException {
        LOG.info("Registering schema for subject: {}", subject);
        AvroSchema avroSchema = new AvroSchema(schema);
        return schemaRegistryClient.register(subject + "-value", avroSchema);
    }

    /**
     * Gets the latest version of a schema from the Schema Registry.
     *
     * @param subject Subject name for the schema
     * @return The latest Avro schema for the subject
     * @throws IOException       If there's an error communicating with the Schema Registry
     * @throws RestClientException If the Schema Registry returns an error
     */
    public Schema getLatestSchema(String subject) throws IOException, RestClientException {
        LOG.info("Getting latest schema for subject: {}", subject);
        String schemaString = schemaRegistryClient.getLatestSchemaMetadata(subject).getSchema();
        return new Schema.Parser().parse(schemaString);
    }

    /**
     * Gets the SchemaRegistryClient instance.
     *
     * @return The SchemaRegistryClient
     */
    public SchemaRegistryClient getSchemaRegistryClient() {
        return schemaRegistryClient;
    }
}
