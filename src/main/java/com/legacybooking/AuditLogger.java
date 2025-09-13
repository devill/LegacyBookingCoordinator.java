package com.legacybooking;

/**
 * Interface for audit logging operations
 */
public interface AuditLogger {
    void logBookingActivity(String activity, String bookingReference, String userInfo);
    void recordPricingCalculation(String calculationDetails, java.math.BigDecimal finalPrice, String flightInfo);
    void logErrorWithAlert(Exception ex, String context, String bookingRef);
    void flushAndArchiveLogs();
}