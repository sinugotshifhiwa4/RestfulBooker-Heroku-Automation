package com.restfulbooker.api.payload.payloadBuilder;

import com.restfulbooker.api.payload.payloads.Booking;
import com.restfulbooker.api.payload.payloads.BookingDates;
import com.restfulbooker.config.jackson.JsonMapperConfiguration;
import net.datafaker.Faker;

import java.time.LocalDate;

public class BookingBuilder {

    private static final Faker faker = new Faker();

    public static Booking createBookingRequest() {
        return JsonMapperConfiguration.convertToJsonAndBack(buildBookingRequest(), Booking.class);
    }

    private static Booking buildBookingRequest() {
        return Booking.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .totalPrice(faker.number().numberBetween(100, 1000))
                .depositPaid(faker.bool().bool())
                .bookingDates(buildBookingDates())
                .additionalNeeds("Breakfast")
                .build();
    }

    private static BookingDates buildBookingDates() {
        return BookingDates.builder()
                .checkIn(LocalDate.now())
                .checkOut(LocalDate.now().plusDays(3))
                .build();
    }

}
