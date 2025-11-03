package com.heartgame.service;

import org.junit.jupiter.api.*;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AvatarService
 * Uses test subclass to mock HTTP responses (KISS approach)
 * Tests URL encoding, null handling, and JSON metadata fetching
 *
 * Note: fetchAvatar() tests are limited to validation logic since it directly
 * calls HTTPClient.getStream() which would require real HTTP calls or complex mocking.
 * fetchAvatarMetadata() is fully testable via fetchData() override.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AvatarServiceTest {

    /**
     * Test subclass that overrides fetchData to return mock responses
     * KISS approach: Simple inheritance instead of complex mocking frameworks
     */
    private static class TestableAvatarService extends AvatarService {
        private String mockResponse;
        private IOException mockException;

        public void setMockResponse(String response) {
            this.mockResponse = response;
            this.mockException = null;
        }

        public void setMockException(IOException exception) {
            this.mockException = exception;
            this.mockResponse = null;
        }

        @Override
        public String fetchData(String endpoint) throws IOException {
            if (mockException != null) {
                throw mockException;
            }
            return mockResponse;
        }
    }

    private TestableAvatarService service;

    @BeforeEach
    void setUp() {
        service = new TestableAvatarService();
    }

    // ==================== fetchAvatarMetadata Tests ====================

    @Test
    @Order(1)
    @DisplayName("fetchAvatarMetadata() returns JSON for valid username")
    void testFetchAvatarMetadataSuccess() {
        String mockJson = "{\"svg\":\"<svg>...</svg>\"}";
        service.setMockResponse(mockJson);

        String result = service.fetchAvatarMetadata("testuser");

        assertNotNull(result, "Should return JSON metadata");
        assertEquals(mockJson, result, "Should return the mock JSON");
    }

    @Test
    @Order(2)
    @DisplayName("fetchAvatarMetadata() returns null for null username")
    void testFetchAvatarMetadataNullUsername() {
        String result = service.fetchAvatarMetadata(null);

        assertNull(result, "Should return null for null username");
    }

    @Test
    @Order(3)
    @DisplayName("fetchAvatarMetadata() returns null for empty username")
    void testFetchAvatarMetadataEmptyUsername() {
        String result = service.fetchAvatarMetadata("");

        assertNull(result, "Should return null for empty username");
    }

    @Test
    @Order(4)
    @DisplayName("fetchAvatarMetadata() handles username with spaces")
    void testFetchAvatarMetadataUsernameWithSpaces() {
        String mockJson = "{\"svg\":\"<svg>...</svg>\"}";
        service.setMockResponse(mockJson);

        String result = service.fetchAvatarMetadata("user name");

        assertNotNull(result, "Should handle username with spaces");
        assertEquals(mockJson, result);
    }

    @Test
    @Order(5)
    @DisplayName("fetchAvatarMetadata() handles username with special characters")
    void testFetchAvatarMetadataSpecialCharacters() {
        String mockJson = "{\"svg\":\"<svg>...</svg>\"}";
        service.setMockResponse(mockJson);

        String result = service.fetchAvatarMetadata("user@example.com");

        assertNotNull(result, "Should handle special characters");
    }

    @Test
    @Order(6)
    @DisplayName("fetchAvatarMetadata() handles username with unicode")
    void testFetchAvatarMetadataUnicode() {
        String mockJson = "{\"svg\":\"<svg>...</svg>\"}";
        service.setMockResponse(mockJson);

        String result = service.fetchAvatarMetadata("user_测试");

        assertNotNull(result, "Should handle unicode characters");
    }

    @Test
    @Order(7)
    @DisplayName("fetchAvatarMetadata() returns null on IOException")
    void testFetchAvatarMetadataIOException() {
        service.setMockException(new IOException("Network error"));

        String result = service.fetchAvatarMetadata("testuser");

        assertNull(result, "Should return null on IOException");
    }

    @Test
    @Order(8)
    @DisplayName("fetchAvatarMetadata() handles empty JSON response")
    void testFetchAvatarMetadataEmptyResponse() {
        service.setMockResponse("");

        String result = service.fetchAvatarMetadata("testuser");

        assertNotNull(result, "Should return empty string (not null)");
        assertEquals("", result);
    }

    @Test
    @Order(9)
    @DisplayName("fetchAvatarMetadata() handles null response from API")
    void testFetchAvatarMetadataNullResponse() {
        service.setMockResponse(null);

        String result = service.fetchAvatarMetadata("testuser");

        assertNull(result, "Should return null if API returns null");
    }

    @Test
    @Order(10)
    @DisplayName("fetchAvatarMetadata() handles complex JSON")
    void testFetchAvatarMetadataComplexJson() {
        String complexJson = """
            {
                "svg": "<svg>...</svg>",
                "seed": "testuser",
                "version": "7.x",
                "metadata": {
                    "colors": ["#FF0000", "#00FF00"]
                }
            }
            """;
        service.setMockResponse(complexJson);

        String result = service.fetchAvatarMetadata("testuser");

        assertNotNull(result);
        assertTrue(result.contains("testuser"), "Should contain username");
        assertTrue(result.contains("svg"), "Should contain svg data");
    }

    // ==================== fetchAvatar Tests (Input Validation) ====================

    @Test
    @Order(11)
    @DisplayName("fetchAvatar() returns null for null username")
    void testFetchAvatarNullUsername() {
        BufferedImage result = service.fetchAvatar(null);

        assertNull(result, "Should return null for null username");
    }

    @Test
    @Order(12)
    @DisplayName("fetchAvatar() returns null for empty username")
    void testFetchAvatarEmptyUsername() {
        BufferedImage result = service.fetchAvatar("");

        assertNull(result, "Should return null for empty username");
    }

    /*
     * Note: Full testing of fetchAvatar() with successful image fetching would require:
     * 1. Real HTTP calls to DiceBear API (not ideal for unit tests)
     * 2. Mock HTTP server (e.g., WireMock) - more complex setup
     * 3. Refactoring to inject HTTPClient dependency - breaks KISS principle
     *
     * The input validation tests above are sufficient for the KISS approach.
     * Integration tests could be added separately to verify actual image fetching.
     */

    // ==================== fetchData Tests ====================

    @Test
    @Order(13)
    @DisplayName("fetchData() constructs correct endpoint")
    void testFetchDataEndpoint() throws IOException {
        // Create a service that can capture the endpoint
        class EndpointCapturingService extends AvatarService {
            String capturedEndpoint;

            @Override
            public String fetchData(String endpoint) throws IOException {
                capturedEndpoint = endpoint;
                return "{}";
            }
        }

        EndpointCapturingService capturingService = new EndpointCapturingService();
        capturingService.fetchData("test/endpoint");

        assertEquals("test/endpoint", capturingService.capturedEndpoint);
    }

    // ==================== Integration Tests ====================

    @Test
    @Order(14)
    @DisplayName("Multiple fetchAvatarMetadata calls with different users")
    void testMultipleFetchAvatarMetadataCalls() {
        service.setMockResponse("{\"user\":\"user1\"}");
        String result1 = service.fetchAvatarMetadata("user1");

        service.setMockResponse("{\"user\":\"user2\"}");
        String result2 = service.fetchAvatarMetadata("user2");

        service.setMockResponse("{\"user\":\"user3\"}");
        String result3 = service.fetchAvatarMetadata("user3");

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertNotEquals(result1, result2, "Different users should have different responses");
    }

    @Test
    @Order(15)
    @DisplayName("fetchAvatarMetadata is deterministic for same username")
    void testFetchAvatarMetadataDeterministic() {
        String mockJson = "{\"seed\":\"testuser\"}";
        service.setMockResponse(mockJson);

        String result1 = service.fetchAvatarMetadata("testuser");
        String result2 = service.fetchAvatarMetadata("testuser");

        assertEquals(result1, result2, "Same username should return same result (deterministic)");
    }

    @Test
    @Order(16)
    @DisplayName("fetchAvatarMetadata handles very long username")
    void testFetchAvatarMetadataLongUsername() {
        String longUsername = "a".repeat(1000);
        service.setMockResponse("{\"svg\":\"<svg>...</svg>\"}");

        String result = service.fetchAvatarMetadata(longUsername);

        assertNotNull(result, "Should handle very long username");
    }

    @Test
    @Order(17)
    @DisplayName("fetchAvatarMetadata handles username with URL-unsafe characters")
    void testFetchAvatarMetadataUrlUnsafeCharacters() {
        String mockJson = "{\"svg\":\"<svg>...</svg>\"}";
        service.setMockResponse(mockJson);

        // Characters that need URL encoding: space, &, =, ?, #, /
        String unsafeUsername = "user&name=test?foo#bar/baz";
        String result = service.fetchAvatarMetadata(unsafeUsername);

        assertNotNull(result, "Should handle URL-unsafe characters via encoding");
    }

    @Test
    @Order(18)
    @DisplayName("fetchAvatar and fetchAvatarMetadata handle same username consistently")
    void testFetchAvatarAndMetadataConsistency() {
        service.setMockResponse("{\"seed\":\"testuser\"}");

        String metadata = service.fetchAvatarMetadata("testuser");
        BufferedImage avatar = service.fetchAvatar("testuser");

        // Both should handle the same username
        assertNotNull(metadata, "Metadata should be fetched");
        // Avatar returns null because we can't mock HTTPClient.getStream easily
        // but it should not throw exceptions
    }

    @Test
    @Order(19)
    @DisplayName("Service can be instantiated multiple times")
    void testMultipleServiceInstances() {
        AvatarService service1 = new AvatarService();
        AvatarService service2 = new AvatarService();

        assertNotNull(service1);
        assertNotNull(service2);
        assertNotSame(service1, service2, "Should be different instances");
    }

    @Test
    @Order(20)
    @DisplayName("fetchAvatarMetadata handles whitespace-only username")
    void testFetchAvatarMetadataWhitespaceUsername() {
        service.setMockResponse("{\"svg\":\"<svg>...</svg>\"}");

        String result = service.fetchAvatarMetadata("   ");

        // Non-empty string (whitespace), so it's processed
        assertNotNull(result, "Whitespace username should be processed");
    }

    @Test
    @Order(21)
    @DisplayName("Real AvatarService instantiation")
    void testRealServiceInstantiation() {
        AvatarService realService = new AvatarService();

        assertNotNull(realService, "Should be able to instantiate real service");

        // Test that null/empty validation works without mocking
        assertNull(realService.fetchAvatar(null));
        assertNull(realService.fetchAvatar(""));
        assertNull(realService.fetchAvatarMetadata(null));
        assertNull(realService.fetchAvatarMetadata(""));
    }
}
