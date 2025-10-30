package com.heartgame.persistence;

import com.heartgame.model.GameSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for GameSession entities
 * Handles all database operations related to game sessions and leaderboard
 */
public class GameSessionDAO {

    private static final Logger logger = LoggerFactory.getLogger(GameSessionDAO.class);
    private static final String NO_CONNECTION_ERROR = "Cannot query database. No connection available.";
    private final Connection connection;

    public GameSessionDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Checks if database connection is available
     * @return true if connection is available, false otherwise
     */
    private boolean hasConnection() {
        if (connection == null) {
            logger.error(NO_CONNECTION_ERROR);
            return false;
        }
        return true;
    }

    /**
     * Saves a game session to the database
     * @param session The game session to save
     * @return true if saved successfully, false otherwise
     */
    public boolean saveGameSession(GameSession session) {
        if (!hasConnection()) {
            return false;
        }

        String sql = "INSERT INTO game_sessions (user_id, start_time, end_time, final_score, questions_answered) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, session.getUserId());
            stmt.setTimestamp(2, Timestamp.from(session.getStartTime()));
            stmt.setTimestamp(3, Timestamp.from(session.getEndTime()));
            stmt.setInt(4, session.getFinalScore());
            stmt.setInt(5, session.getQuestionsAnswered());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Retrieve generated ID
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    session.setId(generatedKeys.getInt(1));
                }
                logger.info("Game session saved: user_id={}, score={}",
                        session.getUserId(), session.getFinalScore());
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error saving game session", e);
            return false;
        }
    }

    /**
     * Retrieves the top 10 highest scoring game sessions (all-time leaderboard)
     * @return List of GameSession objects with username populated
     */
    public List<GameSession> getTopTenScores() {
        if (!hasConnection()) {
            return new ArrayList<>();
        }

        String sql = "SELECT gs.id, gs.user_id, u.username, gs.start_time, gs.end_time, " +
                "gs.final_score, gs.questions_answered " +
                "FROM game_sessions gs " +
                "JOIN users u ON gs.user_id = u.id " +
                "ORDER BY gs.final_score DESC, gs.end_time ASC " +
                "LIMIT 10";

        List<GameSession> topScores = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                GameSession session = new GameSession(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getTimestamp("start_time").toInstant(),
                        rs.getTimestamp("end_time").toInstant(),
                        rs.getInt("final_score"),
                        rs.getInt("questions_answered")
                );
                topScores.add(session);
            }

            logger.debug("Retrieved {} top scores", topScores.size());
            return topScores;

        } catch (SQLException e) {
            logger.error("Error retrieving top scores", e);
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves all game sessions for a specific user
     * @param userId The user ID
     * @return List of GameSession objects for that user
     */
    public List<GameSession> getUserGameSessions(int userId) {
        if (!hasConnection()) {
            return new ArrayList<>();
        }

        String sql = "SELECT gs.id, gs.user_id, u.username, gs.start_time, gs.end_time, " +
                "gs.final_score, gs.questions_answered " +
                "FROM game_sessions gs " +
                "JOIN users u ON gs.user_id = u.id " +
                "WHERE gs.user_id = ? " +
                "ORDER BY gs.final_score DESC, gs.end_time DESC";

        List<GameSession> sessions = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                GameSession session = new GameSession(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getTimestamp("start_time").toInstant(),
                        rs.getTimestamp("end_time").toInstant(),
                        rs.getInt("final_score"),
                        rs.getInt("questions_answered")
                );
                sessions.add(session);
            }

            logger.debug("Retrieved {} sessions for user_id={}", sessions.size(), userId);
            return sessions;

        } catch (SQLException e) {
            logger.error("Error retrieving user game sessions", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets the highest score for a specific user
     * @param userId The user ID
     * @return The highest score, or 0 if no sessions found
     */
    public int getUserHighScore(int userId) {
        if (!hasConnection()) {
            return 0;
        }

        String sql = "SELECT MAX(final_score) as high_score FROM game_sessions WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("high_score");
            }
            return 0;

        } catch (SQLException e) {
            logger.error("Error retrieving user high score", e);
            return 0;
        }
    }

    /**
     * Gets the total number of game sessions in the database
     * @return Total session count
     */
    public int getTotalSessionCount() {
        if (!hasConnection()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) as total FROM game_sessions";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException e) {
            logger.error("Error retrieving total session count", e);
            return 0;
        }
    }
}