package com.example.schema;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Flight Avro schema.
 */
public class FlightSchemaTest {

    @Test
    public void testSchemaCanBeParsed() throws IOException {
        // Get the schema file from resources
        ClassLoader classLoader = getClass().getClassLoader();
        File schemaFile = new File(classLoader.getResource("avro/flight.avsc").getFile());
        
        // Parse the schema
        Schema schema = new Schema.Parser().parse(schemaFile);
        
        // Verify schema properties
        assertEquals("com.example.schema", schema.getNamespace());
        assertEquals("Flight", schema.getName());
        assertEquals("record", schema.getType().getName());
        
        // Verify required fields exist
        assertNotNull(schema.getField("flightNumber"));
        assertNotNull(schema.getField("airline"));
        assertNotNull(schema.getField("origin"));
        assertNotNull(schema.getField("destination"));
        assertNotNull(schema.getField("scheduledDeparture"));
        assertNotNull(schema.getField("actualDeparture"));
        assertNotNull(schema.getField("status"));
        assertNotNull(schema.getField("delayMinutes"));
        assertNotNull(schema.getField("eventTimestamp"));
    }
    
    @Test
    public void testCreateFlightRecord() throws IOException {
        // Get the schema file from resources
        ClassLoader classLoader = getClass().getClassLoader();
        File schemaFile = new File(classLoader.getResource("avro/flight.avsc").getFile());
        
        // Parse the schema
        Schema schema = new Schema.Parser().parse(schemaFile);
        
        // Create a flight record
        GenericRecord flight = new GenericData.Record(schema);
        flight.put("flightNumber", "AA123");
        flight.put("airline", "American Airlines");
        flight.put("origin", "LAX");
        flight.put("destination", "JFK");
        flight.put("scheduledDeparture", Instant.now().toEpochMilli());
        flight.put("status", "SCHEDULED");
        flight.put("eventTimestamp", Instant.now().toEpochMilli());
        
        // Verify record values
        assertEquals("AA123", flight.get("flightNumber"));
        assertEquals("American Airlines", flight.get("airline"));
        assertEquals("LAX", flight.get("origin"));
        assertEquals("JFK", flight.get("destination"));
        assertEquals("SCHEDULED", flight.get("status").toString());
        assertNull(flight.get("actualDeparture"));
        assertNull(flight.get("delayMinutes"));
    }
}
