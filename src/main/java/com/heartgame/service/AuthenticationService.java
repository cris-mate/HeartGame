package com.heartgame.service;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service responsible for authentication-related operations
 * Handles password hashing and verification using BCrypt
 * Separates cryptographic operations from data access layer (follows SRP)
 */
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static final int BCRYPT_ROUNDS = 10;

    /**
     * Hashes a plaintext password using BCrypt
     * Uses auto-generated salt with default work factor
     * @param plainPassword The plaintext password to hash
     * @return BCrypt hashed password string
     * @throws IllegalArgumentException if password is null or empty
     */
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            logger.error("Attempted to hash null or empty password");
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
            logger.debug("Password hashed successfully");
            return hashedPassword;
        } catch (Exception e) {
            logger.error("Error hashing password", e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Verifies a plaintext password against a BCrypt hash
     * @param plainPassword The plaintext password to verify
     * @param hashedPassword The BCrypt hash to verify against
     * @return true if password matches hash, false otherwise
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            logger.warn("Attempted to verify null or empty password");
            return false;
        }

        if (hashedPassword == null || hashedPassword.isEmpty()) {
            logger.warn("Attempted to verify against null or empty hash");
            return false;
        }

        try {
            boolean matches = BCrypt.checkpw(plainPassword, hashedPassword);
            logger.debug("Password verification: {}", matches ? "success" : "failed");
            return matches;
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }

    /**
     * Checks if a password meets minimum security requirements
     * @param password The password to validate
     * @param minLength Minimum required length
     * @return true if password meets requirements, false otherwise
     */
    public boolean isPasswordValid(String password, int minLength) {
        if (password == null) {
            return false;
        }

        if (password.length() < minLength) {
            logger.debug("Password too short: {} < {}", password.length(), minLength);
            return false;
        }

        return true;
    }
}
