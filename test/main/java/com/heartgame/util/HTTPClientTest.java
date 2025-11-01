package com.heartgame.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for HTTPClient
 * Tests utility class structure and error handling
 *
 * Note: Full integration tests with actual HTTP requests would require:
 * - Mock HTTP server (e.g., WireMock, MockWebServer)
 * - Network connectivity
 * - External dependencies
 *
 * These tests focus on the utility class structure and basic validations.
 * Integration tests should be added separately for actual HTTP functionality.
 */
@DisplayName("HTTPClient Tests")
class HTTPClientTest {

    // ========== Utility Class Structure Tests ==========

    @Test
    @DisplayName("Should not be instantiable (utility class)")
    void testCannotInstantiate() {
        assertThrows(InvocationTargetException.class, () -> {
            Constructor<HTTPClient> constructor = HTTPClient.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }, "HTTPClient should throw exception when instantiated");
    }

    @Test
    @DisplayName("Should have private constructor")
    void testPrivateConstructor() throws NoSuchMethodException {
        Constructor<HTTPClient> constructor = HTTPClient.class.getDeclaredConstructor();

        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()),
                "Constructor should be private");
    }

    @Test
    @DisplayName("Constructor should throw UnsupportedOperationException")
    void testConstructorThrowsException() throws Exception {
        Constructor<HTTPClient> constructor = HTTPClient.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                constructor::newInstance);

        assertInstanceOf(UnsupportedOperationException.class, exception.getCause(),
                "Constructor should throw UnsupportedOperationException");
        assertTrue(exception.getCause().getMessage().contains("Utility class"),
                "Exception message should mention utility class");
    }

    // ========== Method Existence Tests ==========

    @Test
    @DisplayName("Should have static get(String) method")
    void testHasGetMethod() throws NoSuchMethodException {
        var method = HTTPClient.class.getDeclaredMethod("get", String.class);

        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                "get() method should be static");
        assertEquals(String.class, method.getReturnType(),
                "get() should return String");
    }

    @Test
    @DisplayName("Should have static get with timeouts method")
    void testHasGetWithTimeoutsMethod() throws NoSuchMethodException {
        var method = HTTPClient.class.getDeclaredMethod("get", String.class, int.class, int.class);

        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                "get() with timeouts should be static");
        assertEquals(String.class, method.getReturnType(),
                "get() should return String");
    }

    @Test
    @DisplayName("Should have static getStream method")
    void testHasGetStreamMethod() throws NoSuchMethodException {
        var method = HTTPClient.class.getDeclaredMethod("getStream", String.class);

        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                "getStream() method should be static");
        assertEquals(java.io.InputStream.class, method.getReturnType(),
                "getStream() should return InputStream");
    }

    @Test
    @DisplayName("Should have static getStream with timeouts method")
    void testHasGetStreamWithTimeoutsMethod() throws NoSuchMethodException {
        var method = HTTPClient.class.getDeclaredMethod("getStream", String.class, int.class, int.class);

        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                "getStream() with timeouts should be static");
        assertEquals(java.io.InputStream.class, method.getReturnType(),
                "getStream() should return InputStream");
    }

    @Test
    @DisplayName("Should have static post method")
    void testHasPostMethod() throws NoSuchMethodException {
        var method = HTTPClient.class.getDeclaredMethod("post", String.class, java.util.Map.class);

        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                "post() method should be static");
        assertEquals(String.class, method.getReturnType(),
                "post() should return String");
    }

    @Test
    @DisplayName("Should have static post with timeouts method")
    void testHasPostWithTimeoutsMethod() throws NoSuchMethodException {
        var method = HTTPClient.class.getDeclaredMethod("post",
                String.class, java.util.Map.class, int.class, int.class);

        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                "post() with timeouts should be static");
        assertEquals(String.class, method.getReturnType(),
                "post() should return String");
    }

    // ========== Error Handling Tests (Invalid URLs) ==========

    @Test
    @DisplayName("Should throw IOException for invalid URL in get()")
    void testGetWithInvalidUrl() {
        assertThrows(IOException.class, () -> {
            HTTPClient.get("not-a-valid-url");
        }, "Should throw IOException for invalid URL");
    }

    @Test
    @DisplayName("Should throw IOException for invalid URL in getStream()")
    void testGetStreamWithInvalidUrl() {
        assertThrows(IOException.class, () -> {
            HTTPClient.getStream("not-a-valid-url");
        }, "Should throw IOException for invalid URL");
    }

    @Test
    @DisplayName("Should throw IOException for invalid URL in post()")
    void testPostWithInvalidUrl() {
        assertThrows(IOException.class, () -> {
            HTTPClient.post("not-a-valid-url", java.util.Map.of("key", "value"));
        }, "Should throw IOException for invalid URL");
    }

    @Test
    @DisplayName("Should throw exception for null URL in get()")
    void testGetWithNullUrl() {
        assertThrows(Exception.class, () -> {
            HTTPClient.get(null);
        }, "Should throw exception for null URL");
    }

    @Test
    @DisplayName("Should throw exception for empty URL in get()")
    void testGetWithEmptyUrl() {
        assertThrows(IOException.class, () -> {
            HTTPClient.get("");
        }, "Should throw IOException for empty URL");
    }

    // ========== Timeout Parameter Tests ==========

    @Test
    @DisplayName("Should accept custom timeouts in get()")
    void testGetWithCustomTimeouts() {
        assertThrows(IOException.class, () -> {
            // Invalid URL, but should accept the parameters
            HTTPClient.get("not-a-valid-url", 5000, 5000);
        });
    }

    @Test
    @DisplayName("Should accept zero timeout values")
    void testGetWithZeroTimeouts() {
        assertThrows(IOException.class, () -> {
            HTTPClient.get("not-a-valid-url", 0, 0);
        });
    }

    @Test
    @DisplayName("Should accept negative timeout values")
    void testGetWithNegativeTimeouts() {
        assertThrows(IOException.class, () -> {
            // Negative timeouts might be invalid, but we test parameter acceptance
            HTTPClient.get("not-a-valid-url", -1, -1);
        });
    }

    // ========== POST Form Data Tests ==========

    @Test
    @DisplayName("Should handle empty form data map")
    void testPostWithEmptyFormData() {
        assertThrows(IOException.class, () -> {
            HTTPClient.post("not-a-valid-url", java.util.Map.of());
        });
    }

    @Test
    @DisplayName("Should handle null form data map")
    void testPostWithNullFormData() {
        assertThrows(Exception.class, () -> {
            HTTPClient.post("not-a-valid-url", null);
        });
    }

    @Test
    @DisplayName("Should handle form data with special characters")
    void testPostWithSpecialCharacters() {
        assertThrows(IOException.class, () -> {
            java.util.Map<String, String> formData = java.util.Map.of(
                    "key", "value with spaces & special=chars"
            );
            HTTPClient.post("not-a-valid-url", formData);
        });
    }

    @Test
    @DisplayName("Should handle form data with multiple entries")
    void testPostWithMultipleEntries() {
        assertThrows(IOException.class, () -> {
            java.util.Map<String, String> formData = java.util.Map.of(
                    "param1", "value1",
                    "param2", "value2",
                    "param3", "value3"
            );
            HTTPClient.post("not-a-valid-url", formData);
        });
    }

    // ========== Class Structure Tests ==========

    @Test
    @DisplayName("Should be final class")
    void testClassIsFinal() {
        assertTrue(java.lang.reflect.Modifier.isFinal(HTTPClient.class.getModifiers()),
                "HTTPClient class should be final");
    }

    @Test
    @DisplayName("Should have public static methods only")
    void testOnlyPublicStaticMethods() {
        var methods = HTTPClient.class.getDeclaredMethods();

        for (var method : methods) {
            if (!method.getName().equals("buildFormData")) { // Private helper method
                assertTrue(java.lang.reflect.Modifier.isPublic(method.getModifiers()),
                        "Method " + method.getName() + " should be public");
                assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                        "Method " + method.getName() + " should be static");
            }
        }
    }

    // ========== Method Signature Tests ==========

    @Test
    @DisplayName("get() methods should declare IOException")
    void testGetMethodsThrowIOException() throws NoSuchMethodException {
        var getMethod = HTTPClient.class.getDeclaredMethod("get", String.class);
        var getWithTimeoutsMethod = HTTPClient.class.getDeclaredMethod("get",
                String.class, int.class, int.class);

        assertTrue(containsException(getMethod.getExceptionTypes(), IOException.class),
                "get() should declare IOException");
        assertTrue(containsException(getWithTimeoutsMethod.getExceptionTypes(), IOException.class),
                "get() with timeouts should declare IOException");
    }

    @Test
    @DisplayName("getStream() methods should declare IOException")
    void testGetStreamMethodsThrowIOException() throws NoSuchMethodException {
        var getStreamMethod = HTTPClient.class.getDeclaredMethod("getStream", String.class);
        var getStreamWithTimeoutsMethod = HTTPClient.class.getDeclaredMethod("getStream",
                String.class, int.class, int.class);

        assertTrue(containsException(getStreamMethod.getExceptionTypes(), IOException.class),
                "getStream() should declare IOException");
        assertTrue(containsException(getStreamWithTimeoutsMethod.getExceptionTypes(), IOException.class),
                "getStream() with timeouts should declare IOException");
    }

    @Test
    @DisplayName("post() methods should declare IOException")
    void testPostMethodsThrowIOException() throws NoSuchMethodException {
        var postMethod = HTTPClient.class.getDeclaredMethod("post",
                String.class, java.util.Map.class);
        var postWithTimeoutsMethod = HTTPClient.class.getDeclaredMethod("post",
                String.class, java.util.Map.class, int.class, int.class);

        assertTrue(containsException(postMethod.getExceptionTypes(), IOException.class),
                "post() should declare IOException");
        assertTrue(containsException(postWithTimeoutsMethod.getExceptionTypes(), IOException.class),
                "post() with timeouts should declare IOException");
    }

    // ========== Helper Methods ==========

    private boolean containsException(Class<?>[] exceptionTypes, Class<? extends Exception> exceptionClass) {
        for (Class<?> type : exceptionTypes) {
            if (type.equals(exceptionClass)) {
                return true;
            }
        }
        return false;
    }

    // ========== Documentation Tests ==========

    @Test
    @DisplayName("Should have buildFormData as private helper method")
    void testBuildFormDataIsPrivate() throws NoSuchMethodException {
        var method = HTTPClient.class.getDeclaredMethod("buildFormData", java.util.Map.class);

        assertTrue(java.lang.reflect.Modifier.isPrivate(method.getModifiers()),
                "buildFormData() should be private helper method");
        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                "buildFormData() should be static");
    }
}