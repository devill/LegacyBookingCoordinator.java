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

        StringBuilder specBook = new StringBuilder();

        // Act
        BookingCoordinatorImpl coordinator = new BookingCoordinatorImpl();
        Booking result = coordinator.bookFlight(passengerName, flightNumber, departureDate,
                passengerCount, airlineCode, specialRequests);

        specBook.append("🔹 Final Result: ").append(result.toString()).append("\n");

        // Assert
        Approvals.verify(specBook.toString());
    }
}