package com.example.processor;

import com.example.model.FlightEvent;
import com.example.model.FlightStatus;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

/**
 * A stateful processor that detects delayed flights by comparing scheduled and actual departure times.
 * It also sets timers for scheduled flights to detect delays if no departure event is received.
 */
public class FlightDelayDetector extends KeyedProcessFunction<String, FlightEvent, FlightEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(FlightDelayDetector.class);
    private static final long DELAY_THRESHOLD_MINUTES = 15;
    
    private transient ValueState<FlightEvent> flightState;
    private transient ValueState<Long> timerState;
    
    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<FlightEvent> flightStateDescriptor = 
                new ValueStateDescriptor<>("flight-state", TypeInformation.of(FlightEvent.class));
        flightState = getRuntimeContext().getState(flightStateDescriptor);
        
        ValueStateDescriptor<Long> timerStateDescriptor = 
                new ValueStateDescriptor<>("timer-state", TypeInformation.of(Long.class));
        timerState = getRuntimeContext().getState(timerStateDescriptor);
    }
    
    @Override
    public void processElement(FlightEvent event, Context ctx, Collector<FlightEvent> out) throws Exception {
        FlightEvent currentFlight = flightState.value();
        
        // If this is a new flight or an update with more information
        if (currentFlight == null || shouldUpdateFlight(currentFlight, event)) {
            LOG.debug("Processing flight {}: {}", event.getFlightNumber(), event.getStatus());
            flightState.update(event);
            
            // If this is a scheduled flight, set a timer for the scheduled departure time + threshold
            if (event.getStatus() == FlightStatus.SCHEDULED) {
                long scheduledTime = event.getScheduledDeparture();
                long timerTime = scheduledTime + Duration.ofMinutes(DELAY_THRESHOLD_MINUTES).toMillis();
                
                // Register timer and save it in state
                ctx.timerService().registerEventTimeTimer(timerTime);
                timerState.update(timerTime);
                
                LOG.debug("Set timer for flight {} at {}", 
                        event.getFlightNumber(), 
                        Instant.ofEpochMilli(timerTime));
            }
            
            // If the flight has departed or been canceled, clear any timers
            if (event.getStatus() == FlightStatus.DEPARTED || 
                event.getStatus() == FlightStatus.CANCELED) {
                Long timerTime = timerState.value();
                if (timerTime != null) {
                    ctx.timerService().deleteEventTimeTimer(timerTime);
                    timerState.clear();
                    LOG.debug("Cleared timer for flight {}", event.getFlightNumber());
                }
                
                // For departed flights, calculate and log the delay
                if (event.getStatus() == FlightStatus.DEPARTED && event.getActualDeparture() != null) {
                    long delay = event.getActualDeparture() - event.getScheduledDeparture();
                    long delayMinutes = delay / (60 * 1000);
                    
                    if (delayMinutes > DELAY_THRESHOLD_MINUTES) {
                        LOG.info("Flight {} departed with delay of {} minutes", 
                                event.getFlightNumber(), delayMinutes);
                        
                        // Create a delayed flight event and emit it
                        FlightEvent delayedEvent = new FlightEvent();
                        delayedEvent.setFlightNumber(event.getFlightNumber())
                                .setAirline(event.getAirline())
                                .setOrigin(event.getOrigin())
                                .setDestination(event.getDestination())
                                .setScheduledDeparture(event.getScheduledDeparture())
                                .setActualDeparture(event.getActualDeparture())
                                .setStatus(FlightStatus.DELAYED)
                                .setDelayMinutes((int) delayMinutes)
                                .setAircraft(event.getAircraft())
                                .setEventTimestamp(System.currentTimeMillis());
                        out.collect(delayedEvent);
                    } else {
                        LOG.info("Flight {} departed on time (delay: {} minutes)", 
                                event.getFlightNumber(), delayMinutes);
                    }
                }
            }
        }
    }
    
    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<FlightEvent> out) throws Exception {
        FlightEvent flight = flightState.value();
        
        // If the flight still exists and hasn't departed or been canceled
        if (flight != null && 
            flight.getStatus() != FlightStatus.DEPARTED && 
            flight.getStatus() != FlightStatus.CANCELED) {
            
            LOG.info("Flight {} is delayed (no departure event received)", flight.getFlightNumber());
            
            // Create a delayed flight event and emit it
            FlightEvent delayedEvent = new FlightEvent();
            delayedEvent.setFlightNumber(flight.getFlightNumber())
                    .setAirline(flight.getAirline())
                    .setOrigin(flight.getOrigin())
                    .setDestination(flight.getDestination())
                    .setScheduledDeparture(flight.getScheduledDeparture())
                    .setStatus(FlightStatus.DELAYED)
                    .setDelayMinutes((int) DELAY_THRESHOLD_MINUTES) // Minimum delay
                    .setAircraft(flight.getAircraft())
                    .setEventTimestamp(System.currentTimeMillis());
            out.collect(delayedEvent);
        }
        
        // Clear the timer state
        timerState.clear();
    }
    
    private boolean shouldUpdateFlight(FlightEvent current, FlightEvent update) {
        // Always update if the current flight is just scheduled and we have more information
        if (current.getStatus() == FlightStatus.SCHEDULED && 
            update.getStatus() != FlightStatus.SCHEDULED) {
            return true;
        }
        
        // Update if we now have actual departure information
        if (current.getActualDeparture() == null && update.getActualDeparture() != null) {
            return true;
        }
        
        return false;
    }
}
