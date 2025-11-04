package com.heartgame.service;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GoogleAuthService
 * Tests helper methods and username generation logic (KISS approach)
 * Note: Full OAuth flow testing (authenticateUser, waitForCallback, etc.)
 * would require complex mocking of HTTP servers, browser interaction, and OAuth endpoints.
 * These integration tests should be performed separately or manually.
 * This test suite focuses on the testable utility methods via reflection.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GoogleAuthServiceTest {

    private GoogleAuthService service;

    @BeforeEach
    void setUp() {
        service = new GoogleAuthService();
    }

    // ==================== generateUsername Tests ====================

    @Test
    @Order(1)
    @DisplayName("generateUsername() uses email prefix when available")
    void testGenerateUsernameFromEmail() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "john.doe@example.com", "John Doe", "12345");

        assertEquals("johnDoe", result, "Should use email prefix without special chars");
    }

    @Test
    @Order(2)
    @DisplayName("generateUsername() removes special characters from email")
    void testGenerateUsernameEmailSpecialChars() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "user.name_123@example.com", null, "12345");

        assertEquals("username123", result, "Should remove dots and underscores");
    }

    @Test
    @Order(3)
    @DisplayName("generateUsername() converts to lowercase")
    void testGenerateUsernameToLowercase() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "JOHN.DOE@EXAMPLE.COM", null, "12345");

        assertEquals("johnDoe", result, "Should convert to lowercase");
    }

    @Test
    @Order(4)
    @DisplayName("generateUsername() uses name when email is null")
    void testGenerateUsernameFromName() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, null, "John Doe", "12345");

        assertEquals("johnDoe", result, "Should use name without spaces");
    }

    @Test
    @Order(5)
    @DisplayName("generateUsername() uses name when email is empty")
    void testGenerateUsernameFromNameWhenEmailEmpty() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "", "Alice Smith", "12345");

        assertEquals("aliceSmith", result, "Should use name when email is empty");
    }

    @Test
    @Order(6)
    @DisplayName("generateUsername() removes spaces from name")
    void testGenerateUsernameNameRemovesSpaces() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, null, "John Middle Doe", "12345");

        assertEquals("johnMiddleDoe", result, "Should remove all spaces");
    }

    @Test
    @Order(7)
    @DisplayName("generateUsername() removes special characters from name")
    void testGenerateUsernameNameSpecialChars() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, null, "Jean-Paul O'Connor", "12345");

        assertEquals("jeanPaulOConnor", result, "Should remove hyphens and apostrophes");
    }

    @Test
    @Order(8)
    @DisplayName("generateUsername() falls back to Google ID")
    void testGenerateUsernameFallbackToGoogleId() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, null, null, "123456789");

        assertEquals("google_user_12345678", result, "Should use Google ID prefix");
    }

    @Test
    @Order(9)
    @DisplayName("generateUsername() handles short Google ID")
    void testGenerateUsernameShortGoogleId() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, null, null, "123");

        assertEquals("google_user_123", result, "Should handle Google ID shorter than 8 chars");
    }

    @Test
    @Order(10)
    @DisplayName("generateUsername() handles empty name and null email")
    void testGenerateUsernameEmptyNameNullEmail() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, null, "", "987654321");

        assertEquals("google_user_98765432", result, "Should fallback to Google ID");
    }

    // ==================== extractParameter Tests ====================

    @Test
    @Order(11)
    @DisplayName("extractParameter() extracts parameter from URL")
    void testExtractParameter() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "extractParameter", String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "/?code=abc123&state=xyz", "code");

        assertEquals("abc123", result, "Should extract code parameter");
    }

    @Test
    @Order(12)
    @DisplayName("extractParameter() extracts state parameter")
    void testExtractParameterState() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "extractParameter", String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "/?code=abc&state=xyz789", "state");

        assertEquals("xyz789", result, "Should extract state parameter");
    }

    @Test
    @Order(13)
    @DisplayName("extractParameter() returns null when parameter not found")
    void testExtractParameterNotFound() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "extractParameter", String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "/?code=abc123", "missing");

        assertNull(result, "Should return null for missing parameter");
    }

    @Test
    @Order(14)
    @DisplayName("extractParameter() returns null when no query string")
    void testExtractParameterNoQuery() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "extractParameter", String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "/callback", "code");

        assertNull(result, "Should return null when no query string");
    }

    @Test
    @Order(15)
    @DisplayName("extractParameter() handles multiple parameters")
    void testExtractParameterMultiple() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "extractParameter", String.class, String.class);
        method.setAccessible(true);

        String url = "/?code=abc123&state=xyz&scope=email+profile";
        String result = (String) method.invoke(service, url, "scope");

        assertEquals("email profile", result, "Should extract scope and decode URL encoding");
    }

    @Test
    @Order(16)
    @DisplayName("extractParameter() handles URL encoded values")
    void testExtractParameterUrlEncoded() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "extractParameter", String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "/?code=abc%20def", "code");

        assertEquals("abc def", result, "Should decode URL-encoded space");
    }

    @Test
    @Order(17)
    @DisplayName("extractParameter() handles parameter with no value")
    void testExtractParameterNoValue() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "extractParameter", String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "/?code=", "code");

        assertNull(result, "Should return null for parameter with no value");
    }

    @Test
    @Order(18)
    @DisplayName("extractParameter() handles first parameter")
    void testExtractParameterFirst() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "extractParameter", String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "/?first=aaa&second=bbb", "first");

        assertEquals("aaa", result, "Should extract first parameter");
    }

    @Test
    @Order(19)
    @DisplayName("extractParameter() handles last parameter")
    void testExtractParameterLast() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "extractParameter", String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "/?first=aaa&second=bbb", "second");

        assertEquals("bbb", result, "Should extract last parameter");
    }

    // ==================== Service Instantiation Tests ====================

    @Test
    @Order(20)
    @DisplayName("Service can be instantiated")
    void testServiceInstantiation() {
        GoogleAuthService service = new GoogleAuthService();

        assertNotNull(service, "Service should be instantiated");
    }

    @Test
    @Order(21)
    @DisplayName("Multiple service instances can coexist")
    void testMultipleInstances() {
        GoogleAuthService service1 = new GoogleAuthService();
        GoogleAuthService service2 = new GoogleAuthService();

        assertNotNull(service1);
        assertNotNull(service2);
        assertNotSame(service1, service2, "Should be different instances");
    }

    // ==================== Edge Cases ====================

    @Test
    @Order(22)
    @DisplayName("generateUsername() handles email with numbers")
    void testGenerateUsernameEmailWithNumbers() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "user123@example.com", null, "12345");

        assertEquals("user123", result, "Should preserve numbers in username");
    }

    @Test
    @Order(23)
    @DisplayName("generateUsername() handles name with numbers")
    void testGenerateUsernameNameWithNumbers() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, null, "John Doe 42", "12345");

        assertEquals("johnDoe42", result, "Should preserve numbers in name");
    }

    @Test
    @Order(24)
    @DisplayName("generateUsername() handles unicode in email")
    void testGenerateUsernameUnicodeEmail() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "user测试@example.com", null, "12345");

        assertEquals("user", result, "Should remove non-ASCII characters");
    }

    @Test
    @Order(25)
    @DisplayName("generateUsername() handles only special characters in email")
    void testGenerateUsernameOnlySpecialCharsEmail() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "generateUsername", String.class, String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "...@example.com", "John", "12345");

        // Email prefix becomes empty after removing special chars, so fallback to name
        assertEquals("john", result, "Should fallback to name when email becomes empty");
    }

    @Test
    @Order(26)
    @DisplayName("extractParameter() handles fragment in URL")
    void testExtractParameterWithFragment() throws Exception {
        Method method = GoogleAuthService.class.getDeclaredMethod(
                "extractParameter", String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "/?code=abc123#fragment", "code");

        assertEquals("abc123#fragment", result, "Should include fragment in parameter value");
    }

    /*
     * Note: Testing the following methods would require complex setup:
     *
     * - authenticateUser(): Requires browser interaction, local server, real OAuth endpoints
     * - waitForCallback(): Requires actual socket connections and HTTP server
     * - buildAuthorizationUrl(): Could be tested but requires client_id which may not be available
     * - exchangeCodeForToken(): Requires mocking HTTPClient.post() or real OAuth token endpoint
     * - fetchUserInfo(): Requires mocking HTTPClient.get() or real OAuth userinfo endpoint
     *
     * These methods are best tested through:
     * 1. Manual integration testing with real Google OAuth
     * 2. End-to-end tests with mock OAuth server (e.g., WireMock)
     * 3. Refactoring to use dependency injection for better testability
     *
     * The current tests cover the core business logic (username generation, URL parsing)
     * which follows the KISS principle for unit testing.
     */
}
