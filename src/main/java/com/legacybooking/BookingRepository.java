package com.legacybooking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Interface for booking repository operations
 */
public interface BookingRepository {
    String saveBookingDetails(String passengerName, String flightDetails, BigDecimal price, LocalDateTime bookingDate);

    Map<String, Object> getBookingInfo(String bookingReference);

    boolean validateAndEnrichBookingData(String bookingRef, BigDecimal[] actualPrice, String[] enrichedFlightInfo);

    BigDecimal getHistoricalPricingData(String flightNumber, LocalDateTime date, int dayRange);
}