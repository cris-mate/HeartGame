package com.heartgame.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for AuthenticationService
 * Tests password hashing, verification, and validation logic
 */
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {

    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthenticationService();
    }

    // ========== Password Hashing Tests ==========

    @Test
    @DisplayName("Should successfully hash a valid password")
    void testHashPassword_Success() {
        String plainPassword = "mySecurePassword123";
        String hashedPassword = authService.hashPassword(plainPassword);

        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertFalse(hashedPassword.isEmpty(), "Hashed password should not be empty");
        assertNotEquals(plainPassword, hashedPassword, "Hashed password should differ from plaintext");
        assertTrue(hashedPassword.startsWith("$2a$"), "BCrypt hash should start with $2a$");
    }

    @Test
    @DisplayName("Should produce different hashes for same password (salt test)")
    void testHashPassword_DifferentSalts() {
        String plainPassword = "testPassword";
        String hash1 = authService.hashPassword(plainPassword);
        String hash2 = authService.hashPassword(plainPassword);

        assertNotEquals(hash1, hash2, "Two hashes of same password should differ due to different salts");
    }

    @Test
    @DisplayName("Should throw exception when hashing null password")
    void testHashPassword_NullPassword() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.hashPassword(null);
        });

        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    @DisplayName("Should throw exception when hashing empty password")
    void testHashPassword_EmptyPassword() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.hashPassword("");
        });

        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    @DisplayName("Should successfully hash password with special characters")
    void testHashPassword_SpecialCharacters() {
        String passwordWithSpecialChars = "p@ssw0rd!#$%^&*()";
        String hashedPassword = authService.hashPassword(passwordWithSpecialChars);

        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$"));
    }

    @Test
    @DisplayName("Should successfully hash very long password")
    void testHashPassword_LongPassword() {
        String longPassword = "a".repeat(100);
        String hashedPassword = authService.hashPassword(longPassword);

        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$"));
    }

    // ========== Password Verification Tests ==========

    @Test
    @DisplayName("Should verify correct password successfully")
    void testVerifyPassword_CorrectPassword() {
        String plainPassword = "correctPassword123";
        String hashedPassword = authService.hashPassword(plainPassword);

        boolean result = authService.verifyPassword(plainPassword, hashedPassword);

        assertTrue(result, "Verification should succeed with correct password");
    }

    @Test
    @DisplayName("Should fail verification with incorrect password")
    void testVerifyPassword_IncorrectPassword() {
        String plainPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hashedPassword = authService.hashPassword(plainPassword);

        boolean result = authService.verifyPassword(wrongPassword, hashedPassword);

        assertFalse(result, "Verification should fail with incorrect password");
    }

    @Test
    @DisplayName("Should fail verification with null plain password")
    void testVerifyPassword_NullPlainPassword() {
        String hashedPassword = authService.hashPassword("somePassword");

        boolean result = authService.verifyPassword(null, hashedPassword);

        assertFalse(result, "Verification should fail with null plain password");
    }

    @Test
    @DisplayName("Should fail verification with empty plain password")
    void testVerifyPassword_EmptyPlainPassword() {
        String hashedPassword = authService.hashPassword("somePassword");

        boolean result = authService.verifyPassword("", hashedPassword);

        assertFalse(result, "Verification should fail with empty plain password");
    }

    @Test
    @DisplayName("Should fail verification with null hashed password")
    void testVerifyPassword_NullHashedPassword() {
        boolean result = authService.verifyPassword("somePassword", null);

        assertFalse(result, "Verification should fail with null hashed password");
    }

    @Test
    @DisplayName("Should fail verification with empty hashed password")
    void testVerifyPassword_EmptyHashedPassword() {
        boolean result = authService.verifyPassword("somePassword", "");

        assertFalse(result, "Verification should fail with empty hashed password");
    }

    @Test
    @DisplayName("Should fail verification with invalid hash format")
    void testVerifyPassword_InvalidHashFormat() {
        boolean result = authService.verifyPassword("password", "not-a-valid-bcrypt-hash");

        assertFalse(result, "Verification should fail with invalid hash format");
    }

    @Test
    @DisplayName("Should verify password with special characters")
    void testVerifyPassword_SpecialCharacters() {
        String passwordWithSpecialChars = "p@ssw0rd!#$%^&*()";
        String hashedPassword = authService.hashPassword(passwordWithSpecialChars);

        boolean result = authService.verifyPassword(passwordWithSpecialChars, hashedPassword);

        assertTrue(result, "Verification should succeed with special characters");
    }

    @Test
    @DisplayName("Should be case-sensitive in verification")
    void testVerifyPassword_CaseSensitive() {
        String plainPassword = "Password123";
        String hashedPassword = authService.hashPassword(plainPassword);

        boolean resultLowerCase = authService.verifyPassword("password123", hashedPassword);
        boolean resultUpperCase = authService.verifyPassword("PASSWORD123", hashedPassword);

        assertFalse(resultLowerCase, "Verification should be case-sensitive (lowercase)");
        assertFalse(resultUpperCase, "Verification should be case-sensitive (uppercase)");
    }

    // ========== Password Validation Tests ==========

    @Test
    @DisplayName("Should validate password meeting minimum length")
    void testIsPasswordValid_MeetsMinimumLength() {
        String validPassword = "password";
        int minLength = 8;

        boolean result = authService.isPasswordValid(validPassword, minLength);

        assertTrue(result, "Password meeting minimum length should be valid");
    }

    @Test
    @DisplayName("Should validate password exceeding minimum length")
    void testIsPasswordValid_ExceedsMinimumLength() {
        String validPassword = "veryLongPassword123";
        int minLength = 8;

        boolean result = authService.isPasswordValid(validPassword, minLength);

        assertTrue(result, "Password exceeding minimum length should be valid");
    }

    @Test
    @DisplayName("Should reject password below minimum length")
    void testIsPasswordValid_BelowMinimumLength() {
        String shortPassword = "pass";
        int minLength = 8;

        boolean result = authService.isPasswordValid(shortPassword, minLength);

        assertFalse(result, "Password below minimum length should be invalid");
    }

    @Test
    @DisplayName("Should reject null password")
    void testIsPasswordValid_NullPassword() {
        int minLength = 8;

        boolean result = authService.isPasswordValid(null, minLength);

        assertFalse(result, "Null password should be invalid");
    }

    @Test
    @DisplayName("Should reject empty password")
    void testIsPasswordValid_EmptyPassword() {
        int minLength = 1;

        boolean result = authService.isPasswordValid("", minLength);

        assertFalse(result, "Empty password should be invalid");
    }

    @Test
    @DisplayName("Should validate with minimum length of zero")
    void testIsPasswordValid_ZeroMinLength() {
        String password = "";
        int minLength = 0;

        boolean result = authService.isPasswordValid(password, minLength);

        assertTrue(result, "Empty password should be valid when minimum length is 0");
    }

    @Test
    @DisplayName("Should validate password exactly at minimum length")
    void testIsPasswordValid_ExactMinimumLength() {
        String password = "12345678";
        int minLength = 8;

        boolean result = authService.isPasswordValid(password, minLength);

        assertTrue(result, "Password exactly at minimum length should be valid");
    }

    // ========== Integration Tests ==========

    @Test
    @DisplayName("Should hash and verify password end-to-end")
    void testHashAndVerify_Integration() {
        String originalPassword = "myPassword123!";

        // Hash the password
        String hashedPassword = authService.hashPassword(originalPassword);

        // Verify with correct password
        assertTrue(authService.verifyPassword(originalPassword, hashedPassword),
                "Should verify with original password");

        // Verify with incorrect password
        assertFalse(authService.verifyPassword("wrongPassword", hashedPassword),
                "Should not verify with wrong password");
    }

    @Test
    @DisplayName("Should handle multiple hash and verify operations")
    void testMultipleOperations() {
        String password1 = "password1";
        String password2 = "password2";

        String hash1 = authService.hashPassword(password1);
        String hash2 = authService.hashPassword(password2);

        assertTrue(authService.verifyPassword(password1, hash1));
        assertTrue(authService.verifyPassword(password2, hash2));
        assertFalse(authService.verifyPassword(password1, hash2));
        assertFalse(authService.verifyPassword(password2, hash1));
    }
}