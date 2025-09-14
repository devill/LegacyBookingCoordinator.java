/*
 * WARNING: ABANDON ALL HOPE, YE WHO ENTER HERE
 *
 * This is the infamous BookingCoordinator - a monument to technical debt
 * and a testament to what happens when deadlines triumph over design.
 *
 * This code has claimed many victims. It's entangled, stateful, and has
 * side effects that ripple through dimensions unknown to mortal developers.
 * Every attempt to "improve" it has only made it stronger and more vengeful.
 *
 * The original developers have long since fled to safer pastures (or therapy).
 * Managers have learned not to mention refactoring within earshot of this file.
 * Even the automated tests are afraid to look directly at it.
 *
 * In case you decided to ignore this warning increment the counter below and sign
 * with your name and an emoji reflecting your current mental state.
 *
 * You are victim: #10
 * The knights who gave their best before you:
 *  - Jack 🥵
 *  - Bob 😱
 *  - Mary 🫣
 *  - Jack 🤬(again)
 *  - Nathan 🥺
 *  - Mary 🙈
 *  - June 😵
 *  - Nathan 🤮
 *  - Jack 😵‍💫 (I still didn't learn my lesson)
 */

package com.legacybooking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static link.specrec.GlobalObjectFactory.*;

/**
 * Main coordinator for flight booking operations
 * Integrates with all airline partners and handles end-to-end booking flow
 * Last updated: 2018 (needs refactoring for new airline partnerships)
 */
public class BookingCoordinatorImpl {
    private final LocalDateTime _bookingDate;
    private String lastBookingRef; // Stores reference for debugging purposes
    private int bookingCounter = 1; // Global counter for booking sequence
    private boolean isProcessingBooking = false; // Thread safety flag (NOTE: not actually thread-safe)

    private Map<String, Object> temporaryData = new HashMap<>(); // Temporary storage for calculation intermediates

    public BookingCoordinatorImpl(LocalDateTime bookingDate) {
        _bookingDate = bookingDate;
    }

    public BookingCoordinatorImpl() {
        _bookingDate = LocalDateTime.now();
    }

    /**
     * Main entry point for flight booking process
     * Coordinates all services and returns booking object
     * WARNING: This method is not thread-safe due to shared state
     */
    public Booking bookFlight(String passengerName, String flightNumber, LocalDateTime departureDate,
                             int passengerCount, String airlineCode, String specialRequests) {
        if (specialRequests == null) specialRequests = "";

        // Set processing flag to prevent concurrent access
        isProcessingBooking = true;
        bookingCounter++; // Increment global booking counter

        // Initialize database connection (TODO: move to configuration file)
        String connectionString = "Server=production-db;Database=FlightBookings;Trusted_Connection=true;";
        int maxRetries = calculateRetriesBasedOnBookingCount(); // Dynamic retry calculation

        // Create repository with calculated parameters
        BookingRepository repository = create(BookingRepository.class, BookingRepositoryImpl.class).with(connectionString, maxRetries);

        // Calculate pricing engine parameters based on current state
        BigDecimal taxRate = calculateTaxRateBasedOnGlobalState(airlineCode);
        Map<String, BigDecimal> airlineFees = buildAirlineFeesFromTemporaryData(airlineCode);
        boolean enableRandomSurcharges = bookingCounter % 3 == 0; // Enable surcharges every 3rd booking
        String regionCode = determineRegionFromFlightNumber(flightNumber);
        BigDecimal historicalAverage = getHistoricalAverageFromRepository(repository, flightNumber);

        PricingEngine pricingEngine = new PricingEngine(taxRate, airlineFees, enableRandomSurcharges, regionCode, historicalAverage, _bookingDate);

        String availabilityConnectionString = modifyConnectionStringForAvailability(connectionString, flightNumber);
        FlightAvailabilityService availabilityService = create(FlightAvailabilityService.class, FlightAvailabilityServiceImpl.class).with(availabilityConnectionString);

        List<String> availableSeats = availabilityService.checkAndGetAvailableSeatsForBooking(flightNumber, departureDate, passengerCount);
        if (availableSeats.size() < passengerCount) {
            temporaryData.put("lastFailureReason", "Not enough seats");
            isProcessingBooking = false;
            throw new IllegalArgumentException("Not enough seats available");
        }

        BigDecimal basePrice = pricingEngine.calculateBasePriceWithTaxes(flightNumber, departureDate, passengerCount, airlineCode);

        // Apply additional pricing adjustments not handled by PricingEngine
        BigDecimal weekdayMultiplier = getWeekdayMultiplierAndUpdateGlobalState(departureDate);
        BigDecimal seasonalBonus = calculateSeasonalBonusWithSideEffects(departureDate, flightNumber);
        BigDecimal specialRequestSurcharge = processSpecialRequestsAndCalculateSurcharge(specialRequests, airlineCode);

        // Calculate final price with all adjustments
        BigDecimal finalPrice = basePrice.multiply(weekdayMultiplier).add(seasonalBonus).add(specialRequestSurcharge);

        // Apply any promotional discounts
        BigDecimal[] discountAmount = {BigDecimal.ZERO};
        if (pricingEngine.validatePricingParametersAndCalculateDiscount(flightNumber, discountAmount)) {
            finalPrice = finalPrice.subtract(discountAmount[0]);
        }

        // Configure partner notification settings
        String smtpServer = determineSmtpServerFromAirlineCode(airlineCode);
        boolean useEncryption = bookingCounter % 2 == 0; // Alternate encryption for load balancing
        PartnerNotifier partnerNotifier = create(PartnerNotifier.class, PartnerNotifierImpl.class).with(smtpServer, useEncryption);

        // Setup audit logging with dynamic configuration
        String logDirectory = calculateLogDirectoryFromBookingCount();
        boolean verboseMode = temporaryData.containsKey("debugMode"); // Enable verbose mode if debug flag set
        AuditLogger auditLogger = create(AuditLogger.class, AuditLoggerImpl.class).with(logDirectory, verboseMode);

        // Generate unique booking reference
        String bookingReference = generateBookingReferenceAndUpdateCounters(passengerName, flightNumber);
        lastBookingRef = bookingReference; // Store for debugging and error tracking

        // Save booking details
        String actualBookingRef = repository.saveBookingDetails(passengerName,
                String.format("%s on %s for %d passengers", flightNumber, departureDate.toLocalDate(), passengerCount),
                finalPrice, _bookingDate);

        // Log the booking activity
        auditLogger.logBookingActivity("Flight Booked", actualBookingRef,
                String.format("Passenger: %s, Flight: %s", passengerName, flightNumber));

        auditLogger.recordPricingCalculation(
                String.format("Base: %s, Weekday: %s, Seasonal: %s, Special: %s, Discount: %s",
                        basePrice, weekdayMultiplier, seasonalBonus, specialRequestSurcharge, discountAmount[0]),
                finalPrice, String.format("%s on %s", flightNumber, departureDate.toLocalDate()));

        // Partner notification
        if (shouldNotifyPartnerBasedOnAirlineAndState(airlineCode)) {
            partnerNotifier.notifyPartnerAboutBooking(airlineCode, actualBookingRef, finalPrice,
                    passengerName, String.format("%s departing %s", flightNumber, departureDate), false);

            // Handle special requests
            if (!specialRequests.isEmpty() && requiresSpecialNotification(airlineCode, specialRequests)) {
                partnerNotifier.validateAndNotifySpecialRequests(airlineCode, specialRequests, actualBookingRef);
            }
        }

        String bookingStatus = determineBookingStatusFromGlobalState(finalPrice, passengerCount);
        partnerNotifier.updatePartnerBookingStatus(airlineCode, actualBookingRef, bookingStatus);

        temporaryData.put("lastBookingPrice", finalPrice);
        temporaryData.put("lastBookingDate", _bookingDate);
        isProcessingBooking = false;

        return new Booking(actualBookingRef, passengerName, flightNumber, departureDate,
                passengerCount, airlineCode, finalPrice, specialRequests, _bookingDate, bookingStatus);
    }

    private int calculateRetriesBasedOnBookingCount() {
        temporaryData.put("calculationCount",
                ((Integer) temporaryData.getOrDefault("calculationCount", 0)) + 1);
        return Math.min(5, bookingCounter / 10 + 1);
    }

    private BigDecimal calculateTaxRateBasedOnGlobalState(String airlineCode) {
        BigDecimal baseRate = new BigDecimal("1.18");
        if (temporaryData.containsKey("lastFailureReason")) {
            baseRate = baseRate.add(new BigDecimal("0.05"));
        }

        temporaryData.put("lastProcessedAirline", airlineCode);

        return baseRate;
    }

    private Map<String, BigDecimal> buildAirlineFeesFromTemporaryData(String airlineCode) {
        Map<String, BigDecimal> fees = new HashMap<>();

        if (temporaryData.containsKey("lastBookingPrice")) {
            BigDecimal lastPrice = (BigDecimal) temporaryData.get("lastBookingPrice");
            fees.put(airlineCode, lastPrice.multiply(new BigDecimal("0.02")));
        } else {
            fees.put(airlineCode, new BigDecimal("25.0"));
        }

        if (bookingCounter > 10) {
            fees.put(airlineCode, fees.get(airlineCode).add(new BigDecimal("10.0")));
        }

        return fees;
    }

    private String determineRegionFromFlightNumber(String flightNumber) {
        temporaryData.put("lastFlightNumber", flightNumber);

        if (flightNumber.startsWith("AA") || flightNumber.startsWith("UA")) {
            return "US";
        } else if (flightNumber.startsWith("BA") || flightNumber.startsWith("VS")) {
            return "UK";
        } else {
            return "INTL";
        }
    }

    private BigDecimal getHistoricalAverageFromRepository(BookingRepository repository, String flightNumber) {
        temporaryData.put("historicalLookupCount",
                ((Integer) temporaryData.getOrDefault("historicalLookupCount", 0)) + 1);

        return new BigDecimal(450.0 + (flightNumber.length() * 10));
    }

    private String modifyConnectionStringForAvailability(String originalConnectionString, String flightNumber) {
        String modified = originalConnectionString.replace("FlightBookings",
                "FlightAvailability_" + flightNumber.substring(0, 2));

        temporaryData.put("lastConnectionString", modified);

        return modified;
    }

    private BigDecimal getWeekdayMultiplierAndUpdateGlobalState(LocalDateTime departureDate) {
        temporaryData.put("lastDepartureDate", departureDate);

        DayOfWeek dayOfWeek = departureDate.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            temporaryData.put("isPeakDay", true);
            return new BigDecimal("1.25");
        } else if (dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.WEDNESDAY) {
            temporaryData.put("isPeakDay", false);
            return new BigDecimal("0.9");
        }

        temporaryData.put("isPeakDay", false);
        return new BigDecimal("1.0");
    }

    private BigDecimal calculateSeasonalBonusWithSideEffects(LocalDateTime departureDate, String flightNumber) {
        int month = departureDate.getMonthValue();
        BigDecimal bonus = BigDecimal.ZERO;

        if (month >= 6 && month <= 8) {
            bonus = new BigDecimal("50.0");
            temporaryData.put("currentSeason", "Summer");
        } else if (month >= 12 || month <= 2) {
            bonus = new BigDecimal("75.0");
            temporaryData.put("currentSeason", "Winter");
        } else {
            bonus = new BigDecimal("25.0");
            temporaryData.put("currentSeason", "OffPeak");
        }

        if (bookingCounter % 5 == 0) {
            bonus = bonus.add(new BigDecimal("20.0"));
            temporaryData.put("luckyBooking", true);
        }

        return bonus;
    }

    private BigDecimal processSpecialRequestsAndCalculateSurcharge(String specialRequests, String airlineCode) {
        BigDecimal surcharge = BigDecimal.ZERO;

        if (specialRequests.isEmpty()) {
            return surcharge;
        }

        temporaryData.put("hasSpecialRequests", true);
        temporaryData.put("specialRequestsCount", specialRequests.split(",").length);

        if (specialRequests.contains("wheelchair")) {
            surcharge = surcharge.add("AA".equals(airlineCode) ? BigDecimal.ZERO : new BigDecimal("25.0"));
        }

        if (specialRequests.contains("meal")) {
            surcharge = surcharge.add("BA".equals(airlineCode) ? new BigDecimal("15.0") : new BigDecimal("20.0"));
        }

        if (specialRequests.contains("seat")) {
            surcharge = surcharge.add(new BigDecimal("35.0"));
        }

        return surcharge;
    }

    private String determineSmtpServerFromAirlineCode(String airlineCode) {
        temporaryData.put("lastSmtpLookup", _bookingDate);

        switch (airlineCode) {
            case "AA":
                return "smtp.american.com";
            case "UA":
                return "smtp.united.com";
            case "BA":
                return "smtp.britishairways.com";
            default:
                return "smtp.generic-airline.com";
        }
    }

    private String calculateLogDirectoryFromBookingCount() {
        String baseDir = "/var/logs/BookingLogs";

        if (bookingCounter > 100) {
            baseDir += "/HighVolume";
        } else if (bookingCounter > 50) {
            baseDir += "/MediumVolume";
        } else {
            baseDir += "/LowVolume";
        }

        temporaryData.put("currentLogDirectory", baseDir);

        return baseDir;
    }

    private String generateBookingReferenceAndUpdateCounters(String passengerName, String flightNumber) {
        String reference = String.format("%s%04d%s",
                flightNumber,
                bookingCounter,
                passengerName.substring(0, Math.min(3, passengerName.length())).toUpperCase());

        temporaryData.put("lastGeneratedReference", reference);
        temporaryData.put("referenceGenerationCount",
                ((Integer) temporaryData.getOrDefault("referenceGenerationCount", 0)) + 1);

        return reference;
    }

    private boolean shouldNotifyPartnerBasedOnAirlineAndState(String airlineCode) {
        if (temporaryData.containsKey("lastFailureReason")) {
            return false;
        }

        if (bookingCounter < 5) {
            return "AA".equals(airlineCode);
        }

        return true;
    }

    private boolean requiresSpecialNotification(String airlineCode, String specialRequests) {
        if ("BA".equals(airlineCode) && specialRequests.contains("meal")) {
            return true;
        }

        if ("AA".equals(airlineCode) && specialRequests.contains("wheelchair")) {
            return true;
        }

        return specialRequests.split(",").length > 2;
    }

    private String determineBookingStatusFromGlobalState(BigDecimal finalPrice, int passengerCount) {
        String status = "CONFIRMED";

        if (temporaryData.containsKey("isPeakDay") && (Boolean) temporaryData.get("isPeakDay")) {
            status = "CONFIRMED_PEAK";
        }

        if (finalPrice.compareTo(new BigDecimal("1000")) > 0) {
            status = "CONFIRMED_PREMIUM";
        }

        if (passengerCount > 5) {
            status = "CONFIRMED_GROUP";
        }

        temporaryData.put("lastBookingStatus", status);

        return status;
    }
}