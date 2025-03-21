package com.restfulbooker.utils.constants;

public class Encryption {

    private static final String AUTH_USERNAME = "AUTHENTICATION_USERNAME";
    private static final String AUTH_PASSWORD = "AUTHENTICATION_PASSWORD";

    public static String getAuthenticationUsername() {
        return AUTH_USERNAME;
    }

    public static String getAuthenticationPassword() {
        return AUTH_PASSWORD;
    }
}
