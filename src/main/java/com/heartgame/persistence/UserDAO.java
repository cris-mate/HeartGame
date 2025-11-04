package com.heartgame.persistence;

import com.heartgame.model.User;
import com.heartgame.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Data Access Object for User entities
 * Handles all database operations related to users
 * Extends BaseDAO for transaction support and error recovery
 */
public class UserDAO extends BaseDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private static final int RECENT_LOGIN_THRESHOLD_MINUTES = 5;
    private final AuthenticationService authService = new AuthenticationService();

    /**
     * Finds a user by username
     * Uses retry logic for resilience
     * @param username The username to search for
     * @return Optional containing the User if found, empty otherwise
     */
    public Optional<User> findByUsername(String username) {
        // ... (connection check)
        String sql = "SELECT id, username, email, oauth_provider, oauth_id " +
                "FROM users WHERE username = ?";

        // Use a safe, modern holder
        AtomicReference<Optional<User>> result = new AtomicReference<>(Optional.empty());

        executeWithRetry(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    result.set(Optional.of(mapResultSetToUser(rs))); // Set value
                    return true;
                }
                return true;
            }
        }, 2);

        return result.get(); // Get value
    }

    /**
     * Finds a user by OAuth provider and OAuth ID
     * @param oauthProvider The OAuth provider
     * @param oauthId       The OAuth provider's user ID
     * @return Optional containing the User if found, empty otherwise
     */
    public Optional<User> findByOAuthId(String oauthProvider, String oauthId) {
        // ... (connection check)
        String sql = "SELECT id, username, email, oauth_provider, oauth_id, last_login " +
                "FROM users WHERE oauth_provider = ? AND oauth_id = ?";

        // Use a safe, modern holder
        AtomicReference<Optional<User>> result = new AtomicReference<>(Optional.empty());

        executeWithRetry(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, oauthProvider);
                stmt.setString(2, oauthId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    result.set(Optional.of(mapResultSetToUser(rs))); // Set value
                }
                return true;
            }
        }, 2);

        return result.get(); // Get value
    }

    /**
     * Verifies a user's password
     * @param username The username
     * @param password The plaintext password to verify
     * @return True if password matches, false otherwise
     */
    public boolean verifyPassword(String username, String password) {
        if (!hasConnection()) {
            return false;
        }

        String sql = "SELECT password_hash FROM users WHERE username = ?";

        boolean[] verified = new boolean[]{false};

        executeWithRetry(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String hashedPassword = rs.getString("password_hash");
                    if (hashedPassword == null) {
                        logger.warn("User '{}' has no password (OAuth user)", username);
                        verified[0] = false;
                    } else {
                        verified[0] = authService.verifyPassword(password, hashedPassword);
                    }
                }
                return true;
            }
        }, 2);

        return verified[0];
    }

    /**
     * Creates a new user in the database
     * Uses transaction to ensure atomicity
     * @param user     The user to create
     * @param password The plaintext password (null for OAuth users)
     * @return True if creation succeeded, false otherwise
     */
    public boolean createUser(User user, String password) {
        if (!hasConnection()) {
            return false;
        }

        String sql = "INSERT INTO users (username, password_hash, email, oauth_provider, oauth_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        return executeInTransaction(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getUsername());

                // Hash password only for password-based users
                if (password != null && !password.isEmpty()) {
                    stmt.setString(2, authService.hashPassword(password));
                } else {
                    stmt.setNull(2, Types.VARCHAR);
                }

                stmt.setString(3, user.getEmail());
                stmt.setString(4, user.getOauthProvider());
                stmt.setString(5, user.getOauthId());

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Retrieve generated ID
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                    logger.info("User '{}' created successfully", user.getUsername());
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Updates the last_login timestamp for a user
     * Keeps original signature for backward compatibility
     * Also checks for potential multi-session scenario
     * @param username The username
     */
    public void updateLastLogin(String username) {
        if (!hasConnection()) {
            return;
        }

        // First, check if there's a recent login (multi-session detection)
        boolean recentLoginDetected = checkForRecentLogin(username);

        if (recentLoginDetected) {
            logger.warn("Multi-session detected: User '{}' logged in within the last {} minutes",
                    username, RECENT_LOGIN_THRESHOLD_MINUTES);
        }

        // Update last_login timestamp
        String sql = "UPDATE users SET last_login = ? WHERE username = ?";

        executeWithRetry(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setTimestamp(1, Timestamp.from(Instant.now()));
                stmt.setString(2, username);
                stmt.executeUpdate();
                logger.debug("Updated last_login for user '{}'", username);
                return true;
            }
        }, 2);
    }

    /**
     * Checks if user has logged in recently (multi-session detection)
     * @param username The username to check
     * @return True if user logged in within threshold, false otherwise
     */
    private boolean checkForRecentLogin(String username) {
        String sql = "SELECT last_login FROM users WHERE username = ?";

        boolean[] isRecent = new boolean[]{false};

        executeWithRetry(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Timestamp lastLogin = rs.getTimestamp("last_login");
                    if (lastLogin != null) {
                        Instant lastLoginInstant = lastLogin.toInstant();
                        Instant threshold = Instant.now().minus(RECENT_LOGIN_THRESHOLD_MINUTES, ChronoUnit.MINUTES);
                        isRecent[0] = lastLoginInstant.isAfter(threshold);
                    }
                }
                return true;
            }
        }, 1);

        return isRecent[0];
    }

    /**
     * Checks if a username already exists in the database
     * @param username The username to check
     * @return True if username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        return findByUsername(username).isPresent();
    }

    /**
     * Maps a ResultSet row to a User object
     * Eliminates code duplication in findByUsername and findByOAuthId
     * @param rs The ResultSet positioned at the current row
     * @return A User object with data from the ResultSet
     * @throws SQLException If column access fails
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("oauth_provider"),
                rs.getString("oauth_id")
        );
    }
}