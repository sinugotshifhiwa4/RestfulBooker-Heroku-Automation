package com.restfulbooker.testDataStorage;

import com.restfulbooker.utils.LoggerUtils;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TestContextStore {

    private static final Logger logger = LoggerUtils.getLogger(TestContextStore.class);
    private static final Map<String, ThreadLocal<Map<String, String>>> testContextData = new ConcurrentHashMap<>();

    private TestContextStore() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    public static void initializeContext(String... testIds) {
        for (String testId : testIds) {
            logger.info("Initializing test context for testId: {}", testId);
            testContextData.computeIfAbsent(testId, k -> ThreadLocal.withInitial(ConcurrentHashMap::new));
        }
    }

    public static void storeContextValue(String testId, String key, String value) {
        ThreadLocal<Map<String, String>> threadLocalMap = testContextData.get(testId);
        if (threadLocalMap == null) {
            throw new IllegalStateException("Test context not initialized: " + testId);
        }
        threadLocalMap.get().put(key, value);
        logger.info("Stored context value for key: '{}' in testId: '{}',", key, testId);
    }

    public static void storeContextValue(String testId, String key, int value) {
        ThreadLocal<Map<String, String>> threadLocalMap = testContextData.get(testId);
        if (threadLocalMap == null) {
            throw new IllegalStateException("Test context not initialized: " + testId);
        }
        // Convert int to String for storage
        threadLocalMap.get().put(key, String.valueOf(value));
        logger.info("Stored context int value for key: '{}' in testId: '{}',", key, testId);
    }

    public static String getContextValue(String testId, String key) {
        return Optional.ofNullable(testContextData.get(testId))
                .map(ThreadLocal::get)
                .map(ctx -> ctx.get(key))
                .map(value -> {
                    logger.info("Successfully retrieved context value for key: '{}' in testId: '{}'", key, testId);
                    return value;
                })
                .orElseThrow(() -> new IllegalArgumentException("Test context not initialized or key not found: " + testId));
    }

    public static int getContextValueAsInt(String testId, String key) {
        String value = getContextValue(testId, key);
        return Integer.parseInt(value);
    }

    public static void removeContextValue(String testId, String key) {
        Optional.ofNullable(testContextData.get(testId))
                .map(ThreadLocal::get)
                .ifPresent(ctx -> ctx.remove(key));
    }

    public static void removeTestContext(String testId) {
        testContextData.remove(testId);
    }

    public static boolean containsTestContext(String testId) {
        return testContextData.containsKey(testId);
    }

    private static void cleanupThreadLocal(String testId) {
        ThreadLocal<Map<String, String>> threadLocal = testContextData.remove(testId);
        if (threadLocal != null) {
            threadLocal.remove(); // Ensures proper cleanup for GC
        }
        logger.info("Cleared test context for testId: {}", testId);
    }

    public static void cleanupTestContext(String... testIds) {
        for (String testId : testIds) {
            TestContextStore.cleanupThreadLocal(testId);
        }
    }
}
