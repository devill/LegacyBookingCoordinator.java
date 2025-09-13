package com.legacybooking;

import java.time.LocalDateTime;
import java.util.List;

public class FlightAvailabilityServiceImpl implements FlightAvailabilityService {
    private final String airlineApiConfig;

    public FlightAvailabilityServiceImpl(String connectionString) {
        throw new CanNotUseInTestsException("FlightAvailabilityServiceImpl");
    }

    @Override
    public List<String> checkAndGetAvailableSeatsForBooking(String flightNumber, LocalDateTime departureDate, int passengerCount) {
        throw new CanNotUseInTestsException("FlightAvailabilityServiceImpl");
    }

    @Override
    public boolean isFlightFullyBooked(String flightNumber, LocalDateTime departureDate) {
        throw new CanNotUseInTestsException("FlightAvailabilityServiceImpl");
    }
}