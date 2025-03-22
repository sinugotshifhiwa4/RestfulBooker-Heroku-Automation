package com.restfulbooker.tests.restfulBooker;

import com.restfulbooker.api.endpoints.BookingEndpoints;
import com.restfulbooker.api.payload.requestBuilder.AuthenticationBuilder;
import com.restfulbooker.api.payload.requestBuilder.BookingBuilder;
import com.restfulbooker.api.utils.ApiResponseValidator;
import com.restfulbooker.config.environments.EnvironmentConfigConstants;
import com.restfulbooker.testDataStorage.TestContextIds;
import com.restfulbooker.testDataStorage.TestContextStore;
import com.restfulbooker.utils.ErrorHandler;
import com.restfulbooker.utils.LoggerUtils;
import com.restfulbooker.utils.constants.Encryption;
import io.restassured.response.Response;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class BookingTests {

    private final BookingEndpoints bookingEndpoints = new BookingEndpoints();
    private static final Logger logger = LoggerUtils.getLogger(BookingTests.class);
    private static final String TOKEN = "token";
    private static final String BOOKING_ID = "bookingid";


    @BeforeMethod
    public void setup() {
        TestContextStore.initializeContext(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId());
    }

    @Test(groups = {"sanity"}, priority = 1)
    public void createAuthenticationToken() {
        try {
            // Send auth request
            Response response = bookingEndpoints.generateAuthenticationToken(
                    AuthenticationBuilder.createAuthenticationRequest(EnvironmentConfigConstants.Environment.UAT.getDisplayName(),
                            EnvironmentConfigConstants.EnvironmentFilePath.UAT.getFilename(),
                            EnvironmentConfigConstants.EnvironmentSecretKey.UAT.getKeyName(),
                            Encryption.getAuthenticationUsername(), Encryption.getAuthenticationPassword())
            );

            ApiResponseValidator.assertResponseStatusCode(response, 200);
            // Add after retrieving token
            ApiResponseValidator.assertContentType(response, "application/json");
            ApiResponseValidator.assertResponseTime(response, 3000); // 3 seconds max
            ApiResponseValidator.assertFieldPresent(response, TOKEN);

            // Store token for future use
            String retrievedToken = ApiResponseValidator.extractResponseField(response, TOKEN, String.class);
            Assert.assertTrue(retrievedToken.length() > 10, "Token should have sufficient length for security");
            TestContextStore.storeContextValue(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId(), TOKEN, retrievedToken);
            logger.info("Retrieved token saved");

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

            ApiResponseValidator.assertResponseStatusCode(response, 200);

            // Validate response schema and content using the method you provided
            ApiResponseValidator.assertFieldsPresent(response, ApiResponseValidator.getCreateBookingResponseFields());

            // Add after fields validation
            ApiResponseValidator.assertContentType(response, "application/json");
            ApiResponseValidator.assertResponseTime(response, 3000);
            ApiResponseValidator.assertNumericRange(response, "booking.totalprice", 0, Integer.MAX_VALUE);
            ApiResponseValidator.assertFieldType(response, "booking.depositpaid", Boolean.class);
            ApiResponseValidator.assertDateFormat(response, "booking.bookingdates.checkin", "\\d{4}-\\d{2}-\\d{2}");
            ApiResponseValidator.assertDateFormat(response, "booking.bookingdates.checkout", "\\d{4}-\\d{2}-\\d{2}");


            // Save Booking Id
            int retrievedBookingId = ApiResponseValidator.extractResponseField(response, BOOKING_ID, Integer.class);
            TestContextStore.storeContextValue(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId(), BOOKING_ID, retrievedBookingId);
            logger.info("Retrieved booking id saved");

            logger.info("Booking created successfully");
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

            ApiResponseValidator.assertResponseStatusCode(response, 200);
            response.prettyPrint();

            // Validate response schema and content using the method you provided
            ApiResponseValidator.assertFieldsPresent(response, ApiResponseValidator.getBookingObjectFields());
            // Add after fields validation
            ApiResponseValidator.assertContentType(response, "application/json");
            ApiResponseValidator.assertResponseTime(response, 3000);
            ApiResponseValidator.assertFieldNotNull(response, "firstname");
            ApiResponseValidator.assertFieldNotNull(response, "lastname");
            ApiResponseValidator.assertFieldType(response, "totalprice", Integer.class);
            ApiResponseValidator.assertFieldType(response, "depositpaid", Boolean.class);
            ApiResponseValidator.assertDateFormat(response, "bookingdates.checkin", "\\d{4}-\\d{2}-\\d{2}");
            ApiResponseValidator.assertDateFormat(response, "bookingdates.checkout", "\\d{4}-\\d{2}-\\d{2}");

            // Validate checkIn date is before checkout date
            String checkInDate = ApiResponseValidator.extractResponseField(response, "bookingdates.checkin");
            String checkOutDate = ApiResponseValidator.extractResponseField(response, "bookingdates.checkout");
            Assert.assertTrue(checkInDate.compareTo(checkOutDate) < 0, "Checkin date should be before checkout date");

            logger.info("Get booking successfully");

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

            ApiResponseValidator.assertResponseStatusCode(response, 200);

            // Validate response schema and content using the method you provided
            ApiResponseValidator.assertFieldsPresent(response, ApiResponseValidator.getBookingObjectFields());

            // Add after fields validation
            ApiResponseValidator.assertContentType(response, "application/json");
            ApiResponseValidator.assertResponseTime(response, 3000);
            ApiResponseValidator.assertFieldNotNull(response, "firstname");
            ApiResponseValidator.assertFieldNotNull(response, "lastname");
            ApiResponseValidator.assertFieldType(response, "totalprice", Integer.class);
            ApiResponseValidator.assertFieldType(response, "depositpaid", Boolean.class);
            ApiResponseValidator.assertDateFormat(response, "bookingdates.checkin", "\\d{4}-\\d{2}-\\d{2}");
            ApiResponseValidator.assertDateFormat(response, "bookingdates.checkout", "\\d{4}-\\d{2}-\\d{2}");

            logger.info("Update booking successfully");

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

            ApiResponseValidator.assertResponseStatusCode(response, 200);

            // Validate response schema and content using the method you provided
            ApiResponseValidator.assertFieldsPresent(response, ApiResponseValidator.getBookingObjectFields());

            // Add after fields validation
            ApiResponseValidator.assertContentType(response, "application/json");
            ApiResponseValidator.assertResponseTime(response, 3000);
            ApiResponseValidator.assertFieldNotNull(response, "firstname");
            ApiResponseValidator.assertFieldNotNull(response, "lastname");

            logger.info("Partially update booking successfully");

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

            ApiResponseValidator.assertResponseStatusCode(response, 201);

            // Add after status code validation
            ApiResponseValidator.assertBodyContains(response, "Created");
            ApiResponseValidator.assertContentType(response, "text/plain");
            ApiResponseValidator.assertResponseTime(response, 3000);

            // create validation that response is Created

            logger.info("Delete booking successfully");

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

            ApiResponseValidator.assertResponseStatusCode(response, 200);

            // Add after status code validation
            ApiResponseValidator.assertContentType(response, "application/json");
            ApiResponseValidator.assertResponseTime(response, 5000); // Longer timeout for list operation
            ApiResponseValidator.assertFieldType(response, "$", List.class);

            // Verify the response contains a list
            List<?> bookings = response.jsonPath().getList("$");
            Assert.assertNotNull(bookings, "Bookings list should not be null");

            // Validate at least some basic structure if list is not empty
            if (!bookings.isEmpty()) {
                ApiResponseValidator.assertFieldPresent(response, "[0].bookingid");
            }

            // Validate the response doesn't exceed a reasonable size for performance
            int maxExpectedItems = 10000; // Adjust based on your API's expected scale
            Assert.assertTrue(bookings.size() <= maxExpectedItems, "Number of bookings should not exceed reasonable limit");

            logger.info("Get all booking successfully");

        } catch (Exception error) {
            ErrorHandler.logError(error, "getAllBookings", "Failed to get all booking details by id");
            throw error;
        }
    }

}
