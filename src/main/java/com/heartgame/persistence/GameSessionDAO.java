package com.heartgame.persistence;

import com.heartgame.model.GameSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for GameSession entities
 * Handles all database operations related to game sessions and leaderboard
 * Extends BaseDAO for transaction support and error recovery
 */
public class GameSessionDAO extends BaseDAO {

    private static final Logger logger = LoggerFactory.getLogger(GameSessionDAO.class);

    /**
     * Saves a game session to the database
     * Uses transaction to ensure data integrity
     * @param session The game session to save
     * @return true if saved successfully, false otherwise
     */
    public boolean saveGameSession(GameSession session) {
        if (!hasConnection()) {
            return false;
        }

        String sql = "INSERT INTO game_sessions (user_id, start_time, end_time, final_score, questions_answered) " +
                "VALUES (?, ?, ?, ?, ?)";

        return executeInTransaction(() -> {
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
            }
        });
    }

    /**
     * Retrieves the top 10 highest scoring game sessions (all-time leaderboard)
     * Uses retry logic for resilience
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
                "ORDER BY gs.final_score DESC, gs.end_time " +
                "LIMIT 10";

        List<GameSession> topScores = new ArrayList<>();

        executeWithRetry(() -> {
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
                return true;
            }
        }, 2);

        return topScores;
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

        executeWithRetry(() -> {
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
                return true;
            }
        }, 2);

        return sessions;
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

        int[] highScore = new int[]{0};

        executeWithRetry(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    highScore[0] = rs.getInt("high_score");
                }
                return true;
            }
        }, 2);

        return highScore[0];
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

        int[] total = new int[]{0};

        executeWithRetry(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    total[0] = rs.getInt("total");
                }
                return true;
            }
        }, 2);

        return total[0];
    }
}