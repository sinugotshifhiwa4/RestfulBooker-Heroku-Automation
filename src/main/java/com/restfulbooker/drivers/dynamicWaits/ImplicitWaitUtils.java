package com.restfulbooker.drivers.dynamicWaits;

import com.restfulbooker.config.properties.PropertyConfigConstants;
import com.restfulbooker.config.properties.PropertyFileConfigManager;
import com.restfulbooker.drivers.DriverFactory;
import com.restfulbooker.utils.ErrorHandler;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

public class ImplicitWaitUtils {

    private static final String TIMEOUT_KEY = "IMPLICIT_TIMEOUT";

    private ImplicitWaitUtils() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Retrieves implicit wait timeout from properties and applies it to WebDriver
     */
    public static void applyImplicitWait(WebDriver driver) {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(getTimeout()));
        } catch (Exception error) {
            ErrorHandler.logError(error, "applyImplicitWait", "Failed to apply implicit wait time");
            throw error;
        }
    }

    public static void applyImplicitWait() {
        applyImplicitWait(DriverFactory.getInstance().getDriver());
    }

    /**
     * Fetch timeout value from properties file
     *
     * @return Optional containing the timeout value if found
     */
    private static int getTimeout() {
        try {
            return PropertyFileConfigManager.getConfiguration(
                            PropertyConfigConstants.Environment.GLOBAL.getDisplayName(),
                            PropertyConfigConstants.PropertiesFilePath.GLOBAL.getFullPath())
                    .getProperty(TIMEOUT_KEY, Integer.class)
                    .orElse(10);
        } catch (Exception error) {
            ErrorHandler.logError(error, "getTimeout", "Failed to retrieve timeout value");
            throw error;
        }
    }
}