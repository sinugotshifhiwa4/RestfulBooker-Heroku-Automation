package com.restfulbooker.testDataStorage;

public enum TestContextIds {
    HOTEL_BOOKING_ID_ONE("BOOKING_ID_ONE"),
    HOTEL_BOOKING_ID_TWO("BOOKING_ID_TWO");

    private final String value;

    TestContextIds(String value) {
        this.value = value;
    }

    /**
     * Retrieves the string value of the test ID.
     *
     * @return the test ID as a string
     */
    public String getTestId() {
        return value;
    }
}
