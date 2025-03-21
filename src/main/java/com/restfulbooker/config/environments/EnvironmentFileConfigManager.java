package com.restfulbooker.config.environments;

import com.restfulbooker.config.AbstractConfigManager;
import com.restfulbooker.utils.Base64Utils;
import com.restfulbooker.utils.ErrorHandler;
import io.github.cdimascio.dotenv.Dotenv;

import javax.crypto.SecretKey;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.restfulbooker.utils.ErrorHandler.validateParameters;


public class EnvironmentFileConfigManager extends AbstractConfigManager<EnvironmentFileConfigManager> {

    // Cache for EnvironmentConfigManager instances
    private static final Map<String, EnvironmentFileConfigManager> configManagerCache = new ConcurrentHashMap<>();

    private Dotenv dotenv;

    public EnvironmentFileConfigManager(String configurationDisplayName, String envFileName) {
        super(configurationDisplayName, envFileName);
        try {
            loadEnvironment();
        } catch (Exception error) {
            logger.error("Failed to load environment '{}' with name '{}'", envFileName, configurationDisplayName);
            ErrorHandler.logError(error, "EnvironmentFileConfigManager Constructor", "Failed to load dotenv variables");
            throw error;
        }
    }

    private static EnvironmentFileConfigManager loadConfiguration(String configurationDisplayName, String envFileName) {
        try {
            return new EnvironmentFileConfigManager(configurationDisplayName, envFileName);
        } catch (Exception error) {
            ErrorHandler.logError(error, "loadConfiguration", "Failed to create environment configuration");
            throw error;
        }
    }

    private void loadEnvironment() {
        this.dotenv = Dotenv.configure()
                .directory(EnvironmentConfigConstants.getEnvironmentDirectoryPath())
                .filename(configSource)
                .load();
    }

    @Override
    public String getProperty(String key) {
        try {
            // First check system environment variables
            String systemValue = System.getenv(key);
            if (systemValue != null) {
                ErrorHandler.logPropertySource(key, "system environment variable", systemValue);
                return systemValue;
            }

            // Then check dotenv file
            String value = dotenv.get(key);
            if (value == null || value.isEmpty()) {
                String message = String.format("Environment variable '%s' not found or empty in configuration '%s'",
                        key, configurationDisplayName);
                logger.error(message);
                throw new IllegalArgumentException(message);
            }

            return value;
        } catch (Exception error) {
            ErrorHandler.logError(error, "getProperty", "Failed to retrieve environment variable");
            throw error;
        }
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        try {
            // First check system environment variables
            String systemValue = System.getenv(key);
            if (systemValue != null) {
                ErrorHandler.logPropertySource(key, "system environment variable", systemValue);
                return systemValue;
            }

            // Then check dotenv file
            String value = dotenv.get(key, defaultValue);
            if (value.equals(defaultValue)) {
                logger.warn("Environment variable '{}' not found, using default '{}' in configuration '{}'",
                        key, defaultValue, configurationDisplayName);
            } else {
                logger.info("Retrieved environment variable '{}' from configuration '{}'", key, configurationDisplayName);
            }

            return value;
        } catch (Exception error) {
            ErrorHandler.logError(error, "getProperty", "Failed to retrieve environment variable with default");
            throw error;
        }
    }

    @Override
    public <ConversionType> Optional<ConversionType> getProperty(String key, Class<ConversionType> type) {
        try {
            // Check system environment variables first
            String systemValue = System.getenv(key);
            String value = systemValue != null ? systemValue : dotenv.get(key);

            if (value == null || value.isEmpty()) {
                logger.warn("Environment variable '{}' not found in configuration '{}'", key, configurationDisplayName);
                return Optional.empty();
            }

            // Type conversion
            ConversionType result = getConversionType(type, value);
            logger.debug("Retrieved and converted environment variable '{}' to type: {}", key, type.getSimpleName());
            return Optional.of(result);
        } catch (Exception error) {
            ErrorHandler.logError(error, "getProperty", "Failed to retrieve or convert environment variable");
            return Optional.empty();
        }
    }

    @Override
    public void reload() {
        try {
            loadEnvironment();
            logger.info("Environment configuration '{}' reloaded successfully", configurationDisplayName);
        } catch (Exception error) {
            ErrorHandler.logError(error, "reload", "Failed to reload environment configuration");
            throw new RuntimeException(error);
        }
    }

    // Utility methods
    public SecretKey getSecretKey(String environmentSecretKey) {
        try {
            return Base64Utils.decodeSecretKey(getProperty(environmentSecretKey));
        } catch (Exception error) {
            ErrorHandler.logError(error, "getSecretKey", "Failed to retrieve secret key");
            throw error;
        }
    }

    /**
     * Gets the configuration for the specified display name and environment name.
     * This method handles cache management and ensures a non-null result.
     *
     * @param configurationDisplayName The display name of the configuration
     * @param envName The environment name
     * @return The environment configuration manager (never null)
     * @throws IllegalStateException if configuration cannot be loaded
     */
    public static EnvironmentFileConfigManager getConfiguration(String configurationDisplayName, String envName) {
        validateParameters(configurationDisplayName, envName);

        String cacheKey = configurationDisplayName + ":" + envName;

        // Check cache
        EnvironmentFileConfigManager cachedConfig = configManagerCache.get(cacheKey);
        if (cachedConfig != null) {
            logger.info("Using cached configuration for: {}", cacheKey);
            return cachedConfig;
        }

        // Load configuration
        logger.info("Loading configuration for: {}", cacheKey);
        try {
            EnvironmentFileConfigManager newConfig = EnvironmentFileConfigManager.loadConfiguration(
                    configurationDisplayName, envName);

            configManagerCache.put(cacheKey, newConfig);
            return newConfig;
        } catch (Exception e) {
            logger.error("Error loading configuration for {}: {}", cacheKey, e.getMessage(), e);
            throw new IllegalStateException("Failed to load configuration for: " + cacheKey, e);
        }
    }

    /**
     * Clears all entries from the configuration cache
     */
    public static void clearConfigCache() {
        configManagerCache.clear();
        logger.info("Configuration cache cleared");
    }
}