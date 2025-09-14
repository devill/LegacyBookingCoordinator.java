package com.legacybooking;

import link.specrec.CallLogger;
import link.specrec.ObjectFactory;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
        LocalDateTime bookingDate = LocalDateTime.of(2025, 3, 4, 14, 0, 56);

        StringBuilder specBook = new StringBuilder();
        CallLogger logger = new CallLogger(specBook);

        // Setup stubs using SpecRec ObjectFactory
        ObjectFactory factory = ObjectFactory.getInstance();

        // Wrap interface-based stubs with CallLogger to record interactions
        factory.setOne(BookingRepository.class, logger.wrap(BookingRepository.class, new BookingRepositoryStub(), "💾"));
        factory.setOne(FlightAvailabilityService.class, logger.wrap(FlightAvailabilityService.class, new FlightAvailabilityServiceStub(), "✈️"));
        factory.setOne(PartnerNotifier.class, logger.wrap(PartnerNotifier.class, new PartnerNotifierStub(), "📣"));
        factory.setOne(AuditLogger.class, logger.wrap(AuditLogger.class, new AuditLoggerStub(), "🪵"));

        factory.setOne(Random.class, logger.wrap(Random.class, new RandomStub(), "🎲"));

        try {
            // Act
            BookingCoordinatorImpl coordinator = new BookingCoordinatorImpl(bookingDate);
            Booking result = coordinator.bookFlight(passengerName, flightNumber, departureDate,
                    passengerCount, airlineCode, specialRequests);

            specBook.append("🔹 Final Result: ").append(result.toString()).append("\n");

            // Assert
            Approvals.verify(specBook.toString());
        } finally {
            // Clean up factory
            factory.clearAll();
        }
    }

    // Stub implementations
    public static class AuditLoggerStub implements AuditLogger {
        @Override
        public void logBookingActivity(String activity, String bookingReference, String userInfo) {
        }

        @Override
        public void recordPricingCalculation(String calculationDetails, BigDecimal finalPrice, String flightInfo) {
        }

        @Override
        public void logErrorWithAlert(Exception ex, String context, String bookingRef) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flushAndArchiveLogs() {
            throw new UnsupportedOperationException();
        }
    }

    public static class PartnerNotifierStub implements PartnerNotifier {
        @Override
        public void notifyPartnerAboutBooking(String airlineCode, String bookingReference, BigDecimal totalPrice,
                                              String passengerName, String flightDetails, boolean isRebooking) {
        }

        @Override
        public boolean validateAndNotifySpecialRequests(String airlineCode, String specialRequests, String bookingRef) {
            return true;
        }

        @Override
        public void updatePartnerBookingStatus(String airlineCode, String bookingRef, String newStatus) {
        }
    }

    public static class FlightAvailabilityServiceStub implements FlightAvailabilityService {
        @Override
        public List<String> checkAndGetAvailableSeatsForBooking(String flightNumber, LocalDateTime departureDate,
                                                                 int passengerCount) {
            return Arrays.asList("11A", "11B");
        }

        @Override
        public boolean isFlightFullyBooked(String flightNumber, LocalDateTime departureDate) {
            throw new UnsupportedOperationException();
        }
    }

    public static class BookingRepositoryStub implements BookingRepository {
        @Override
        public String saveBookingDetails(String passengerName, String flightDetails, BigDecimal price,
                                         LocalDateTime bookingDate) {
            return "APPLE3.14";
        }

        @Override
        public Map<String, Object> getBookingInfo(String bookingReference) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean validateAndEnrichBookingData(String bookingRef, BigDecimal[] actualPrice,
                                                    String[] enrichedFlightInfo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal getHistoricalPricingData(String flightNumber, LocalDateTime date, int dayRange) {
            throw new UnsupportedOperationException();
        }
    }

    public static class RandomStub extends Random {
        @Override
        public int nextInt(int bound) {
            return 3;
        }
    }

}