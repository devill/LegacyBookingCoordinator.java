package com.legacybooking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class BookingRepositoryImpl implements BookingRepository {
    private final String connectionString;
    private final int retryCount;

    public BookingRepositoryImpl(String dbConnectionString, int maxRetries) {
        throw new CanNotUseInTestsException("BookingRepositoryImpl");
    }

    @Override
    public String saveBookingDetails(String passengerName, String flightDetails, BigDecimal price, LocalDateTime bookingDate) {
        throw new CanNotUseInTestsException("BookingRepositoryImpl");
    }

    @Override
    public Map<String, Object> getBookingInfo(String bookingReference) {
        throw new CanNotUseInTestsException("BookingRepositoryImpl");
    }

    @Override
    public boolean validateAndEnrichBookingData(String bookingRef, BigDecimal[] actualPrice, String[] enrichedFlightInfo) {
        throw new CanNotUseInTestsException("BookingRepositoryImpl");
    }

    @Override
    public BigDecimal getHistoricalPricingData(String flightNumber, LocalDateTime date, int dayRange) {
        throw new CanNotUseInTestsException("BookingRepositoryImpl");
    }
}