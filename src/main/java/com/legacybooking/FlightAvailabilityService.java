package com.legacybooking;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for flight availability operations
 */
public interface FlightAvailabilityService {
    List<String> checkAndGetAvailableSeatsForBooking(String flightNumber, LocalDateTime departureDate, int passengerCount);

    boolean isFlightFullyBooked(String flightNumber, LocalDateTime departureDate);
}