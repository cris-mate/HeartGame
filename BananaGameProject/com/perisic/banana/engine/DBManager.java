package com.perisic.banana.engine;

import com.perisic.banana.engine.HallOfFameEntry;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles all database interactions for the Banana Game,
 * including user account management and session tracking.
 */
public class DBManager {

    private static final Logger logger = Logger.getLogger(DBManager.class.getName());
    private final Connection connection;

    /**
     * Constructs a DBManager with a given database connection.
     *
     * @param connection The JDBC connection to the banana_game database.
     */
    public DBManager(Connection connection) {
        this.connection = connection;
    }

    /**
     * Registers a new user with the given credentials and email.
     * The password is securely hashed before storage.
     *
     * @param username The desired unique username.
     * @param password The raw password, which will be hashed.
     * @param email The user's email address.
     * @return true if registration is successful; false if the username/email already exists or an error occurs.
     */
    public boolean registerUser(String username, String password, String email) {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, email);
            stmt.executeUpdate();
            logger.info("User " + username + " registered successfully.");
            return true;
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                logger.warning("User " + username + " already registered.");
            } else {
                logger.severe("Failed to register user: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Deletes a user and all related data from the database.
     *
     * @param username The username of the account to delete.
     */
    public void deleteAccount(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                logger.info("Deleted account for user: " + username);
            } else {
                logger.severe("Failed to delete user: " + username);
            }
        } catch (SQLException e) {
            logger.severe("Error during deletion for user " + username + ": " + e.getMessage());
        }
    }

    /**
     * Updates the password and/or email of an existing user.
     * If both parameters are null or empty, no update occurs.
     *
     * @param username The username of the account to update.
     * @param newPassword The new password (raw) to set; null or empty to leave unchanged.
     * @param newEmail The new email to set; null or empty to leave unchanged.
     */
    public void updateAccount(String username, String newPassword, String newEmail) {
        boolean updatePassword = newPassword != null && !newPassword.isEmpty();
        boolean updateEmail = newEmail != null && !newEmail.isEmpty();

        if (!updatePassword && !updateEmail) {
            logger.info("No changes requested for user: " + username);
            return;
        }

        StringBuilder sql = new StringBuilder("UPDATE users SET ");
        int index = 1;

        if (updatePassword) sql.append("password = ?");
        if (updateEmail) {
            if (updatePassword) sql.append(", ");
            sql.append("email = ?");
        }
        sql.append(" WHERE username = ?");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            if (updatePassword) {
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                stmt.setString(index++, hashedPassword);
            }
            if (updateEmail) {
                stmt.setString(index++, newEmail);
            }
            stmt.setString(index, username);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                logger.info("Account updated for user: " + username);
            } else {
                logger.warning("No matching user found or no changes made for user: " + username);
            }
        } catch (SQLException e) {
            logger.severe("Error updating account for user " + username + ": " + e.getMessage());
        }
    }

    /**
     * Starts a new game session for a user.
     *
     * @param username The username of the player.
     * @param sessionId A unique identifier for the session.
     * @param startTime Timestamp for the session start; uses current time if null.
     * @return true if session started successfully; false if a database error occurred.
     */
    public boolean startSession(String username, String sessionId, Timestamp startTime) {
        try {
            int userId = getUser(username);

            String query = "INSERT INTO session (session_id, user_id, start_time) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, sessionId);
                stmt.setInt(2, userId);
                stmt.setTimestamp(3, startTime != null ? startTime : new Timestamp(System.currentTimeMillis()));
                stmt.executeUpdate();
                logger.info("Started session " + sessionId + " for user: " + username);
                return true;
            }
        } catch (SQLException e) {
            logger.severe("Failed to start session for user " + username + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Overloaded method to start a new session using the current timestamp.
     *
     * @param username The username of the player.
     * @param sessionId A unique identifier for the session.
     * @return true if session started successfully; false otherwise.
     */
    public boolean startSession(String username, String sessionId) {
        return startSession(username, sessionId, null);
    }

    /**
     * Updates the session result for an existing session ID.
     * It modifies last_score, win_streak, and updates best_score if improved.
     *
     * @param username The username of the player.
     * @param sessionId The identifier for the session.
     * @param score The score achieved in the session.
     * @param winStreak The player's win streak at the end of the session.
     */
    public void saveSessionResult(String username, String sessionId, int score, int winStreak, String milestone) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.warning("Cannot save session result: sessionId is null or empty for user: " + username);
            return;
        }

        try {
            int userId = getUser(username);
            int previousBest = 0;

            String bestQuery = "SELECT best_score FROM session WHERE user_id = ? ORDER BY start_time DESC LIMIT 1";
            try (PreparedStatement bestStmt = connection.prepareStatement(bestQuery)) {
                bestStmt.setInt(1, userId);
                try (ResultSet rs = bestStmt.executeQuery()) {
                    if (rs.next()) {
                        previousBest = rs.getInt("best_score");
                    }
                }
            }

            int bestScore = Math.max(previousBest, score);

            String updateSql = "UPDATE session SET last_score = ?, best_score = ?, win_streak = ?, milestones = ? WHERE session_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
                stmt.setInt(1, score);
                stmt.setInt(2, bestScore);
                stmt.setInt(3, winStreak);
                stmt.setString(4, milestone != null ? milestone : "");
                stmt.setString(5, sessionId);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    logger.info("Session result with milestones updated for user: " + username);
                } else {
                    logger.warning("No session found to update for session_id: " + sessionId);
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to update session result for user " + username + ": " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Unexpected error while updating session result for user " + username + ": " + e.getMessage());
        }
    }

    /**
     * Retrieves the user ID from the database based on the username.
     *
     * @param username The username whose ID is to be fetched.
     * @return The integer ID of the user.
     * @throws SQLException If the user is not found or database access fails.
     */
    private int getUser(String username) throws SQLException {
        String query = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
                throw new SQLException("User not found: " + username);
            }
        }
    }

    /**
     * Retrieves the milestone text from the user's most recent session.
     *
     * @param username The username whose milestones are to be fetched.
     * @return A string of milestone data, or empty string if none found or on error.
     */
    private String getMilestoneText(String username) {
        String sql = "SELECT milestones FROM session WHERE user_id = ? ORDER BY start_time DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int userId = getUser(username);
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("milestones");
                } else {
                    logger.warning("No session found for user: " + username);
                    return "";
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to retrieve milestones for user " + username + ": " + e.getMessage());
            return "";
        }
    }

    public List<HallOfFameEntry> getHallOfFameEntries() {
        List<HallOfFameEntry> entries = new ArrayList<>();
        String sql = """
        SELECT u.username,\s
               MAX(s.best_score) AS best_score,\s
               MAX(s.win_streak) AS win_streak
        FROM session s
        JOIN users u ON s.user_id = u.id
        WHERE s.best_score >= 1
        GROUP BY s.user_id
        ORDER BY best_score DESC
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                int bestScore = rs.getInt("best_score");
                int winStreak = rs.getInt("win_streak");
                entries.add(new HallOfFameEntry(username, bestScore, winStreak));
            }

        } catch (SQLException e) {
            logger.severe("Failed to fetch Hall of Fame entries: " + e.getMessage());
        }

        return entries;
    }
}
