package com.heartgame.persistence;

import com.heartgame.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;

/**
 * JDBC-based implementation of UserRepository.
 * Handles all database operations related to User entities.
 */
public class UserRepositoryImpl implements UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);
    private final Connection connection;

    /**
     * Constructs a UserRepositoryImpl with the given database connection
     * @param connection The database connection to use
     */
    public UserRepositoryImpl(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Database connection cannot be null");
        }
        this.connection = connection;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT username FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(rs.getString("username"));
                logger.debug("User '{}' found in database", username);
                return Optional.of(user);
            }

            logger.debug("User '{}' not found in database", username);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding user by username: '{}'", username, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByGoogleId(String googleId) {
        String sql = "SELECT username FROM users WHERE google_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, googleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(rs.getString("username"));
                logger.debug("User with Google ID '{}' found in database", googleId);
                return Optional.of(user);
            }

            logger.debug("User with Google ID '{}' not found in database", googleId);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error finding user by Google ID: '{}'", googleId, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean authenticateUser(String username, String password) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password_hash");

                // Handle Google OAuth users (they don't have password_hash)
                if (hashedPassword == null || hashedPassword.isEmpty()) {
                    logger.warn("Authentication attempted for Google OAuth user: '{}'", username);
                    return false;
                }

                boolean authenticated = BCrypt.checkpw(password, hashedPassword);

                if (authenticated) {
                    logger.info("User '{}' authenticated successfully", username);
                } else {
                    logger.warn("Authentication failed for user: '{}'", username);
                }

                return authenticated;
            }

            logger.warn("Authentication failed: user '{}' not found", username);
            return false;

        } catch (SQLException e) {
            logger.error("Error during authentication for user '{}'", username, e);
            return false;
        }
    }

    @Override
    public boolean updateLastLogin(String username) {
        String sql = "UPDATE users SET last_login = ? WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.from(Instant.now()));
            stmt.setString(2, username);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Updated last_login for user '{}'", username);
                return true;
            }

            logger.warn("Failed to update last_login: user '{}' not found", username);
            return false;

        } catch (SQLException e) {
            logger.error("Error updating last_login for user '{}'", username, e);
            return false;
        }
    }

    @Override
    public boolean createUser(String username, String password) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("User '{}' created successfully", username);
                return true;
            }

            logger.warn("Failed to create user '{}'", username);
            return false;

        } catch (SQLException e) {
            logger.error("Error creating user '{}'", username, e);
            return false;
        }
    }

    @Override
    public Optional<User> createOrUpdateGoogleUser(String googleId, String email, String name) {
        // First, check if user already exists
        Optional<User> existingUser = findByGoogleId(googleId);

        if (existingUser.isPresent()) {
            // Update last login
            updateLastLogin(existingUser.get().getUsername());
            logger.info("Existing Google user logged in: '{}'", existingUser.get().getUsername());
            return existingUser;
        }

        // Create new user from Google account
        // Use email prefix as username (or full email if preferred)
        String username = email.split("@")[0];

        // Ensure username is unique by appending number if needed
        String finalUsername = ensureUniqueUsername(username);

        String sql = "INSERT INTO users (username, google_id, email, password_hash) VALUES (?, ?, ?, NULL)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, finalUsername);
            stmt.setString(2, googleId);
            stmt.setString(3, email);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                User newUser = new User(finalUsername);
                logger.info("New Google user '{}' created with email '{}'", finalUsername, email);
                return Optional.of(newUser);
            }

            logger.error("Failed to create Google user from email '{}'", email);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error creating Google user from email '{}'", email, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error checking if username exists: '{}'", username, e);
            return false;
        }
    }

    /**
     * Ensures a username is unique by appending a number if necessary
     * @param baseUsername The base username to check
     * @return A unique username
     */
    private String ensureUniqueUsername(String baseUsername) {
        String candidate = baseUsername;
        int suffix = 1;

        while (usernameExists(candidate)) {
            candidate = baseUsername + suffix;
            suffix++;
        }

        return candidate;
    }
}
