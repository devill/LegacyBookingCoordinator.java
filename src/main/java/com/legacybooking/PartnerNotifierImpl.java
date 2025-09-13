package com.legacybooking;

import java.math.BigDecimal;

public class PartnerNotifierImpl implements PartnerNotifier {
    private final String logDestination;
    private final boolean enableSecureMode;

    public PartnerNotifierImpl(String smtpServer, boolean useEncryption) {
        throw new CanNotUseInTestsException("PartnerNotifierImpl");
    }

    @Override
    public void notifyPartnerAboutBooking(String airlineCode, String bookingReference,
                                        BigDecimal totalPrice, String passengerName, String flightDetails, boolean isRebooking) {
        throw new CanNotUseInTestsException("PartnerNotifierImpl");
    }

    @Override
    public boolean validateAndNotifySpecialRequests(String airlineCode, String specialRequests, String bookingRef) {
        throw new CanNotUseInTestsException("PartnerNotifierImpl");
    }

    @Override
    public void updatePartnerBookingStatus(String airlineCode, String bookingRef, String newStatus) {
        throw new CanNotUseInTestsException("PartnerNotifierImpl");
    }
}