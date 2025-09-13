package com.legacybooking;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class BookingCoordinatorTest {

    @Test
    public void bookFlightShouldCreateBookingSuccessfully() {
        // Arrange
        String passengerName = "John Doe";
        String flightNumber = "AA123";
        LocalDateTime departureDate = LocalDateTime.of(2025, 7, 3, 12, 42, 11);
        int passengerCount = 2;
        String airlineCode = "AA";
        String specialRequests = "meal,wheelchair";

        // Act & Assert
        BookingCoordinatorImpl coordinator = new BookingCoordinatorImpl();
        String result = coordinator.bookFlight(passengerName, flightNumber, departureDate,
                passengerCount, airlineCode, specialRequests).toString();

        Approvals.verify(result);
    }
}