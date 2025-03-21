package com.restfulbooker.api.endpoints;

import com.restfulbooker.config.properties.PropertyConfigConstants;
import com.restfulbooker.config.properties.PropertyFileConfigManager;
import com.restfulbooker.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Utility class for managing API endpoints and URL construction.
 * Handles the creation of properly formatted URLs for various API operations.
 */
public final class Routes {

    private static final Logger logger = LoggerUtils.getLogger(Routes.class);
    private static final String BASE_URL_PROPERTY = "API_BASE_URL";

    // API endpoint paths - relative to base URL
    private static final String ROOT_ENDPOINT = "";
    private static final String BOOKING_ENDPOINT = "booking";
    private static final String AUTH_ENDPOINT = "auth";

    // Cache the base URI to avoid repeated parsing
    private static URI baseUriCache;

    private Routes() {
        throw new UnsupportedOperationException("Routes is a utility class and cannot be instantiated.");
    }

    /**
     * Retrieves the base URI from configuration properties.
     */
    private static URI getBaseUri() {
        if (baseUriCache != null) {
            return baseUriCache;
        }

        try {
            String baseUrl = PropertyFileConfigManager.getConfiguration(
                    PropertyConfigConstants.Environment.UAT.getDisplayName(),
                    PropertyConfigConstants.PropertiesFilePath.UAT.getFullPath()
            ).getProperty(BASE_URL_PROPERTY);

            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                throw new IllegalStateException("Base URL property is missing or empty");
            }

            baseUriCache = new URI(baseUrl);
            return baseUriCache;
        } catch (URISyntaxException e) {
            logger.error("Invalid base URL syntax", e);
            throw new IllegalStateException("Invalid base URL", e);
        } catch (Exception e) {
            logger.error("Failed to retrieve API Base URL", e);
            throw new IllegalStateException("Failed to retrieve API Base URL", e);
        }
    }

    public static String getRootUrl() {
        return buildUri(ROOT_ENDPOINT).toString();
    }

    public static String getBookingsUrl() {
        return buildUri(BOOKING_ENDPOINT).toString();
    }

    public static String getBookingUrl(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Booking ID must be a positive integer");
        }
        return buildUri(BOOKING_ENDPOINT + "/" + id).toString();
    }

    public static String getAuthUrl() {
        return buildUri(AUTH_ENDPOINT).toString();
    }

    /**
     * Constructs a URI by resolving the given path against the base URL.
     */
    private static URI buildUri(String path) {
        Objects.requireNonNull(path, "Path cannot be null");

        try {
            URI baseUri = getBaseUri();
            return baseUri.resolve(path);
        } catch (Exception e) {
            logger.error("Failed to build URI with path: {}", path, e);
            throw new IllegalStateException("Failed to construct URI for path: " + path, e);
        }
    }

    /**
     * Validates if a given string represents a valid booking ID.
     *
     * @param id Booking identifier to check
     * @return true if id is valid (non-null and numeric), false otherwise
     */
    public static boolean isValidId(String id) {
        return id != null && !id.isEmpty() && id.matches("^[1-9]\\d*$");
    }
}