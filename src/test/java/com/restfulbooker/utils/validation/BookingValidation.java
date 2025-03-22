package com.restfulbooker.utils.validation;

import io.restassured.response.Response;
import org.testng.Assert;

import java.util.Arrays;
import java.util.List;

/**
 * Dedicated validation class for Booking API responses.
 * Contains methods specific to validating booking-related responses.
 */
public class BookingValidation {

    public static List<String> getCreateBookingResponseFields() {
        return Arrays.asList(
                "bookingid",
                "booking.firstname",
                "booking.lastname",
                "booking.totalprice",
                "booking.depositpaid",
                "booking.bookingdates.checkin",
                "booking.bookingdates.checkout",
                "booking.additionalneeds"
        );
    }

    public static List<String> getBookingObjectFields() {
        return Arrays.asList(
                "firstname",
                "lastname",
                "totalprice",
                "depositpaid",
                "bookingdates.checkin",
                "bookingdates.checkout",
                "additionalneeds"
        );
    }

    /**
     * Validates an authentication token response
     *
     * @param response The authentication API response
     * @return The extracted token
     */
    public static String validateAuthenticationResponse(Response response) {
        // Basic response validation
        ResponseValidator.validateBasicResponse(response, 200, "application/json", 3000);

        // Token-specific validations
        ResponseValidator.assertFieldPresent(response, "token");

        // Extract and validate token
        String token = ResponseValidator.extractResponseField(response, "token", String.class);
        Assert.assertTrue(token.length() > 10, "Token should have sufficient length for security");

        return token;
    }

    /**
     * Validates a create booking response
     *
     * @param response The create booking API response
     * @return The extracted booking ID
     */
    public static int validateCreateBookingResponse(Response response) {
        // Basic response validation
        ResponseValidator.validateBasicResponse(response, 200, "application/json", 3000);

        // Validate booking-specific fields
        ResponseValidator.assertFieldsPresent(response, BookingValidation.getCreateBookingResponseFields());

        // Additional booking validations
        ResponseValidator.assertNumericRange(response, "booking.totalprice", 0, Integer.MAX_VALUE);
        ResponseValidator.assertFieldType(response, "booking.depositpaid", Boolean.class);
        ResponseValidator.assertDateFormat(response, "booking.bookingdates.checkin", "\\d{4}-\\d{2}-\\d{2}");
        ResponseValidator.assertDateFormat(response, "booking.bookingdates.checkout", "\\d{4}-\\d{2}-\\d{2}");

        // Extract and return booking ID
        return ResponseValidator.extractResponseField(response, "bookingid", Integer.class);
    }

    /**
     * Validates a get booking response
     *
     * @param response The get booking API response
     */
    public static void validateGetBookingResponse(Response response) {
        // Basic response validation
        ResponseValidator.validateBasicResponse(response, 200, "application/json", 3000);

        // Validate booking-specific fields
        ResponseValidator.assertFieldsPresent(response, BookingValidation.getBookingObjectFields());

        // Additional booking validations
        ResponseValidator.assertFieldNotNull(response, "firstname");
        ResponseValidator.assertFieldNotNull(response, "lastname");
        ResponseValidator.assertFieldType(response, "totalprice", Integer.class);
        ResponseValidator.assertFieldType(response, "depositpaid", Boolean.class);
        ResponseValidator.assertDateFormat(response, "bookingdates.checkin", "\\d{4}-\\d{2}-\\d{2}");
        ResponseValidator.assertDateFormat(response, "bookingdates.checkout", "\\d{4}-\\d{2}-\\d{2}");

        // Validate checkIn date is before checkout date
        String checkInDate = ResponseValidator.extractResponseField(response, "bookingdates.checkin");
        String checkOutDate = ResponseValidator.extractResponseField(response, "bookingdates.checkout");
        Assert.assertTrue(checkInDate.compareTo(checkOutDate) < 0, "Checkin date should be before checkout date");
    }

    /**
     * Validates an update booking response
     *
     * @param response The update booking API response
     */
    public static void validateUpdateBookingResponse(Response response) {
        // Basic response validation
        ResponseValidator.validateBasicResponse(response, 200, "application/json", 3000);

        // Validate booking-specific fields
        ResponseValidator.assertFieldsPresent(response, BookingValidation.getBookingObjectFields());

        // Additional booking validations
        ResponseValidator.assertFieldNotNull(response, "firstname");
        ResponseValidator.assertFieldNotNull(response, "lastname");
        ResponseValidator.assertFieldType(response, "totalprice", Integer.class);
        ResponseValidator.assertFieldType(response, "depositpaid", Boolean.class);
        ResponseValidator.assertDateFormat(response, "bookingdates.checkin", "\\d{4}-\\d{2}-\\d{2}");
        ResponseValidator.assertDateFormat(response, "bookingdates.checkout", "\\d{4}-\\d{2}-\\d{2}");
    }

    /**
     * Validates a partial update booking response
     *
     * @param response The partial update booking API response
     */
    public static void validatePartialUpdateBookingResponse(Response response) {
        // Basic response validation
        ResponseValidator.validateBasicResponse(response, 200, "application/json", 3000);

        // Validate booking-specific fields
        ResponseValidator.assertFieldsPresent(response, BookingValidation.getBookingObjectFields());

        // For partial updates, only basic field presence is sufficient
        ResponseValidator.assertFieldNotNull(response, "firstname");
        ResponseValidator.assertFieldNotNull(response, "lastname");
    }

    /**
     * Validates a delete booking response
     *
     * @param response The delete booking API response
     */
    public static void validateDeleteBookingResponse(Response response) {
        // Basic response validation
        ResponseValidator.validateBasicResponse(response, 201, "text/plain", 3000);

        // Specific validation for deletion
        ResponseValidator.assertBodyContains(response, "Created");
    }

    /**
     * Validates a get all bookings response
     *
     * @param response The get all bookings API response
     */
    public static void validateGetAllBookingsResponse(Response response) {
        // Basic response validation
        ResponseValidator.validateBasicResponse(response, 200, "application/json", 5000); // Higher timeout for list

        // Validate list-specific structure
        ResponseValidator.assertFieldType(response, "$", java.util.List.class);

        // Extract and validate list
        java.util.List<?> bookings = response.jsonPath().getList("$");
        Assert.assertNotNull(bookings, "Bookings list should not be null");

        // Validate structure if list is not empty
        if (!bookings.isEmpty()) {
            ResponseValidator.assertFieldPresent(response, "[0].bookingid");
        }

        // Validate reasonable size
        int maxExpectedItems = 10000; // Adjust based on your API's expected scale
        Assert.assertTrue(bookings.size() <= maxExpectedItems, "Number of bookings should not exceed reasonable limit");
    }
}