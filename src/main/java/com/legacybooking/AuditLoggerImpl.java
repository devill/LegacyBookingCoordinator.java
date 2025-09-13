package com.legacybooking;

import java.math.BigDecimal;

public class AuditLoggerImpl implements AuditLogger {
    private final String filePath;
    private final boolean enableConsoleOutput;

    public AuditLoggerImpl(String logDirectory, boolean verboseMode) {
        throw new CanNotUseInTestsException("AuditLoggerImpl");
    }

    @Override
    public void logBookingActivity(String activity, String bookingReference, String userInfo) {
        throw new CanNotUseInTestsException("AuditLoggerImpl");
    }

    @Override
    public void recordPricingCalculation(String calculationDetails, BigDecimal finalPrice, String flightInfo) {
        throw new CanNotUseInTestsException("AuditLoggerImpl");
    }

    @Override
    public void logErrorWithAlert(Exception ex, String context, String bookingRef) {
        throw new CanNotUseInTestsException("AuditLoggerImpl");
    }

    @Override
    public void flushAndArchiveLogs() {
        throw new CanNotUseInTestsException("AuditLoggerImpl");
    }
}