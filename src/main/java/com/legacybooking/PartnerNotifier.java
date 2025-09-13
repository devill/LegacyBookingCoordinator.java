package com.legacybooking;

import java.math.BigDecimal;

/**
 * Interface for partner notification operations
 */
public interface PartnerNotifier {
    void notifyPartnerAboutBooking(String airlineCode, String bookingReference,
                                 BigDecimal totalPrice, String passengerName, String flightDetails, boolean isRebooking);

    boolean validateAndNotifySpecialRequests(String airlineCode, String specialRequests, String bookingRef);

    void updatePartnerBookingStatus(String airlineCode, String bookingRef, String newStatus);
}