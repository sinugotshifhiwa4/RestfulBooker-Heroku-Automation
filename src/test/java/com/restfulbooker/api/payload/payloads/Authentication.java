package com.restfulbooker.api.payload.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request payload for authentication.
 *
 * @ Data : Generates getter, setter, toString, equals, and hashCode methods.
 * @ Builder : Enables the Builder pattern for object creation.
 * @ NoArgsConstructor : Generates a no-argument constructor.
 * @ AllArgsConstructor : Generates a constructor with all arguments.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Authentication {

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;
}
