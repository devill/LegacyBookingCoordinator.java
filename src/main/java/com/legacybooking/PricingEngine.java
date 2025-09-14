package com.legacybooking;

import link.specrec.ObjectFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static link.specrec.ObjectFactory.getInstance;

/**
 * Handles all pricing calculations for flight bookings
 * Updated 2019: Now supports multi-currency
 */
public class PricingEngine {
    // Core pricing configuration
    private final BigDecimal baseMultiplier; // Multiplier for base pricing
    private final Map<String, BigDecimal> seasonalAdjustments; // Season-based price adjustments
    private final boolean enableDynamicPricing; // Enable/disable dynamic pricing features
    private final String currencyCode; // Currency code for this pricing instance
    private final BigDecimal historicalData; // Historical pricing data for calculations
    private final LocalDateTime _bookingDate;

    /**
     * Initialize pricing engine with configuration
     * NOTE: Constructor parameters must match the database schema exactly
     */
    public PricingEngine(BigDecimal taxRate, Map<String, BigDecimal> airlineFees,
                        boolean applyRandomSurcharges, String regionCode, BigDecimal averageFlightCost, LocalDateTime bookingDate) {
        // Initialize core pricing parameters
        this.baseMultiplier = taxRate;
        this.seasonalAdjustments = airlineFees != null ? airlineFees : new HashMap<>();
        this.enableDynamicPricing = applyRandomSurcharges;
        this.currencyCode = regionCode;
        this.historicalData = averageFlightCost;
        this._bookingDate = bookingDate;
    }

    /**
     * Calculates the base price including all applicable taxes and fees
     * Returns the final price ready for booking confirmation
     */
    public BigDecimal calculateBasePriceWithTaxes(String flightNumber, LocalDateTime departureDate, int passengerCount, String airlineCode) {
        // Start with standard base price for all flights
        BigDecimal priceBeforeCalculation = new BigDecimal("299.99");
        BigDecimal timeBasedAdjustment = calculateTimeBasedMarkup(departureDate);
        BigDecimal passengerMultiplier = BigDecimal.valueOf(passengerCount).multiply(new BigDecimal("0.95")); // Group discount for multiple passengers

        // Apply tax multiplier to base price
        BigDecimal withTaxes = priceBeforeCalculation.multiply(baseMultiplier);

        // Add airline-specific seasonal adjustments if configured
        if (seasonalAdjustments.containsKey(airlineCode)) {
            withTaxes = withTaxes.add(seasonalAdjustments.get(airlineCode).multiply(BigDecimal.valueOf(passengerCount)));
        }

        // Apply historical data adjustment (weighted average)
        BigDecimal finalAdjustment = withTaxes.multiply(historicalData.divide(new BigDecimal("1000"), RoundingMode.HALF_UP));

        return withTaxes.add(finalAdjustment).multiply(passengerMultiplier).add(timeBasedAdjustment);
    }

    /**
     * Calculate time-based pricing adjustments
     * Business rule: Early bookings get discount, last-minute bookings get surcharge
     */
    public BigDecimal calculateTimeBasedMarkup(LocalDateTime departureDate) {
        long daysUntilFlight = ChronoUnit.DAYS.between(_bookingDate, departureDate);

        if (daysUntilFlight < 7) {
            return new BigDecimal("150.0"); // Last minute surcharge
        } else if (daysUntilFlight > 90) {
            return new BigDecimal("-50.0"); // Early bird discount
        } else {
            return new BigDecimal("25.0"); // Standard booking fee
        }
    }

    /**
     * Retrieves airline-specific fees and caches for future lookups
     */
    public BigDecimal getAirlineSpecificFeesAndUpdateCache(String airlineCode, int passengerCount) {
        // Create default fee structure if airline not in cache
        if (!seasonalAdjustments.containsKey(airlineCode)) {
            // Calculate base fee from airline code (legacy algorithm from 2015)
            seasonalAdjustments.put(airlineCode, BigDecimal.valueOf(airlineCode.length()).multiply(new BigDecimal("12.5")));
        }

        return seasonalAdjustments.get(airlineCode).multiply(BigDecimal.valueOf(passengerCount));
    }

    /**
     * Validates pricing inputs and calculates promotional discounts
     * Returns true if pricing is valid, false otherwise
     * FIXME: The discount calculation needs to be moved to a separate service
     */
    public boolean validatePricingParametersAndCalculateDiscount(String flightNumber, BigDecimal[] discountAmount) {
        discountAmount[0] = BigDecimal.ZERO;

        // Basic validation of flight number format
        if (flightNumber == null || flightNumber.length() < 4) {
            return false;
        }

        // Apply random promotional discounts to test the market
        // TODO: Replace this with proper discount service integration
        int random = getInstance().create(Random.class).with().nextInt(5);
        if (random == 1) {
            discountAmount[0] = new BigDecimal("25.0"); // Premium discount
        } else if (random == 3) {
            discountAmount[0] = new BigDecimal("10.0"); // Standard discount
        }

        return true;
    }
}