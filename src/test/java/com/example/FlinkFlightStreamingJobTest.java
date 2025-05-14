package com.example;

import com.example.model.FlightEvent;
import com.example.model.FlightStatus;
import com.example.processor.FlightDelayDetector;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration test for the FlinkFlightStreamingJob.
 */
public class FlinkFlightStreamingJobTest {

    @Test
    public void testFlightDelayDetection() throws Exception {
        // Create a local stream execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        // Create a list of test flight events
        List<FlightEvent> testEvents = createTestFlightEvents();
        
        // Create a watermark strategy with a 5-minute out-of-orderness allowance
        WatermarkStrategy<FlightEvent> watermarkStrategy = WatermarkStrategy
                .<FlightEvent>forBoundedOutOfOrderness(Duration.ofMinutes(5))
                .withTimestampAssigner((event, timestamp) -> event.getScheduledDeparture());
        
        // Create a test source using the modern API
        DataStream<FlightEvent> flightEvents = env
                .fromCollection(testEvents)
                .assignTimestampsAndWatermarks(watermarkStrategy)
                .name("flight-events-with-watermarks")
                .uid("flight-events-with-watermarks-uid");
        
        // Key the stream by flight number and apply the flight delay detector
        DataStream<FlightEvent> delayedFlights = flightEvents
                .keyBy(FlightEvent::getFlightNumber)
                .process(new FlightDelayDetector())
                .name("flight-delay-detector")
                .uid("flight-delay-detector-uid");
        
        // Print delayed flights to the console (for development/debugging)
        delayedFlights
                .filter(event -> event.getStatus() == FlightStatus.DELAYED)
                .print()
                .name("print-delayed-flights")
                .uid("print-delayed-flights-uid");
        
        // Execute the job
        env.execute("Test Flight Delay Detection");
    }
    
    /**
     * Creates a list of test flight events.
     * 
     * @return A list of test flight events
     */
    private List<FlightEvent> createTestFlightEvents() {
        List<FlightEvent> events = new ArrayList<>();
        
        // Current time
        Instant now = Instant.now();
        
        // Flight 1: On-time flight
        FlightEvent flight1Scheduled = new FlightEvent();
        flight1Scheduled.setFlightNumber("AA101")
                .setAirline("American Airlines")
                .setOrigin("LAX")
                .setDestination("JFK")
                .setStatus(FlightStatus.SCHEDULED)
                .setScheduledDeparture(now.toEpochMilli())
                .setEventTimestamp(now.toEpochMilli());
        events.add(flight1Scheduled);
        
        // Flight 1: Departed on time (5 minutes later)
        FlightEvent flight1Departed = new FlightEvent();
        flight1Departed.setFlightNumber("AA101")
                .setAirline("American Airlines")
                .setOrigin("LAX")
                .setDestination("JFK")
                .setStatus(FlightStatus.DEPARTED)
                .setScheduledDeparture(now.toEpochMilli())
                .setActualDeparture(now.plus(Duration.ofMinutes(5)).toEpochMilli())
                .setEventTimestamp(now.plus(Duration.ofMinutes(5)).toEpochMilli());
        events.add(flight1Departed);
        
        // Flight 2: Delayed flight
        FlightEvent flight2Scheduled = new FlightEvent();
        flight2Scheduled.setFlightNumber("AA202")
                .setAirline("American Airlines")
                .setOrigin("SFO")
                .setDestination("ORD")
                .setStatus(FlightStatus.SCHEDULED)
                .setScheduledDeparture(now.plus(Duration.ofMinutes(30)).toEpochMilli())
                .setEventTimestamp(now.toEpochMilli());
        events.add(flight2Scheduled);
        
        // Flight 2: Departed with delay (30 + 20 = 50 minutes after now)
        FlightEvent flight2Departed = new FlightEvent();
        flight2Departed.setFlightNumber("AA202")
                .setAirline("American Airlines")
                .setOrigin("SFO")
                .setDestination("ORD")
                .setStatus(FlightStatus.DEPARTED)
                .setScheduledDeparture(now.plus(Duration.ofMinutes(30)).toEpochMilli())
                .setActualDeparture(now.plus(Duration.ofMinutes(50)).toEpochMilli())
                .setEventTimestamp(now.plus(Duration.ofMinutes(50)).toEpochMilli());
        events.add(flight2Departed);
        
        // Flight 3: No departure event (will trigger timer)
        FlightEvent flight3Scheduled = new FlightEvent();
        flight3Scheduled.setFlightNumber("AA303")
                .setAirline("American Airlines")
                .setOrigin("DFW")
                .setDestination("MIA")
                .setStatus(FlightStatus.SCHEDULED)
                .setScheduledDeparture(now.plus(Duration.ofMinutes(60)).toEpochMilli())
                .setEventTimestamp(now.toEpochMilli());
        events.add(flight3Scheduled);
        
        return events;
    }
    
    // Modern approach doesn't require a custom source implementation
    // We use the built-in collection source instead
}
