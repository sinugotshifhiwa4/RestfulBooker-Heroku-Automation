//package com.restfulbooker.api.payload.requestBuilder;
//
//import com.restfulbooker.api.payload.payloads.Booking;
//import com.restfulbooker.api.payload.payloads.BookingDates;
//import com.restfulbooker.config.jackson.JsonMapperConfiguration;
//import net.datafaker.Faker;
//
//import java.time.LocalDate;
//import java.util.Objects;
//
//public class BookingBuilder {
//
//    private static final Faker faker = new Faker();
//    private static String cachedFirstName;
//    private static String cachedLastName;
//    private static Integer cachedTotalPrice;
//    private static BookingDates cachedBookingDates;
//    private static String cachedAdditionalNeeds;
//
//    public static Booking createBookingRequest() {
//        Booking booking = JsonMapperConfiguration.convertToJsonAndBack(buildBookingRequest(), Booking.class);
//        // Cache the names for future use
//        cachedFirstName = Objects.requireNonNull(booking).getFirstName();
//        cachedLastName = Objects.requireNonNull(booking).getLastName();
//        cachedTotalPrice = booking.getTotalPrice();
//        cachedBookingDates = booking.getBookingDates();
//        cachedAdditionalNeeds = booking.getAdditionalNeeds();
//        return booking;
//    }
//
//    public static Booking updateBookingRequest() {
//        return JsonMapperConfiguration.convertToJsonAndBack(buildUpdatedBookingRequest(), Booking.class);
//    }
//
//    public static Booking partiallyUpdateBookingRequest() {
//        return JsonMapperConfiguration.convertToJsonAndBack(buildPartiallyUpdatedBookingRequest(), Booking.class);
//    }
//
//    private static Booking buildBookingRequest() {
//        // Generate new values only if they haven't been cached yet
//        if (cachedFirstName == null || cachedLastName == null) {
//            cachedFirstName = faker.name().firstName();
//            cachedLastName = faker.name().lastName();
//            cachedTotalPrice = faker.number().numberBetween(100, 1000);
//            cachedAdditionalNeeds = "Breakfast";
//            // BookingDates will be created in buildBookingDates()
//        }
//
//        return Booking.builder()
//                .firstName(cachedFirstName)
//                .lastName(cachedLastName)
//                .totalPrice(cachedTotalPrice)
//                .depositPaid(false)
//                .bookingDates(buildBookingDates())
//                .additionalNeeds(cachedAdditionalNeeds)
//                .build();
//    }
//
//    private static BookingDates buildBookingDates() {
//        if (cachedBookingDates == null) {
//            cachedBookingDates = BookingDates.builder()
//                    .checkIn(LocalDate.now())
//                    .checkOut(LocalDate.now().plusDays(3))
//                    .build();
//        }
//        return cachedBookingDates;
//    }
//
//    private static Booking buildUpdatedBookingRequest() {
//        // Use the cached names
//        return Booking.builder()
//                .firstName(cachedFirstName)
//                .lastName(cachedLastName)
//                .totalPrice(faker.number().numberBetween(1970, 4000))
//                .depositPaid(true)
//                .bookingDates(buildUpdatedBookingDates())
//                .additionalNeeds("Extra Towels")
//                .build();
//    }
//
//    private static BookingDates buildUpdatedBookingDates() {
//        return BookingDates.builder()
//                .checkIn(LocalDate.now())
//                .checkOut(LocalDate.now().plusDays(11))
//                .build();
//    }
//
//    private static Booking buildPartiallyUpdatedBookingRequest() {
//        // Cache everything except depositPaid
//        return Booking.builder()
//                .firstName(cachedFirstName)
//                .lastName(cachedLastName)
//                .totalPrice(cachedTotalPrice)
//                .depositPaid(true)
//                .bookingDates(cachedBookingDates)
//                .additionalNeeds(cachedAdditionalNeeds)
//                .build();
//    }
//}


package com.restfulbooker.api.payload.requestBuilder;

import com.restfulbooker.api.endpoints.BookingEndpoints;
import com.restfulbooker.api.payload.payloads.Booking;
import com.restfulbooker.api.payload.payloads.BookingDates;
import com.restfulbooker.config.jackson.JsonMapperConfiguration;
import com.restfulbooker.testDataStorage.TestContextIds;
import com.restfulbooker.testDataStorage.TestContextStore;
import com.restfulbooker.utils.ErrorHandler;
import com.restfulbooker.utils.validation.ResponseValidator;
import io.restassured.response.Response;
import net.datafaker.Faker;

import java.time.LocalDate;
import java.util.Objects;

public class BookingBuilder {

    private static final Faker faker = new Faker();
    private static String cachedFirstName;
    private static String cachedLastName;
    private static Integer cachedTotalPrice;
    private static BookingDates cachedBookingDates;
    private static String cachedAdditionalNeeds;
    private static final String BOOKING_ID = "bookingid";
    private static final BookingEndpoints bookingEndpoints = new BookingEndpoints();

    public static Booking createBookingRequest() {
        Booking booking = JsonMapperConfiguration.convertToJsonAndBack(buildBookingRequest(), Booking.class);
        // Cache the names for future use
        cachedFirstName = Objects.requireNonNull(booking).getFirstName();
        cachedLastName = Objects.requireNonNull(booking).getLastName();
        cachedTotalPrice = booking.getTotalPrice();
        cachedBookingDates = booking.getBookingDates();
        cachedAdditionalNeeds = booking.getAdditionalNeeds();
        return booking;
    }

    public static Booking updateBookingRequest() {
        return JsonMapperConfiguration.convertToJsonAndBack(buildUpdatedBookingRequest(), Booking.class);
    }

    public static Booking partiallyUpdateBookingRequest() {
        // First, get the current booking state from the server
        Booking currentBooking = getCurrentBookingFromServer();
        return JsonMapperConfiguration.convertToJsonAndBack(buildPartiallyUpdatedBookingRequest(currentBooking), Booking.class);
    }

    /**
     * Retrieves the current booking from the server
     * @return The current booking as stored on the server
     */
    private static Booking getCurrentBookingFromServer() {
        try {
            int bookingId = TestContextStore.getContextValueAsInt(
                    TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId(),
                    BOOKING_ID
            );

            Response response = bookingEndpoints.getBookingById(bookingId);
            ResponseValidator.assertResponseStatusCode(response, 200);

            // Convert response to Booking object
            return response.as(Booking.class);
        } catch (Exception error) {
            ErrorHandler.logError(error, "getCurrentBookingFromServer", "Failed to get current booking from server");
            throw error;
        }
    }

    private static Booking buildBookingRequest() {
        // Generate new values only if they haven't been cached yet
        if (cachedFirstName == null || cachedLastName == null) {
            cachedFirstName = faker.name().firstName();
            cachedLastName = faker.name().lastName();
            cachedTotalPrice = faker.number().numberBetween(100, 1000);
            cachedAdditionalNeeds = "Breakfast";
            // BookingDates will be created in buildBookingDates()
        }

        return Booking.builder()
                .firstName(cachedFirstName)
                .lastName(cachedLastName)
                .totalPrice(cachedTotalPrice)
                .depositPaid(false)
                .bookingDates(buildBookingDates())
                .additionalNeeds(cachedAdditionalNeeds)
                .build();
    }

    private static BookingDates buildBookingDates() {
        if (cachedBookingDates == null) {
            cachedBookingDates = BookingDates.builder()
                    .checkIn(LocalDate.now())
                    .checkOut(LocalDate.now().plusDays(3))
                    .build();
        }
        return cachedBookingDates;
    }

    private static Booking buildUpdatedBookingRequest() {
        // Use the cached names
        return Booking.builder()
                .firstName(cachedFirstName)
                .lastName(cachedLastName)
                .totalPrice(faker.number().numberBetween(1970, 4000))
                .depositPaid(true)
                .bookingDates(buildUpdatedBookingDates())
                .additionalNeeds("Extra Towels")
                .build();
    }

    private static BookingDates buildUpdatedBookingDates() {
        return BookingDates.builder()
                .checkIn(LocalDate.now())
                .checkOut(LocalDate.now().plusDays(11))
                .build();
    }

    private static Booking buildPartiallyUpdatedBookingRequest(Booking currentBooking) {
        // If we couldn't get the current booking from server, fall back to cached values
        if (currentBooking == null) {
            return Booking.builder()
                    .firstName(cachedFirstName)
                    .lastName(cachedLastName)
                    .totalPrice(cachedTotalPrice)
                    .depositPaid(true) // Only changing this field
                    .bookingDates(cachedBookingDates)
                    .additionalNeeds(cachedAdditionalNeeds)
                    .build();
        }

        // Use current booking values from server and just update depositPaid
        return Booking.builder()
                .firstName(currentBooking.getFirstName())
                .lastName(currentBooking.getLastName())
                .totalPrice(currentBooking.getTotalPrice())
                .depositPaid(true) // This is the only field we want to partially update
                .bookingDates(currentBooking.getBookingDates())
                .additionalNeeds(currentBooking.getAdditionalNeeds())
                .build();
    }
}