package com.example.processor;

import com.example.model.FlightEvent;
import com.example.model.FlightStatus;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.operators.KeyedProcessOperator;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.KeyedOneInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the FlightDelayDetector class.
 */
public class FlightDelayDetectorTest {
    
    private OneInputStreamOperatorTestHarness<FlightEvent, FlightEvent> testHarness;
    private FlightDelayDetector delayDetector;
    
    @BeforeEach
    public void setUp() throws Exception {
        delayDetector = new FlightDelayDetector();
        
        // Create a test harness for the KeyedProcessFunction
        KeyedProcessOperator<String, FlightEvent, FlightEvent> operator = 
                new KeyedProcessOperator<>(delayDetector);
        
        testHarness = new KeyedOneInputStreamOperatorTestHarness<>(
                operator, 
                FlightEvent::getFlightNumber, 
                Types.STRING);
        
        testHarness.open();
    }
    
    @Test
    public void testDetectsDelayedFlightWhenActualDepartureIsLate() throws Exception {
        // Current time
        Instant now = Instant.now();
        
        // Create a scheduled flight event
        FlightEvent scheduledEvent = createFlightEvent(
                "AA123", 
                "American Airlines", 
                "LAX", 
                "JFK", 
                FlightStatus.SCHEDULED, 
                now, 
                null);
        
        // Process the scheduled event
        testHarness.processElement(scheduledEvent, now.toEpochMilli());
        
        // Create a departed event with a 20-minute delay (beyond the 15-minute threshold)
        Instant actualDeparture = now.plus(20, ChronoUnit.MINUTES);
        FlightEvent departedEvent = createFlightEvent(
                "AA123", 
                "American Airlines", 
                "LAX", 
                "JFK", 
                FlightStatus.DEPARTED, 
                now, 
                actualDeparture);
        
        // Process the departed event
        testHarness.processElement(departedEvent, actualDeparture.toEpochMilli());
        
        // Verify that a delayed flight event was emitted
        assertEquals(1, testHarness.getOutput().size());
        StreamRecord<FlightEvent> record = (StreamRecord<FlightEvent>) testHarness.getOutput().poll();
        FlightEvent delayedEvent = record.getValue();
        
        assertEquals(FlightStatus.DELAYED, delayedEvent.getStatus());
        assertEquals("AA123", delayedEvent.getFlightNumber());
        assertEquals(now.toEpochMilli(), delayedEvent.getScheduledDeparture());
        assertEquals(actualDeparture.toEpochMilli(), delayedEvent.getActualDeparture());
        assertTrue(delayedEvent.getDelayMinutes() >= 20);
    }
    
    @Test
    public void testDoesNotDetectDelayWhenDepartureIsOnTime() throws Exception {
        // Current time
        Instant now = Instant.now();
        
        // Create a scheduled flight event
        FlightEvent scheduledEvent = createFlightEvent(
                "AA456", 
                "American Airlines", 
                "LAX", 
                "JFK", 
                FlightStatus.SCHEDULED, 
                now, 
                null);
        
        // Process the scheduled event
        testHarness.processElement(scheduledEvent, now.toEpochMilli());
        
        // Create a departed event with a 10-minute delay (below the 15-minute threshold)
        Instant actualDeparture = now.plus(10, ChronoUnit.MINUTES);
        FlightEvent departedEvent = createFlightEvent(
                "AA456", 
                "American Airlines", 
                "LAX", 
                "JFK", 
                FlightStatus.DEPARTED, 
                now, 
                actualDeparture);
        
        // Process the departed event
        testHarness.processElement(departedEvent, actualDeparture.toEpochMilli());
        
        // Verify that no delayed flight event was emitted
        assertEquals(0, testHarness.getOutput().size());
    }
    
    @Test
    public void testDetectsDelayWhenTimerFires() throws Exception {
        // Current time
        Instant now = Instant.now();
        
        // Create a scheduled flight event
        FlightEvent scheduledEvent = createFlightEvent(
                "AA789", 
                "American Airlines", 
                "LAX", 
                "JFK", 
                FlightStatus.SCHEDULED, 
                now, 
                null);
        
        // Process the scheduled event
        testHarness.processElement(scheduledEvent, now.toEpochMilli());
        
        // Advance time to after the delay threshold (15 minutes)
        Instant afterThreshold = now.plus(16, ChronoUnit.MINUTES);
        
        // Fire the event time timer
        testHarness.processWatermark(afterThreshold.toEpochMilli());
        
        // Verify that at least two elements were emitted (watermark + event)
        assertTrue(testHarness.getOutput().size() >= 2);
        
        // Find the delayed flight event in the output
        StreamRecord<FlightEvent> record = null;
        while (!testHarness.getOutput().isEmpty()) {
            Object element = testHarness.getOutput().poll();
            if (element instanceof StreamRecord) {
                record = (StreamRecord<FlightEvent>) element;
                break;
            }
        }
        
        // Verify we found a record
        assertNotNull(record, "No StreamRecord found in output");
        FlightEvent delayedEvent = record.getValue();
        
        assertEquals(FlightStatus.DELAYED, delayedEvent.getStatus());
        assertEquals("AA789", delayedEvent.getFlightNumber());
        assertEquals(now.toEpochMilli(), delayedEvent.getScheduledDeparture());
    }
    
    @Test
    public void testClearsTimerWhenFlightDeparts() throws Exception {
        // Current time
        Instant now = Instant.now();
        
        // Create a scheduled flight event
        FlightEvent scheduledEvent = createFlightEvent(
                "AA101", 
                "American Airlines", 
                "LAX", 
                "JFK", 
                FlightStatus.SCHEDULED, 
                now, 
                null);
        
        // Process the scheduled event
        testHarness.processElement(scheduledEvent, now.toEpochMilli());
        
        // Create a departed event before the delay threshold
        Instant actualDeparture = now.plus(5, ChronoUnit.MINUTES);
        FlightEvent departedEvent = createFlightEvent(
                "AA101", 
                "American Airlines", 
                "LAX", 
                "JFK", 
                FlightStatus.DEPARTED, 
                now, 
                actualDeparture);
        
        // Process the departed event
        testHarness.processElement(departedEvent, actualDeparture.toEpochMilli());
        
        // Advance time to after the delay threshold (15 minutes)
        Instant afterThreshold = now.plus(16, ChronoUnit.MINUTES);
        testHarness.setProcessingTime(afterThreshold.toEpochMilli());
        
        // Fire the event time timer
        testHarness.processWatermark(afterThreshold.toEpochMilli());
        
        // Verify that no delayed flight event was emitted (only the watermark)
        assertEquals(1, testHarness.getOutput().size());
        testHarness.getOutput().poll(); // Remove the watermark
        assertTrue(testHarness.getOutput().isEmpty());
    }
    
    /**
     * Helper method to create a flight event.
     */
    private FlightEvent createFlightEvent(
            String flightNumber, 
            String airline, 
            String origin, 
            String destination, 
            FlightStatus status, 
            Instant scheduledDeparture, 
            Instant actualDeparture) {
        
        FlightEvent event = new FlightEvent();
        event.setFlightNumber(flightNumber)
                .setAirline(airline)
                .setOrigin(origin)
                .setDestination(destination)
                .setStatus(status)
                .setScheduledDeparture(scheduledDeparture.toEpochMilli());
        
        if (actualDeparture != null) {
            event.setActualDeparture(actualDeparture.toEpochMilli());
        }
        
        event.setEventTimestamp(Instant.now().toEpochMilli());
        
        return event;
    }
}
