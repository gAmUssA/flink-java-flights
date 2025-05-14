package com.example.model;

import java.time.Instant;

/**
 * Flight event data class representing a flight and its status.
 */
public class FlightEvent {
    // Flight identification
    private String flightNumber;
    private String airline;
    private String origin;
    private String destination;
    
    // Timing information
    private long scheduledDeparture;
    private Long actualDeparture; // nullable
    
    // Status information
    private FlightStatus status; // SCHEDULED, DEPARTED, DELAYED, CANCELED
    private Integer delayMinutes; // nullable
    
    // Additional information
    private String aircraft; // nullable
    private long eventTimestamp;
    
    /**
     * Creates a new empty FlightEvent.
     */
    public FlightEvent() {
        // Default constructor
    }
    
    /**
     * Gets the flight number.
     *
     * @return The flight number
     */
    public String getFlightNumber() {
        return flightNumber;
    }

    /**
     * Sets the flight number.
     *
     * @param flightNumber The flight number to set
     * @return This FlightEvent for method chaining
     */
    public FlightEvent setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
        return this;
    }

    /**
     * Gets the airline name.
     *
     * @return The airline name
     */
    public String getAirline() {
        return airline;
    }

    /**
     * Sets the airline name.
     *
     * @param airline The airline name to set
     * @return This FlightEvent for method chaining
     */
    public FlightEvent setAirline(String airline) {
        this.airline = airline;
        return this;
    }

    /**
     * Gets the origin airport code.
     *
     * @return The origin airport code
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Sets the origin airport code.
     *
     * @param origin The origin airport code to set
     * @return This FlightEvent for method chaining
     */
    public FlightEvent setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    /**
     * Gets the destination airport code.
     *
     * @return The destination airport code
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets the destination airport code.
     *
     * @param destination The destination airport code to set
     * @return This FlightEvent for method chaining
     */
    public FlightEvent setDestination(String destination) {
        this.destination = destination;
        return this;
    }

    /**
     * Gets the scheduled departure time in milliseconds since epoch.
     *
     * @return The scheduled departure time
     */
    public long getScheduledDeparture() {
        return scheduledDeparture;
    }

    /**
     * Sets the scheduled departure time.
     *
     * @param scheduledDeparture The scheduled departure time in milliseconds since epoch
     * @return This FlightEvent for method chaining
     */
    public FlightEvent setScheduledDeparture(long scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
        return this;
    }

    /**
     * Sets the scheduled departure time using an Instant.
     *
     * @param instant The scheduled departure time as an Instant
     * @return This FlightEvent for method chaining
     */
    public FlightEvent setScheduledDeparture(Instant instant) {
        this.scheduledDeparture = instant.toEpochMilli();
        return this;
    }

    /**
     * Gets the actual departure time in milliseconds since epoch.
     *
     * @return The actual departure time, or null if not departed yet
     */
    public Long getActualDeparture() {
        return actualDeparture;
    }

    /**
     * Sets the actual departure time.
     *
     * @param actualDeparture The actual departure time in milliseconds since epoch
     * @return This FlightEvent for method chaining
     */
    public FlightEvent setActualDeparture(Long actualDeparture) {
        this.actualDeparture = actualDeparture;
        return this;
    }

    /**
     * Sets the actual departure time using an Instant.
     *
     * @param instant The actual departure time as an Instant
     * @return This FlightEvent for method chaining
     */
    public FlightEvent setActualDeparture(Instant instant) {
        this.actualDeparture = instant != null ? instant.toEpochMilli() : null;
        return this;
    }

    /**
     * Gets the flight status.
     *
     * @return The flight status
     */
    public FlightStatus getStatus() {
        return status;
    }

    /**
     * Sets the flight status.
     *
     * @param status The flight status to set
     * @return This FlightEvent for method chaining
     */
    public FlightEvent setStatus(FlightStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Gets the delay in minutes.
     *
     * @return The delay in minutes, or null if not delayed
     */
    public Integer getDelayMinutes() {
        return delayMinutes;
    }

    /**
     * Sets the delay in minutes.
     *
     * @param delayMinutes The delay in minutes
     * @return This FlightEvent for method chaining
     */
    public FlightEvent setDelayMinutes(Integer delayMinutes) {
        this.delayMinutes = delayMinutes;
        return this;
    }

    /**
     * Gets the aircraft type.
     *
     * @return The aircraft type, or null if not specified
     */
    public String getAircraft() {
        return aircraft;
    }

    /**
     * Sets the aircraft type.
     *
     * @param aircraft The aircraft type to set
     * @return This FlightEvent for method chaining
     */
    public FlightEvent setAircraft(String aircraft) {
        this.aircraft = aircraft;
        return this;
    }

    /**
     * Gets the event timestamp in milliseconds since epoch.
     *
     * @return The event timestamp
     */
    public long getEventTimestamp() {
        return eventTimestamp;
    }

    /**
     * Sets the event timestamp.
     *
     * @param eventTimestamp The event timestamp in milliseconds since epoch
     * @return This FlightEvent for method chaining
     */
    public FlightEvent setEventTimestamp(long eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
        return this;
    }

    /**
     * Sets the event timestamp using an Instant.
     *
     * @param instant The event timestamp as an Instant
     * @return This FlightEvent for method chaining
     */
    public FlightEvent setEventTimestamp(Instant instant) {
        this.eventTimestamp = instant.toEpochMilli();
        return this;
    }

    /**
     * Calculates the delay in minutes between scheduled and actual departure times.
     * 
     * @return The delay in minutes, or null if actual departure is not set
     */
    public Integer calculateDelay() {
        if (actualDeparture == null) {
            return null;
        }
        
        long delayMs = actualDeparture - scheduledDeparture;
        return (int) (delayMs / (60 * 1000));
    }

    @Override
    public String toString() {
        return "FlightEvent{" +
                "flightNumber='" + flightNumber + '\'' +
                ", airline='" + airline + '\'' +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", status=" + status +
                ", scheduledDeparture=" + Instant.ofEpochMilli(scheduledDeparture) +
                ", actualDeparture=" + (actualDeparture != null ? Instant.ofEpochMilli(actualDeparture) : "null") +
                ", delayMinutes=" + delayMinutes +
                '}';
    }
}
