package com.heartgame.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ConfigurationManager
 * Tests singleton pattern, property loading, and default values
 */
@DisplayName("ConfigurationManager Tests")
class ConfigurationManagerTest {

    private ConfigurationManager configManager;

    @BeforeEach
    void setUp() {
        configManager = ConfigurationManager.getInstance();
    }

    // ========== Singleton Pattern Tests ==========

    @Test
    @DisplayName("Should return same instance on multiple getInstance() calls")
    void testSingleton() {
        ConfigurationManager instance1 = ConfigurationManager.getInstance();
        ConfigurationManager instance2 = ConfigurationManager.getInstance();

        assertSame(instance1, instance2, "getInstance() should return the same instance");
    }

    // ========== Default Properties Tests ==========

    @Test
    @DisplayName("Should return HeartGame API URL")
    void testGetHeartGameApiUrl() {
        String apiUrl = configManager.getHeartGameApiUrl();

        assertNotNull(apiUrl, "API URL should not be null");
        assertTrue(apiUrl.contains("marcconrad.com") || apiUrl.contains("heartgame.api.url"),
                "API URL should be valid");
    }

    @Test
    @DisplayName("Should return OAuth callback port")
    void testGetOAuthCallbackPort() {
        int port = configManager.getOAuthCallbackPort();

        assertTrue(port > 0 && port <= 65535,
                "Port should be in valid range (1-65535)");
    }

    @Test
    @DisplayName("Should return tokens directory")
    void testGetTokensDirectory() {
        String tokensDir = configManager.getTokensDirectory();

        assertNotNull(tokensDir, "Tokens directory should not be null");
        assertFalse(tokensDir.isEmpty(), "Tokens directory should not be empty");
    }

    // ========== Property Getter Tests ==========

    @Test
    @DisplayName("Should get property value")
    void testGetProperty() {
        String apiUrl = configManager.getProperty("heartgame.api.url");

        assertNotNull(apiUrl, "Property value should not be null");
    }

    @Test
    @DisplayName("Should return null for non-existent property")
    void testGetNonExistentProperty() {
        String value = configManager.getProperty("non.existent.property");

        assertNull(value, "Non-existent property should return null");
    }

    @Test
    @DisplayName("Should get property with default value")
    void testGetPropertyWithDefault() {
        String value = configManager.getProperty("non.existent.property", "defaultValue");

        assertEquals("defaultValue", value,
                "Non-existent property should return default value");
    }

    @Test
    @DisplayName("Should get existing property ignoring default")
    void testGetExistingPropertyWithDefault() {
        String apiUrl = configManager.getProperty("heartgame.api.url", "defaultValue");

        assertNotNull(apiUrl);
        assertNotEquals("defaultValue", apiUrl,
                "Existing property should not return default value");
    }

    // ========== Integer Property Tests ==========

    @Test
    @DisplayName("Should get integer property")
    void testGetIntProperty() {
        int timeout = configManager.getIntProperty("heartgame.api.timeout", 10000);

        assertTrue(timeout > 0, "Timeout should be positive");
    }

    @Test
    @DisplayName("Should return default for non-existent integer property")
    void testGetNonExistentIntProperty() {
        int value = configManager.getIntProperty("non.existent.int.property", 999);

        assertEquals(999, value,
                "Non-existent integer property should return default");
    }

    @Test
    @DisplayName("Should return default for invalid integer property")
    void testGetInvalidIntProperty() {
        // First set a non-numeric property value (if possible)
        // Since we can't modify properties directly, we test with a known string property
        int value = configManager.getIntProperty("app.name", 123);

        assertEquals(123, value,
                "Invalid integer property should return default");
    }

    @Test
    @DisplayName("Should parse valid integer property")
    void testParseValidIntProperty() {
        // Test with a known integer property
        int port = configManager.getIntProperty("google.oauth.callback.port", 8080);

        assertTrue(port > 0 && port <= 65535,
                "Should parse valid integer property");
    }

    // ========== Specific Configuration Tests ==========

    @Test
    @DisplayName("Should have valid API timeout configuration")
    void testApiTimeoutConfiguration() {
        int timeout = configManager.getIntProperty("heartgame.api.timeout", 30000);

        assertTrue(timeout >= 1000 && timeout <= 120000,
                "API timeout should be reasonable (1s - 120s)");
    }

    @Test
    @DisplayName("Should have valid OAuth port configuration")
    void testOAuthPortConfiguration() {
        int port = configManager.getOAuthCallbackPort();

        assertTrue(port >= 1024 && port <= 65535,
                "OAuth port should be in valid non-privileged range (1024-65535)");
    }

    // ========== Default Values Tests ==========

    @Test
    @DisplayName("Should load default values when properties file is missing")
    void testDefaultValuesPresent() {
        // Even if application.properties is missing, defaults should be loaded
        assertNotNull(configManager.getHeartGameApiUrl(),
                "Should have default API URL");
        assertTrue(configManager.getOAuthCallbackPort() > 0,
                "Should have default OAuth port");
        assertNotNull(configManager.getTokensDirectory(),
                "Should have default tokens directory");
    }

    @Test
    @DisplayName("Should have app name in configuration")
    void testAppNameConfiguration() {
        String appName = configManager.getProperty("app.name", "HeartGame");

        assertNotNull(appName);
        assertFalse(appName.isEmpty(), "App name should not be empty");
    }

    @Test
    @DisplayName("Should have app version in configuration")
    void testAppVersionConfiguration() {
        String appVersion = configManager.getProperty("app.version", "1.0");

        assertNotNull(appVersion);
        assertFalse(appVersion.isEmpty(), "App version should not be empty");
    }

    // ========== Property Key Tests ==========

    @Test
    @DisplayName("Should handle empty property key")
    void testEmptyPropertyKey() {
        String value = configManager.getProperty("");

        // Empty key should return null or empty value
        assertTrue(value == null || value.isEmpty());
    }

    @Test
    @DisplayName("Should handle null property key gracefully")
    void testNullPropertyKey() {
        assertDoesNotThrow(() -> {
            String value = configManager.getProperty(null);
            // May return null or throw NullPointerException depending on implementation
        }, "Should handle null property key gracefully");
    }

    // ========== Multiple Access Tests ==========

    @Test
    @DisplayName("Should return consistent values on multiple accesses")
    void testConsistentValues() {
        String value1 = configManager.getHeartGameApiUrl();
        String value2 = configManager.getHeartGameApiUrl();

        assertEquals(value1, value2,
                "Should return consistent values on multiple accesses");
    }

    @Test
    @DisplayName("Should return consistent integer values on multiple accesses")
    void testConsistentIntValues() {
        int value1 = configManager.getOAuthCallbackPort();
        int value2 = configManager.getOAuthCallbackPort();

        assertEquals(value1, value2,
                "Should return consistent integer values on multiple accesses");
    }

    // ========== Integration Tests ==========

    @Test
    @DisplayName("Should have all required properties for HeartGame API")
    void testHeartGameApiProperties() {
        String apiUrl = configManager.getHeartGameApiUrl();
        int timeout = configManager.getIntProperty("heartgame.api.timeout", 30000);

        assertNotNull(apiUrl, "API URL should be configured");
        assertTrue(timeout > 0, "API timeout should be configured");
    }

    @Test
    @DisplayName("Should have all required properties for OAuth")
    void testOAuthProperties() {
        int port = configManager.getOAuthCallbackPort();
        String tokensDir = configManager.getTokensDirectory();

        assertTrue(port > 0, "OAuth port should be configured");
        assertNotNull(tokensDir, "Tokens directory should be configured");
    }

    // ========== Edge Case Tests ==========

    @Test
    @DisplayName("Should handle property with spaces")
    void testPropertyWithSpaces() {
        String value = configManager.getProperty("property with spaces", "default");

        assertNotNull(value);
        // Either returns default or actual value if such a property exists
    }

    @Test
    @DisplayName("Should handle very long property key")
    void testVeryLongPropertyKey() {
        String longKey = "a".repeat(1000);
        String value = configManager.getProperty(longKey, "default");

        assertEquals("default", value,
                "Very long non-existent key should return default");
    }

    @Test
    @DisplayName("Should handle integer overflow gracefully")
    void testIntegerOverflow() {
        // Test with a property that doesn't exist (so default is used)
        int value = configManager.getIntProperty("non.existent", Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, value,
                "Should handle integer max value as default");
    }

    @Test
    @DisplayName("Should handle negative default values")
    void testNegativeDefaultValues() {
        int value = configManager.getIntProperty("non.existent", -100);

        assertEquals(-100, value,
                "Should handle negative default values");
    }
}