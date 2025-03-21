package com.restfulbooker.api.endpoints;

import com.restfulbooker.api.core.HttpClient;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

public class BookingEndpoints {

    private final HttpClient httpClient = new HttpClient();

    public Response generateAuthenticationToken(Object payload){ // replace obj with class
        return httpClient.sendPostRequest(
                Routes.getAuthUrl(),
                payload,
                null,
                null
        );
    }

    public Response createBooking(Object payload){
        return httpClient.sendPostRequest(
                Routes.getBookingsUrl(),
                payload,
                null,
                null
        );
    }

    public Response getBookingById(int id){
        return httpClient.sendGetRequest(
                Routes.getBookingUrl(id),
                null,
                null
        );
    }

    public Response updateBookingById(Object payload, int id, String token){
        return httpClient.sendPutRequest(
                Routes.getBookingUrl(id),
                payload,
                null,
                createAuthHeader(token)
        );
    }

    public Response partialUpdateBookingById(Object payload, int id, String token){
        return httpClient.sendPatchRequest(
                Routes.getBookingUrl(id),
                payload,
                null,
                createAuthHeader(token)
        );
    }

    private Map<String, String> createAuthHeader(String token) {
        if (token == null) return null;
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "token=" + token);
        return headers;
    }
}
