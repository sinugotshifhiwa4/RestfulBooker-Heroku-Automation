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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BookingTests {

    private final BookingEndpoints bookingEndpoints = new BookingEndpoints();
    private static final Logger logger = LoggerUtils.getLogger(BookingTests.class);
    private static final String TOKEN = "token";
    private static final String BOOKING_ID = "bookingid";


    @BeforeMethod
    public void setup(){
        TestContextStore.initializeContext(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId());
    }

    @Test(groups = {"sanity"}, priority = 1)
    public void createAuthenticationToken() throws Exception {
        try{
            // Send auth request
            Response response = bookingEndpoints.generateAuthenticationToken(
                    AuthenticationBuilder.createAuthenticationRequest(
                            EnvironmentConfigConstants.Environment.UAT.getDisplayName(),
                            EnvironmentConfigConstants.EnvironmentFilePath.UAT.getFilename(),
                            EnvironmentConfigConstants.EnvironmentSecretKey.UAT.getKeyName(),
                            Encryption.getAuthenticationUsername(), Encryption.getAuthenticationPassword()
                    )
            );

            ApiResponseValidator.assertResponseStatusCode(response, 200);

            // Store token for future use
            String retrievedToken = ApiResponseValidator.extractResponseField(response, TOKEN, String.class);
            TestContextStore.storeContextValue(TestContextIds.HOTEL_BOOKING_ID_ONE.getTestId(), TOKEN, retrievedToken);
            logger.info("Retrieved token saved");

            logger.info("AuthToken generated and stored successfully");
        } catch (Exception error) {
            ErrorHandler.logError(error, "createAuthenticationToken", "Failed to create authentication token");
            throw error;
        }
    }

    @Test(groups = {"sanity"}, priority = 2)
    public void createNewBooking() throws Exception {
        try{
            // Send new booking request
            Response response = bookingEndpoints.createBooking(
                    BookingBuilder.createBookingRequest()
            );

            ApiResponseValidator.assertResponseStatusCode(response, 200);

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
}
