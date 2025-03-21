package com.restfulbooker.api.payload.payloadBuilder;

import com.restfulbooker.api.payload.payloads.Authentication;
import com.restfulbooker.config.jackson.JsonMapperConfiguration;
import com.restfulbooker.crypto.services.EnvironmentCryptoManager;
import com.restfulbooker.utils.ErrorHandler;

import java.util.List;
import java.util.Optional;

/**
 * Builder class for creating Authentication payload objects.
 * Handles secure retrieval and processing of authentication credentials.
 */
public class AuthenticationBuilder {


    /**
     * Creates an authentication request with credentials retrieved from environment variables.
     *
     * @param configName                The configuration name to use for retrieving credentials
     * @param envName                   The environment name
     * @param environmentSecretKeyType  The type of secret key used in the environment
     * @param requiredKeys              The keys required for authentication (typically username and password)
     * @return                          An Authentication object with the necessary credentials
     * @throws RuntimeException         If authentication request creation fails
     */
    public static Authentication createAuthenticationRequest(
            String configName,
            String envName,
            String environmentSecretKeyType,
            String... requiredKeys
    ) {
        try {
            // Fetch and build auth token data
            Authentication authRequest = buildAuthenticationRequest(configName, envName, environmentSecretKeyType, requiredKeys);

            // Validate the built request
            validateAuthRequest(authRequest);

            // Serialize and Deserialize
            return JsonMapperConfiguration.convertToJsonAndBack(authRequest, Authentication.class);

        } catch (Exception error) {
            ErrorHandler.logError(error, "createAuthenticationRequest", "Failed to create authentication request");
            throw new RuntimeException("Authentication request creation failed", error);
        }
    }

    /**
     * Builds an Authentication object from decrypted credentials.
     *
     * @param configName                The configuration name to use
     * @param envName                   The environment name
     * @param environmentSecretKeyType  The type of secret key used in the environment
     * @param requiredKeys              The keys required for authentication
     * @return                          An Authentication object with the credentials
     * @throws IllegalArgumentException If required credentials are missing or invalid
     */
    private static Authentication buildAuthenticationRequest(
            String configName,
            String envName,
            String environmentSecretKeyType,
            String... requiredKeys
    ) {
        if (requiredKeys.length < 2) {
            throw new IllegalArgumentException("Both username and password keys are required");
        }

        try {
            // Fetch and decrypt credentials
            List<String> decryptedValues = EnvironmentCryptoManager.decryptEnvironmentVariables(
                    configName, envName, environmentSecretKeyType, requiredKeys);

            return Authentication.builder()
                    .username(decryptedValues.getFirst())
                    .password(decryptedValues.getLast())
                    .build();

        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Missing required credentials", e);
        } catch (Exception error) {
            ErrorHandler.logError(error, "buildAuthenticationRequest", "Failed to build authentication request");
            throw error;
        }
    }

    /**
     * Validates that an authentication request has necessary credentials.
     *
     * @param authRequest  The Authentication object to validate
     * @throws IllegalArgumentException If the credentials are invalid
     */
    private static void validateAuthRequest(Authentication authRequest) {
        Optional.ofNullable(authRequest)
                .filter(auth -> auth.getUsername() != null && !auth.getUsername().isEmpty())
                .filter(auth -> auth.getPassword() != null && !auth.getPassword().isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Authentication request contains empty credentials"));
    }
}