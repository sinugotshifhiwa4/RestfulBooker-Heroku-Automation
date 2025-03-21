package com.restfulbooker.api.core;

import com.restfulbooker.utils.ErrorHandler;
import com.restfulbooker.utils.LoggerUtils;
import io.restassured.response.Response;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;


public class HttpClient {

    private static final Logger logger = LoggerUtils.getLogger(HttpClient.class);
    private final Map<String, String> defaultHeaders;

    public HttpClient() {
        this.defaultHeaders = new HashMap<>();
        this.defaultHeaders.put("Content-Type", "application/json");
        this.defaultHeaders.put("Accept", "application/json");
    }

    private Map<String, String> createHeaders(String authorizationHeader, Map<String, String> additionalHeaders) {
        Map<String, String> headers = new HashMap<>(this.defaultHeaders);

        Optional.ofNullable(authorizationHeader)
                .filter(auth -> !auth.isEmpty())
                .ifPresent(auth -> headers.put("Authorization", "Bearer " + auth));

        Optional.ofNullable(additionalHeaders).ifPresent(headers::putAll);

        return headers;
    }

    public Response sendRequest(HttpMethod method, String endpoint, Object payload,
                                String authorizationHeader, Map<String, String> additionalHeaders) {
        try {
            Map<String, String> headers = createHeaders(authorizationHeader, additionalHeaders);

            return switch (method) {
                case POST -> given().headers(headers).body(payload).when().post(endpoint);
                case PUT -> given().headers(headers).body(payload).when().put(endpoint);
                case PATCH -> given().headers(headers).body(payload).when().patch(endpoint);
                case GET -> given().headers(headers).when().get(endpoint);
                case DELETE -> given().headers(headers).when().delete(endpoint);
            };
        } catch (Exception error) {
            ErrorHandler.logError(error, "sendRequest", "Failed to send http request");
            throw error;
        }
    }

    // Simplified convenience methods
    public Response sendPostRequest(String endpoint, Object payload, String authToken, Map<String, String> headers) {
        return sendRequest(HttpMethod.POST, endpoint, payload, authToken, headers);
    }

    public Response sendPutRequest(String endpoint, Object payload, String authToken, Map<String, String> headers) {
        return sendRequest(HttpMethod.PUT, endpoint, payload, authToken, headers);
    }

    public Response sendPatchRequest(String endpoint, Object payload, String authToken, Map<String, String> headers) {
        return sendRequest(HttpMethod.PATCH, endpoint, payload, authToken, headers);
    }

    public Response sendGetRequest(String endpoint, String authToken, Map<String, String> headers) {
        return sendRequest(HttpMethod.GET, endpoint, null, authToken, headers);
    }

    public Response sendDeleteRequest(String endpoint, String authToken, Map<String, String> headers) {
        return sendRequest(HttpMethod.DELETE, endpoint, null, authToken, headers);
    }

    public enum HttpMethod {
        POST, GET, PUT, PATCH, DELETE
    }
}