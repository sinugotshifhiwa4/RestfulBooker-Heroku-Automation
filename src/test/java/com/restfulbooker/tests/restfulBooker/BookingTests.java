package com.restfulbooker.tests.restfulBooker;

import com.restfulbooker.api.endpoints.BookingEndpoints;
import com.restfulbooker.api.payload.requestBuilder.AuthenticationBuilder;
import com.restfulbooker.api.payload.requestBuilder.BookingBuilder;
import com.restfulbooker.config.environments.EnvironmentConfigConstants;
import com.restfulbooker.testDataStorage.TestContextIds;
import com.restfulbooker.testDataStorage.TestContextStore;
import com.restfulbooker.utils.ErrorHandler;
import com.restfulbooker.utils.LoggerUtils;
import com.restfulbooker.utils.constants.Encryption;
import com.restfulbooker.utils.validation.BookingValidation;
import io.restassured.response.Response;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BookingTests {

    private final BookingEndpoints bookingEndpoints = new BookingEndpoints();
    private static final Logger logger = LoggerUtils.getLogger(BookingTests.class);
    private static final String TOKEN = "token";
    private static final String BOOKING_ID = "bookingid";

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        TestContextStore.initializeContext(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId());
    }

    @Test(groups = {"sanity"}, priority = 1)
    public void createAuthenticationToken() {
        try {
            // Send auth request
            Response response = bookingEndpoints.generateAuthenticationToken(
                    AuthenticationBuilder.createAuthenticationRequest(
                            EnvironmentConfigConstants.Environment.UAT.getDisplayName(),
                            EnvironmentConfigConstants.EnvironmentFilePath.UAT.getFilename(),
                            EnvironmentConfigConstants.EnvironmentSecretKey.UAT.getKeyName(),
                            Encryption.getAuthenticationUsername(),
                            Encryption.getAuthenticationPassword())
            );

            // Validate response and extract token
            String retrievedToken = BookingValidation.validateAuthenticationResponse(response);

            // Store token for future use
            TestContextStore.storeContextValue(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId(), TOKEN, retrievedToken);
            logger.info("AuthToken generated and stored successfully");
        } catch (Exception error) {
            ErrorHandler.logError(error, "createAuthenticationToken", "Failed to create authentication token");
            throw error;
        }
    }

    @Test(groups = {"sanity"}, priority = 2)
    public void createNewBooking() {
        try {
            // Send new booking request
            Response response = bookingEndpoints.createBooking(BookingBuilder.createBookingRequest());

            // Validate response and extract booking ID
            int retrievedBookingId = BookingValidation.validateCreateBookingResponse(response);

            // Store booking ID for future use
            TestContextStore.storeContextValue(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId(), BOOKING_ID, retrievedBookingId);
            logger.info("Booking created successfully with ID: {}", retrievedBookingId);
        } catch (Exception error) {
            ErrorHandler.logError(error, "createNewBooking", "Failed to create new booking");
            throw error;
        }
    }

    @Test(groups = {"sanity"}, priority = 3, dependsOnMethods = {"createNewBooking"})
    public void getBookingDetailsById() {
        try {
            // Send get booking request
            Response response = bookingEndpoints.getBookingById(
                    TestContextStore.getContextValueAsInt(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId(), BOOKING_ID)
            );

            // Validate get booking response
            BookingValidation.validateGetBookingResponse(response);
            logger.info("Get booking details successful");
        } catch (Exception error) {
            ErrorHandler.logError(error, "getBookingDetailsById", "Failed to get booking details by id");
            throw error;
        }
    }

    @Test(groups = {"sanity"}, priority = 4, dependsOnMethods = {"createNewBooking"})
    public void updateBookingDetailsById() {
        try {
            // Send update booking request
            Response response = bookingEndpoints.updateBookingById(
                    BookingBuilder.updateBookingRequest(),
                    TestContextStore.getContextValueAsInt(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId(), BOOKING_ID),
                    TestContextStore.getContextValue(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId(), TOKEN)
            );

            // Validate update booking response
            BookingValidation.validateUpdateBookingResponse(response);
            logger.info("Update booking successful");
        } catch (Exception error) {
            ErrorHandler.logError(error, "updateBookingDetailsById", "Failed to update booking details by id");
            throw error;
        }
    }

    @Test(groups = {"sanity"}, priority = 5, dependsOnMethods = {"createNewBooking"})
    public void partiallyUpdateBookingDetailsById() {
        try {
            // Send partially update booking request
            Response response = bookingEndpoints.partialUpdateBookingById(
                    BookingBuilder.partiallyUpdateBookingRequest(),
                    TestContextStore.getContextValueAsInt(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId(), BOOKING_ID),
                    TestContextStore.getContextValue(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId(), TOKEN)
            );

            // Validate partial update booking response
            BookingValidation.validatePartialUpdateBookingResponse(response);
            logger.info("Partially update booking successful");
        } catch (Exception error) {
            ErrorHandler.logError(error, "partiallyUpdateBookingDetailsById", "Failed to partially update booking details by id");
            throw error;
        }
    }

    @Test(groups = {"sanity"}, priority = 6, dependsOnMethods = {"createNewBooking"})
    public void deleteBookingDetailsById() {
        try {
            // Send delete booking request
            Response response = bookingEndpoints.deleteBookingById(
                    TestContextStore.getContextValueAsInt(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId(), BOOKING_ID),
                    TestContextStore.getContextValue(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId(), TOKEN)
            );

            // Validate delete booking response
            BookingValidation.validateDeleteBookingResponse(response);
            logger.info("Delete booking successful");
        } catch (Exception error) {
            ErrorHandler.logError(error, "deleteBookingDetailsById", "Failed to delete booking details by id");
            throw error;
        }
    }

    @Test(groups = {"sanity"}, priority = 7)
    public void getAllBookings() {
        try {
            // Send get all booking request
            Response response = bookingEndpoints.getAllBooking();

            // Validate get all bookings response
            BookingValidation.validateGetAllBookingsResponse(response);
            logger.info("Get all bookings successful");
        } catch (Exception error) {
            ErrorHandler.logError(error, "getAllBookings", "Failed to get all booking details");
            throw error;
        }
    }
}