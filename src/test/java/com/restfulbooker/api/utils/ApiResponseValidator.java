package com.restfulbooker.api.utils;

import io.restassured.response.Response;
import org.testng.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Utility class for validating REST API responses.
 * Provides methods for extracting data and asserting various properties of responses.
 */
public class ApiResponseValidator {

    private static final String NULL_RESPONSE_MESSAGE = "Response cannot be null.";
    private static final String NULL_FIELD_MESSAGE = "Field name cannot be null or empty.";
    private static final String NULL_HEADERS_MESSAGE = "Expected headers map cannot be null.";
    private static final String NULL_FIELDS_MESSAGE = "Field names list cannot be null.";

    private ApiResponseValidator() {
        // Private constructor to prevent instantiation of utility class
    }

    /**
     * Extracts a field value from the response JSON.
     *
     * @param response  The REST response
     * @param fieldPath The JSON path to the field to extract
     * @return The value of the specified field
     * @throws IllegalArgumentException if response is null
     * @throws IllegalStateException    if field is not found or empty
     */
    public static String extractResponseField(Response response, String fieldPath) {
        validateResponse(response);
        validateFieldName(fieldPath);

        String value = response.jsonPath().getString(fieldPath);

        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "The field '%s' was not found or is empty in the response: %s",
                    fieldPath, response.getBody().asString()
            ));
        }
        return value;
    }

    /**
     * Extracts a field as a specific type from the response JSON.
     *
     * @param response  The REST response
     * @param fieldPath The JSON path to the field to extract
     * @param clazz     The class type to cast the result to
     * @param <T>       The type parameter for the return value
     * @return The value of the specified field cast to the specified type
     * @throws IllegalArgumentException if response is null
     * @throws IllegalStateException    if field is not found
     */
    public static <T> T extractResponseField(Response response, String fieldPath, Class<T> clazz) {
        validateResponse(response);
        validateFieldName(fieldPath);
        Objects.requireNonNull(clazz, "Class type cannot be null");

        T value = response.jsonPath().getObject(fieldPath, clazz);

        if (value == null) {
            throw new IllegalStateException(String.format(
                    "The field '%s' was not found in the response: %s",
                    fieldPath, response.getBody().asString()
            ));
        }
        return value;
    }

    /**
     * Extracts a list of items of a specific type from the response JSON.
     *
     * @param response  The REST response
     * @param fieldPath The JSON path to the list to extract
     * @param itemClass The class type of the list items
     * @param <T>       The type parameter for the list items
     * @return The list of specified type
     * @throws IllegalArgumentException if response is null
     * @throws IllegalStateException    if field is not found
     */
    public static <T> List<T> extractResponseList(Response response, String fieldPath, Class<T> itemClass) {
        validateResponse(response);
        validateFieldName(fieldPath);
        Objects.requireNonNull(itemClass, "Item class type cannot be null");

        List<T> list = response.jsonPath().getList(fieldPath, itemClass);

        if (list == null || list.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "The list at path '%s' was not found or is empty in the response: %s",
                    fieldPath, response.getBody().asString()
            ));
        }
        return list;
    }

    /**
     * Asserts that the response status code matches the expected value.
     *
     * @param response           The REST response
     * @param expectedStatusCode The expected HTTP status code
     * @throws IllegalArgumentException if response is null
     */
    public static void assertResponseStatusCode(Response response, int expectedStatusCode) {
        validateResponse(response);

        int actualStatusCode = response.getStatusCode();
        Assert.assertEquals(
                actualStatusCode,
                expectedStatusCode,
                String.format(
                        "Expected status code: %d, but received: %d%nResponse Body:%n%s",
                        expectedStatusCode,
                        actualStatusCode,
                        response.getBody().asString()
                )
        );
    }

    /**
     * Asserts that the response contains specific headers.
     *
     * @param response        The REST response
     * @param expectedHeaders Map of expected header names and values
     * @throws IllegalArgumentException if response or expectedHeaders is null
     */
    public static void assertResponseHeaders(Response response, Map<String, String> expectedHeaders) {
        validateResponse(response);
        Objects.requireNonNull(expectedHeaders, NULL_HEADERS_MESSAGE);

        expectedHeaders.forEach((headerName, expectedValue) -> {
            String actualValue = response.getHeader(headerName);
            Assert.assertEquals(
                    actualValue,
                    expectedValue,
                    String.format("Header '%s' mismatch. Expected: '%s', but got: '%s'",
                            headerName, expectedValue, actualValue)
            );
        });
    }

    /**
     * Asserts that the response time is less than the specified maximum.
     *
     * @param response         The REST response
     * @param maxResponseTime Maximum acceptable response time in milliseconds
     * @throws IllegalArgumentException if response is null
     */
    public static void assertResponseTime(Response response, long maxResponseTime) {
        validateResponse(response);

        long actualResponseTime = response.getTime();
        Assert.assertTrue(
                actualResponseTime <= maxResponseTime,
                String.format("Response time %d ms exceeded maximum allowed time of %d ms",
                        actualResponseTime, maxResponseTime)
        );
    }

    /**
     * Asserts that all specified fields exist in the response.
     *
     * @param response     The REST response
     * @param fieldPaths   List of JSON paths that should exist in the response
     * @throws IllegalArgumentException if response or fieldPaths is null
     */
    public static void assertFieldsPresent(Response response, List<String> fieldPaths) {
        validateResponse(response);
        Objects.requireNonNull(fieldPaths, NULL_FIELDS_MESSAGE);

        fieldPaths.forEach(fieldPath -> {
            Assert.assertNotNull(
                    response.jsonPath().get(fieldPath),
                    String.format("Expected field at path '%s' not found in response", fieldPath)
            );
        });
    }

    /**
     * Asserts that a field in the response satisfies a given condition.
     *
     * @param response    The REST response
     * @param fieldPath   JSON path to the field to validate
     * @param condition   Predicate that defines the condition to check
     * @param errorMessage Error message to display if the condition fails
     * @param <T>         The type parameter for the field value
     * @throws IllegalArgumentException if response is null
     */
    public static <T> void assertFieldCondition(Response response, String fieldPath,
                                                Predicate<T> condition, String errorMessage, Class<T> clazz) {
        validateResponse(response);
        validateFieldName(fieldPath);
        Objects.requireNonNull(condition, "Condition predicate cannot be null");
        Objects.requireNonNull(errorMessage, "Error message cannot be null");

        T value = response.jsonPath().getObject(fieldPath, clazz);

        Assert.assertTrue(
                condition.test(value),
                String.format("%s. Field: '%s', Value: '%s'", errorMessage, fieldPath, value)
        );
    }

    /**
     * Asserts that the response body contains the expected JSON structure.
     *
     * @param response       The REST response
     * @param expectedSchema The expected JSON schema to validate against
     * @throws IllegalArgumentException if response or expectedSchema is null
     */
    public static void assertJsonSchema(Response response, String expectedSchema) {
        validateResponse(response);
        Objects.requireNonNull(expectedSchema, "Expected schema cannot be null");

        response.then().assertThat().body(org.hamcrest.Matchers.matchesPattern(expectedSchema));
    }

    /**
     * Validates that the response object is not null.
     *
     * @param response The REST response to validate
     * @throws IllegalArgumentException if response is null
     */
    private static void validateResponse(Response response) {
        Objects.requireNonNull(response, NULL_RESPONSE_MESSAGE);
    }

    /**
     * Validates that the field name is not null or empty.
     *
     * @param fieldName The field name to validate
     * @throws IllegalArgumentException if fieldName is null or empty
     */
    private static void validateFieldName(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new IllegalArgumentException(NULL_FIELD_MESSAGE);
        }
    }
}