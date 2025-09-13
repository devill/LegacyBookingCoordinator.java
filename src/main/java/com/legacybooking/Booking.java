package com.legacybooking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Booking {
    private final String bookingReference;
    private final String passengerName;
    private final String flightNumber;
    private final LocalDateTime departureDate;
    private final int passengerCount;
    private final String airlineCode;
    private final BigDecimal finalPrice;
    private final String specialRequests;
    private final LocalDateTime bookingDate;
    private final String status;

    public Booking(String bookingReference, String passengerName, String flightNumber,
                  LocalDateTime departureDate, int passengerCount, String airlineCode,
                  BigDecimal finalPrice, String specialRequests, LocalDateTime bookingDate, String status) {
        this.bookingReference = bookingReference;
        this.passengerName = passengerName;
        this.flightNumber = flightNumber;
        this.departureDate = departureDate;
        this.passengerCount = passengerCount;
        this.airlineCode = airlineCode;
        this.finalPrice = finalPrice;
        this.specialRequests = specialRequests;
        this.bookingDate = bookingDate;
        this.status = status;
    }

    public String getBookingReference() { return bookingReference; }
    public String getPassengerName() { return passengerName; }
    public String getFlightNumber() { return flightNumber; }
    public LocalDateTime getDepartureDate() { return departureDate; }
    public int getPassengerCount() { return passengerCount; }
    public String getAirlineCode() { return airlineCode; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public String getSpecialRequests() { return specialRequests; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        result.append("New booking: ").append(bookingReference).append("\n");
        result.append("  👤 ").append(passengerName).append("\n");
        result.append("  ✈️ ").append(flightNumber).append("\n");
        result.append("  📅 ").append(departureDate.format(formatter)).append("\n");
        result.append("  👥 ").append(passengerCount).append("\n");
        result.append("  🏢 ").append(airlineCode).append("\n");
        result.append("  💰 $").append(String.format("%.2f", finalPrice)).append("\n");

        if (specialRequests != null && !specialRequests.isEmpty()) {
            result.append("  🎯 ").append(specialRequests).append("\n");
        }

        result.append("  📝 ").append(bookingDate.format(formatter)).append("\n");
        result.append("  ✅ ").append(status);

        return result.toString();
    }
}