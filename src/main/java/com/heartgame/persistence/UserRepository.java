package com.heartgame.persistence;

import com.heartgame.model.User;
import java.util.Optional;

/**
 * Repository interface for User data access operations
 * Provides an abstraction layer between the application logic and the database
 */
public interface UserRepository {

    /**
     * Finds a user by their username
     * @param username The username to search for
     * @return An Optional containing the User if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their Google OAuth ID
     * @param googleId The Google ID to search for
     * @return An Optional containing the User if found, empty otherwise
     */
    Optional<User> findByGoogleId(String googleId);

    /**
     * Authenticates a user with username and password
     * @param username The username
     * @param password The plaintext password to verify
     * @return true if authentication succeeds, false otherwise
     */
    boolean authenticateUser(String username, String password);

    /**
     * Updates the last login timestamp for a user
     * @param username The username of the user to update
     * @return true if update succeeded, false otherwise
     */
    boolean updateLastLogin(String username);

    /**
     * Creates a new user account (traditional username/password)
     * @param username The username
     * @param password The plaintext password (will be hashed)
     * @return true if user was created successfully, false otherwise
     */
    boolean createUser(String username, String password);

    /**
     * Creates or updates a user account from Google OAuth
     * @param googleId The Google user ID
     * @param email The user's email from Google
     * @param name The user's display name from Google
     * @return An Optional containing the User if successful, empty otherwise
     */
    Optional<User> createOrUpdateGoogleUser(String googleId, String email, String name);

    /**
     * Checks if a username already exists
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    boolean usernameExists(String username);
}
