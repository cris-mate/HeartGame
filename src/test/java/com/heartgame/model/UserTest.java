package com.heartgame.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for User model
 * Tests constructors, getters, setters, and OAuth detection
 */
@DisplayName("User Model Tests")
class UserTest {

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Should create user with username only (legacy constructor)")
    void testLegacyConstructor() {
        User user = new User("testUser");

        assertEquals("testUser", user.getUsername());
        assertEquals("password", user.getOauthProvider(),
                "Default auth provider should be 'password'");
        assertFalse(user.isOAuthUser(), "Should not be OAuth user");
    }

    @Test
    @DisplayName("Should create user with full OAuth details")
    void testOAuthConstructor() {
        User user = new User("googleUser", "user@example.com", "google", "google-id-123");

        assertEquals("googleUser", user.getUsername());
        assertEquals("user@example.com", user.getEmail());
        assertEquals("google", user.getOauthProvider());
        assertEquals("google-id-123", user.getOauthId());
        assertTrue(user.isOAuthUser(), "Should be OAuth user");
    }

    @Test
    @DisplayName("Should create user with database ID")
    void testFullConstructor() {
        User user = new User(1, "dbUser", "db@example.com", "github", "github-id-456");

        assertEquals(1, user.getId());
        assertEquals("dbUser", user.getUsername());
        assertEquals("db@example.com", user.getEmail());
        assertEquals("github", user.getOauthProvider());
        assertEquals("github-id-456", user.getOauthId());
    }

    // ========== Getter Tests ==========

    @Test
    @DisplayName("Should get username")
    void testGetUsername() {
        User user = new User("myUsername");

        assertEquals("myUsername", user.getUsername());
    }

    @Test
    @DisplayName("Should get email")
    void testGetEmail() {
        User user = new User("user", "test@example.com", "google", "id-123");

        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Should get OAuth provider")
    void testGetOauthProvider() {
        User user = new User("user", "test@example.com", "google", "id-123");

        assertEquals("google", user.getOauthProvider());
    }

    @Test
    @DisplayName("Should get OAuth ID")
    void testGetOauthId() {
        User user = new User("user", "test@example.com", "google", "id-123");

        assertEquals("id-123", user.getOauthId());
    }

    @Test
    @DisplayName("Should get ID")
    void testGetId() {
        User user = new User(42, "user", "test@example.com", "google", "id-123");

        assertEquals(42, user.getId());
    }

    // ========== Setter Tests ==========

    @Test
    @DisplayName("Should set ID")
    void testSetId() {
        User user = new User("user");
        user.setId(100);

        assertEquals(100, user.getId());
    }

    @Test
    @DisplayName("Should set email")
    void testSetEmail() {
        User user = new User("user");
        user.setEmail("newemail@example.com");

        assertEquals("newemail@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Should update email")
    void testUpdateEmail() {
        User user = new User("user", "old@example.com", "google", "id-123");
        assertEquals("old@example.com", user.getEmail());

        user.setEmail("new@example.com");

        assertEquals("new@example.com", user.getEmail());
    }

    // ========== OAuth Detection Tests ==========

    @Test
    @DisplayName("Should detect password-based user")
    void testIsOAuthUserForPasswordUser() {
        User user = new User("passwordUser");

        assertFalse(user.isOAuthUser(),
                "User with 'password' provider should not be OAuth user");
    }

    @Test
    @DisplayName("Should detect OAuth user with Google")
    void testIsOAuthUserForGoogle() {
        User user = new User("googleUser", "user@example.com", "google", "google-id");

        assertTrue(user.isOAuthUser(), "Google user should be OAuth user");
    }

    @Test
    @DisplayName("Should detect OAuth user with GitHub")
    void testIsOAuthUserForGitHub() {
        User user = new User("githubUser", "user@example.com", "github", "github-id");

        assertTrue(user.isOAuthUser(), "GitHub user should be OAuth user");
    }

    @Test
    @DisplayName("Should handle null OAuth provider")
    void testIsOAuthUserWithNullProvider() {
        User user = new User(1, "user", "test@example.com", null, "id-123");

        assertFalse(user.isOAuthUser(),
                "User with null provider should not be OAuth user");
    }

    @Test
    @DisplayName("Should treat explicit 'password' provider as non-OAuth")
    void testIsOAuthUserWithExplicitPassword() {
        User user = new User(1, "user", "test@example.com", "password", null);

        assertFalse(user.isOAuthUser(),
                "User with explicit 'password' provider should not be OAuth user");
    }

    // ========== Username Immutability Tests ==========

    @Test
    @DisplayName("Username should be final and immutable")
    void testUsernameImmutability() {
        User user = new User("originalUsername");

        // Username is final, so there's no setter - this test just verifies getter consistency
        assertEquals("originalUsername", user.getUsername());
        assertEquals("originalUsername", user.getUsername());
    }

    // ========== OAuth Provider Immutability Tests ==========

    @Test
    @DisplayName("OAuth provider should be final and immutable")
    void testOAuthProviderImmutability() {
        User user = new User("user", "test@example.com", "google", "id-123");

        // OAuth provider is final, so there's no setter - this test verifies getter consistency
        assertEquals("google", user.getOauthProvider());
        assertEquals("google", user.getOauthProvider());
    }

    // ========== toString Tests ==========

    @Test
    @DisplayName("Should generate toString output")
    void testToString() {
        User user = new User(1, "testUser", "test@example.com", "google", "google-id");

        String toString = user.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("testUser"), "toString should contain username");
        assertTrue(toString.contains("test@example.com"), "toString should contain email");
        assertTrue(toString.contains("google"), "toString should contain OAuth provider");
        assertTrue(toString.contains("1"), "toString should contain ID");
    }

    @Test
    @DisplayName("Should handle toString with null email")
    void testToStringWithNullEmail() {
        User user = new User("user");

        String toString = user.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("user"), "toString should contain username");
    }

    // ========== Edge Case Tests ==========

    @Test
    @DisplayName("Should handle empty username")
    void testEmptyUsername() {
        User user = new User("");

        assertEquals("", user.getUsername());
    }

    @Test
    @DisplayName("Should handle username with special characters")
    void testUsernameWithSpecialCharacters() {
        User user = new User("user_name.123-test");

        assertEquals("user_name.123-test", user.getUsername());
    }

    @Test
    @DisplayName("Should handle long username")
    void testLongUsername() {
        String longUsername = "a".repeat(100);
        User user = new User(longUsername);

        assertEquals(longUsername, user.getUsername());
    }

    @Test
    @DisplayName("Should handle null email in setter")
    void testSetNullEmail() {
        User user = new User("user", "test@example.com", "google", "id-123");

        user.setEmail(null);

        assertNull(user.getEmail());
    }

    @Test
    @DisplayName("Should handle zero ID")
    void testZeroId() {
        User user = new User(0, "user", "test@example.com", "google", "id-123");

        assertEquals(0, user.getId());
    }

    @Test
    @DisplayName("Should handle negative ID")
    void testNegativeId() {
        User user = new User("user");
        user.setId(-1);

        assertEquals(-1, user.getId());
    }

    // ========== Multiple OAuth Providers Tests ==========

    @Test
    @DisplayName("Should differentiate between multiple OAuth providers")
    void testMultipleOAuthProviders() {
        User googleUser = new User("user1", "user1@example.com", "google", "google-id");
        User githubUser = new User("user2", "user2@example.com", "github", "github-id");
        User passwordUser = new User("user3");

        assertTrue(googleUser.isOAuthUser());
        assertTrue(githubUser.isOAuthUser());
        assertFalse(passwordUser.isOAuthUser());

        assertEquals("google", googleUser.getOauthProvider());
        assertEquals("github", githubUser.getOauthProvider());
        assertEquals("password", passwordUser.getOauthProvider());
    }
}
