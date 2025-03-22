package com.restfulbooker.utils.validation;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.Assert;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Utility class for validating REST API responses.
 * Provides methods for extracting data and asserting various properties of responses.
 */
public class ResponseValidator {

    private static final String NULL_RESPONSE_MESSAGE = "Response cannot be null.";
    private static final String NULL_FIELD_MESSAGE = "Field name cannot be null or empty.";
    private static final String NULL_HEADERS_MESSAGE = "Expected headers map cannot be null.";
    private static final String NULL_FIELDS_MESSAGE = "Field names list cannot be null.";

    private ResponseValidator() {
        // Private constructor to prevent instantiation of utility class
    }

    // ===== High-Level Validation Methods =====

    /**
     * Performs basic validation on an API response
     *
     * @param response The API response to validate
     * @param expectedStatusCode The expected HTTP status code
     * @param expectedContentType The expected content type
     * @param maxResponseTime The maximum acceptable response time in milliseconds
     */
    public static void validateBasicResponse(Response response, int expectedStatusCode,
                                             String expectedContentType, long maxResponseTime) {
        // Status code validation
        assertResponseStatusCode(response, expectedStatusCode);

        // Content type validation
        if (expectedContentType != null) {
            assertContentType(response, expectedContentType);
        }

        // Performance validation
        assertResponseTime(response, maxResponseTime);
    }

    /**
     * Validates that a field exists and is not null
     *
     * @param response The API response
     * @param fieldPath The JSON path to the field
     */
    public static void validateRequiredField(Response response, String fieldPath) {
        assertFieldPresent(response, fieldPath);
        assertFieldNotNull(response, fieldPath);
    }

    /**
     * Validates multiple required fields in one call
     *
     * @param response The API response
     * @param fieldPaths Array of field paths to validate
     */
    public static void validateRequiredFields(Response response, String... fieldPaths) {
        for (String fieldPath : fieldPaths) {
            validateRequiredField(response, fieldPath);
        }
    }

    /**
     * Validates a response for error conditions
     *
     * @param response The API response
     * @param expectedStatusCode The expected error status code
     * @param expectedErrorCode The expected error code
     * @param expectedErrorMessage The expected error message
     */
    public static void validateErrorResponse(Response response, int expectedStatusCode,
                                             String expectedErrorCode, String expectedErrorMessage) {
        assertErrorResponse(response, expectedStatusCode, expectedErrorCode, expectedErrorMessage);
    }

    // ===== Data Extraction Methods =====

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

    // ===== Assertion Methods =====

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
     * Asserts that a specific field exists in the response.
     *
     * @param response  The REST response
     * @param fieldPath The JSON path to the field
     * @throws IllegalArgumentException if response is null
     */
    public static void assertFieldPresent(Response response, String fieldPath) {
        validateResponse(response);
        validateFieldName(fieldPath);

        Assert.assertNotNull(
                response.jsonPath().get(fieldPath),
                String.format("Expected field at path '%s' not found in response", fieldPath)
        );
    }

    /**
     * Asserts that multiple fields have the expected values.
     *
     * @param response       The REST response
     * @param expectedValues Map of field paths and their expected values
     * @throws IllegalArgumentException if response or expectedValues is null
     */
    public static void assertFieldValues(Response response, Map<String, Object> expectedValues) {
        validateResponse(response);
        Objects.requireNonNull(expectedValues, "Expected values map cannot be null");

        expectedValues.forEach((fieldPath, expectedValue) -> {
            Object actualValue = response.jsonPath().get(fieldPath);
            Assert.assertEquals(
                    actualValue,
                    expectedValue,
                    String.format("Field at path '%s' has value '%s' but expected '%s'",
                            fieldPath, actualValue, expectedValue)
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
     * Asserts that the response body matches a JSON schema file located in the classpath.
     *
     * @param response   The REST response
     * @param schemaPath The path to the JSON schema file in the classpath
     * @throws IllegalArgumentException if response or schemaPath is null
     */
    public static void assertJsonSchemaFile(Response response, String schemaPath) {
        validateResponse(response);
        Objects.requireNonNull(schemaPath, "Schema path cannot be null");

        try (InputStream schemaStream = ResponseValidator.class.getResourceAsStream(schemaPath)) {
            response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchema(Objects.requireNonNull(schemaStream)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to validate schema: " + e.getMessage(), e);
        }
    }

    /**
     * Asserts that the content type of the response contains the expected value.
     *
     * @param response           The REST response
     * @param expectedContentType The expected content type
     * @throws IllegalArgumentException if response is null
     */
    public static void assertContentType(Response response, String expectedContentType) {
        validateResponse(response);
        Objects.requireNonNull(expectedContentType, "Expected content type cannot be null");

        String actualContentType = response.getContentType();
        Assert.assertTrue(
                actualContentType.contains(expectedContentType),
                String.format("Content type mismatch. Expected to contain: '%s', but got: '%s'",
                        expectedContentType, actualContentType)
        );
    }

    /**
     * Asserts that the response body contains the expected text.
     *
     * @param response      The REST response
     * @param expectedText  The text that should be present in the response body
     * @throws IllegalArgumentException if response or expectedText is null
     */
    public static void assertBodyContains(Response response, String expectedText) {
        validateResponse(response);
        Objects.requireNonNull(expectedText, "Expected text cannot be null");

        String responseBody = response.getBody().asString();
        Assert.assertTrue(
                responseBody.contains(expectedText),
                String.format("Response body does not contain expected text: '%s'", expectedText)
        );
    }

    /**
     * Asserts that an array in the response has the expected size.
     *
     * @param response      The REST response
     * @param arrayPath     The JSON path to the array
     * @param expectedSize  The expected size of the array
     * @throws IllegalArgumentException if response is null
     */
    public static void assertArraySize(Response response, String arrayPath, int expectedSize) {
        validateResponse(response);
        validateFieldName(arrayPath);

        List<?> array = response.jsonPath().getList(arrayPath);
        Assert.assertNotNull(array, "Array not found at path: " + arrayPath);
        Assert.assertEquals(
                array.size(),
                expectedSize,
                String.format("Array size mismatch at path '%s'. Expected: %d, but got: %d",
                        arrayPath, expectedSize, array.size())
        );
    }

    /**
     * Asserts that a field in the response is of the expected type.
     *
     * @param response      The REST response
     * @param fieldPath     The JSON path to the field
     * @param expectedType  The expected type of the field
     * @throws IllegalArgumentException if response or expectedType is null
     */
    public static void assertFieldType(Response response, String fieldPath, Class<?> expectedType) {
        validateResponse(response);
        validateFieldName(fieldPath);
        Objects.requireNonNull(expectedType, "Expected type cannot be null");

        Object value = response.jsonPath().get(fieldPath);
        Assert.assertNotNull(value, "Field not found at path: " + fieldPath);
        Assert.assertTrue(
                expectedType.isInstance(value),
                String.format("Field at path '%s' is of type '%s' but expected '%s'",
                        fieldPath, value.getClass().getName(), expectedType.getName())
        );
    }

    /**
     * Asserts that a field in the response is null.
     *
     * @param response   The REST response
     * @param fieldPath  The JSON path to the field
     * @throws IllegalArgumentException if response is null
     */
    public static void assertFieldNull(Response response, String fieldPath) {
        validateResponse(response);
        validateFieldName(fieldPath);

        Object value = response.jsonPath().get(fieldPath);
        Assert.assertNull(
                value,
                String.format("Field at path '%s' is not null: %s", fieldPath, value)
        );
    }

    /**
     * Asserts that a field in the response is not null.
     *
     * @param response   The REST response
     * @param fieldPath  The JSON path to the field
     * @throws IllegalArgumentException if response is null
     */
    public static void assertFieldNotNull(Response response, String fieldPath) {
        validateResponse(response);
        validateFieldName(fieldPath);

        Object value = response.jsonPath().get(fieldPath);
        Assert.assertNotNull(
                value,
                String.format("Field at path '%s' is null", fieldPath)
        );
    }

    /**
     * Asserts that a field in the response has the expected value.
     *
     * @param response       The REST response
     * @param fieldPath      The JSON path to the field
     * @param expectedValue  The expected value of the field
     * @throws IllegalArgumentException if response or expectedValue is null
     */
    public static void assertFieldValue(Response response, String fieldPath, Object expectedValue) {
        validateResponse(response);
        validateFieldName(fieldPath);
        Objects.requireNonNull(expectedValue, "Expected value cannot be null");

        Object actualValue = response.jsonPath().get(fieldPath);
        Assert.assertEquals(
                actualValue,
                expectedValue,
                String.format("Field at path '%s' has value '%s' but expected '%s'",
                        fieldPath, actualValue, expectedValue)
        );
    }

    /**
     * Asserts that the response represents an error with the expected status code, error code, and message.
     *
     * @param response             The REST response
     * @param expectedStatusCode   The expected HTTP status code
     * @param expectedErrorCode    The expected error code in the response (can be null if not applicable)
     * @param expectedErrorMessage The expected error message in the response (can be null if not applicable)
     * @throws IllegalArgumentException if response is null
     */
    public static void assertErrorResponse(Response response, int expectedStatusCode, String expectedErrorCode, String expectedErrorMessage) {
        validateResponse(response);

        assertResponseStatusCode(response, expectedStatusCode);

        if (expectedErrorCode != null) {
            assertFieldValue(response, "errorCode", expectedErrorCode);
        }

        if (expectedErrorMessage != null) {
            assertFieldValue(response, "message", expectedErrorMessage);
        }
    }

    /**
     * Asserts that the response time is within the specified range.
     *
     * @param response         The REST response
     * @param minResponseTime  The minimum acceptable response time in milliseconds
     * @param maxResponseTime  The maximum acceptable response time in milliseconds
     * @throws IllegalArgumentException if response is null
     */
    public static void assertResponseTimeRange(Response response, long minResponseTime, long maxResponseTime) {
        validateResponse(response);

        long actualResponseTime = response.getTime();
        Assert.assertTrue(
                actualResponseTime >= minResponseTime && actualResponseTime <= maxResponseTime,
                String.format("Response time %d ms not within expected range (%d ms to %d ms)",
                        actualResponseTime, minResponseTime, maxResponseTime)
        );
    }

    /**
     * Asserts that a date field in the response matches the expected format.
     *
     * @param response      The REST response
     * @param fieldPath     The JSON path to the date field
     * @param formatRegex   The regular expression that the date should match
     * @throws IllegalArgumentException if response is null
     */
    public static void assertDateFormat(Response response, String fieldPath, String formatRegex) {
        validateResponse(response);
        validateFieldName(fieldPath);
        Objects.requireNonNull(formatRegex, "Format regex cannot be null");

        String dateValue = extractResponseField(response, fieldPath);
        Assert.assertTrue(
                dateValue.matches(formatRegex),
                String.format("Date at path '%s' with value '%s' does not match expected format regex '%s'",
                        fieldPath, dateValue, formatRegex)
        );
    }

    /**
     * Asserts that a numeric field in the response is within the specified range.
     *
     * @param response  The REST response
     * @param fieldPath The JSON path to the numeric field
     * @param minValue  The minimum acceptable value
     * @param maxValue  The maximum acceptable value
     * @throws IllegalArgumentException if response is null
     */
    public static void assertNumericRange(Response response, String fieldPath, double minValue, double maxValue) {
        validateResponse(response);
        validateFieldName(fieldPath);

        Double value = extractResponseField(response, fieldPath, Double.class);
        Assert.assertTrue(
                value >= minValue && value <= maxValue,
                String.format("Numeric value %f at path '%s' is not within expected range (%f to %f)",
                        value, fieldPath, minValue, maxValue)
        );
    }

    /**
     * Asserts that the response represents a successful request with a valid response body.
     * This is a convenience method that combines common validations.
     *
     * @param response     The REST response
     * @param contentType  The expected content type (optional, can be null)
     * @throws IllegalArgumentException if response is null
     */
    public static void assertSuccessfulResponse(Response response, String contentType) {
        validateResponse(response);

        int statusCode = response.getStatusCode();
        Assert.assertTrue(
                statusCode >= 200 && statusCode < 300,
                String.format("Response status code %d is not a success code", statusCode)
        );

        if (contentType != null) {
            assertContentType(response, contentType);
        }

        String responseBody = response.getBody().asString();
        Assert.assertFalse(
                responseBody == null || responseBody.isEmpty(),
                "Response body is empty for a successful request"
        );
    }

    // ===== Helper Methods =====

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