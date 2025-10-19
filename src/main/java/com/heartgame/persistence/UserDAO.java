package com.heartgame.persistence;

import com.heartgame.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;

/**
 * Data Access Object for User entities
 * Handles all database operations related to users
 */
public class UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private static final String NO_CONNECTION_ERROR = "Cannot query database. No connection available.";
    private final Connection connection;

    public UserDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Finds a user by username
     * @param username The username to search for
     * @return Optional containing the User if found, empty otherwise
     */
    public Optional<User> findByUsername(String username) {
        if (connection == null) {
            logger.error(NO_CONNECTION_ERROR);
            return Optional.empty();
        }

        String sql = "SELECT id, username, email, display_name, oauth_provider, oauth_id " +
                "FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
            return Optional.empty();
        }
    }

    /**
     * Finds a user by OAuth provider and OAuth ID
     * @param oauthProvider The OAuth provider (e.g., "google")
     * @param oauthId The OAuth provider's user ID
     * @return Optional containing the User if found, empty otherwise
     */
    public Optional<User> findByOAuthId(String oauthProvider, String oauthId) {
        if (connection == null) {
            logger.error(NO_CONNECTION_ERROR);
            return Optional.empty();
        }

        String sql = "SELECT id, username, email, display_name, oauth_provider, oauth_id " +
                "FROM users WHERE oauth_provider = ? AND oauth_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, oauthProvider);
            stmt.setString(2, oauthId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding user by OAuth ID: {} - {}", oauthProvider, oauthId, e);
            return Optional.empty();
        }
    }

    /**
     * Verifies a user's password
     * @param username The username
     * @param password The plaintext password to verify
     * @return True if password matches, false otherwise
     */
    public boolean verifyPassword(String username, String password) {
        if (connection == null) {
            logger.error("Cannot authenticate user. No database connection available.");
            return false;
        }

        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password_hash");
                if (hashedPassword == null) {
                    logger.warn("User '{}' has no password (OAuth user)", username);
                    return false;
                }
                return BCrypt.checkpw(password, hashedPassword);
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error verifying password for user '{}'", username, e);
            return false;
        }
    }

    /**
     * Creates a new user in the database
     * @param user The user to create
     * @param password The plaintext password (null for OAuth users)
     * @return True if creation succeeded, false otherwise
     */
    public boolean createUser(User user, String password) {
        if (connection == null) {
            logger.error("Cannot create user. No database connection available.");
            return false;
        }

        String sql = "INSERT INTO users (username, password_hash, email, display_name, oauth_provider, oauth_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());

            // Hash password only for password-based users
            if (password != null && !password.isEmpty()) {
                stmt.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getDisplayName());
            stmt.setString(5, user.getOauthProvider());
            stmt.setString(6, user.getOauthId());

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
        } catch (SQLException e) {
            logger.error("Error creating user '{}'", user.getUsername(), e);
            return false;
        }
    }

    /**
     * Updates the last_login timestamp for a user
     * @param username The username
     */
    public void updateLastLogin(String username) {
        if (connection == null) {
            logger.error("Cannot update last login. No database connection available.");
            return;
        }

        String sql = "UPDATE users SET last_login = ? WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.from(Instant.now()));
            stmt.setString(2, username);
            stmt.executeUpdate();
            logger.debug("Updated last_login for user '{}'", username);
        } catch (SQLException e) {
            logger.error("Failed to update last_login for user '{}'", username, e);
        }
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
                rs.getString("display_name"),
                rs.getString("oauth_provider"),
                rs.getString("oauth_id")
        );
    }
}
