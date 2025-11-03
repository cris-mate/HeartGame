package com.heartgame.persistence;

import com.heartgame.model.GameSession;
import com.heartgame.model.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for GameSessionDAO
 * Tests game session persistence, leaderboard, and statistics using H2 in-memory database
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GameSessionDAOTest {

    private static DatabaseManager dbManager;
    private static Connection connection;
    private GameSessionDAO sessionDAO;
    private UserDAO userDAO;

    // Test user IDs
    private int user1Id;
    private int user2Id;
    private int user3Id;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        dbManager = DatabaseManager.getInstance();
        connection = dbManager.getConnection();
        DatabaseTestHelper.initializeSchema(connection);
    }

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseTestHelper.clearAllData(connection);
        sessionDAO = new GameSessionDAO();
        userDAO = new UserDAO();

        // Create test users for foreign key constraints
        User user1 = new User("player1");
        User user2 = new User("player2");
        User user3 = new User("player3");

        userDAO.createUser(user1, "pass1");
        userDAO.createUser(user2, "pass2");
        userDAO.createUser(user3, "pass3");

        user1Id = user1.getId();
        user2Id = user2.getId();
        user3Id = user3.getId();
    }

    // ==================== saveGameSession Tests ====================

    @Test
    @Order(1)
    @DisplayName("saveGameSession() saves session successfully")
    void testSaveGameSession() {
        Instant start = Instant.now().minus(60, ChronoUnit.SECONDS);
        Instant end = Instant.now();
        GameSession session = new GameSession(user1Id, start, end, 100, 5);

        boolean result = sessionDAO.saveGameSession(session);

        assertTrue(result, "Should save session successfully");
        assertTrue(session.getId() > 0, "Should set generated ID");
    }

    @Test
    @Order(2)
    @DisplayName("saveGameSession() persists all fields correctly")
    void testSaveGameSessionFields() throws SQLException {
        Instant start = Instant.parse("2024-01-01T10:00:00Z");
        Instant end = Instant.parse("2024-01-01T10:01:00Z");
        GameSession session = new GameSession(user1Id, start, end, 250, 10);

        sessionDAO.saveGameSession(session);

        // Verify by querying directly
        var stmt = connection.createStatement();
        var rs = stmt.executeQuery("SELECT * FROM game_sessions WHERE id = " + session.getId());

        assertTrue(rs.next(), "Session should exist in database");
        assertEquals(user1Id, rs.getInt("user_id"));
        assertEquals(250, rs.getInt("final_score"));
        assertEquals(10, rs.getInt("questions_answered"));
        assertNotNull(rs.getTimestamp("start_time"));
        assertNotNull(rs.getTimestamp("end_time"));

        rs.close();
        stmt.close();
    }

    @Test
    @Order(3)
    @DisplayName("saveGameSession() handles zero score")
    void testSaveGameSessionZeroScore() {
        GameSession session = new GameSession(user1Id, Instant.now(), Instant.now(), 0, 0);
        boolean result = sessionDAO.saveGameSession(session);

        assertTrue(result, "Should save session with zero score");
    }

    @Test
    @Order(4)
    @DisplayName("saveGameSession() rejects invalid user ID")
    void testSaveGameSessionInvalidUserId() {
        GameSession session = new GameSession(99999, Instant.now(), Instant.now(), 100, 5);
        boolean result = sessionDAO.saveGameSession(session);

        assertFalse(result, "Should reject invalid user_id (foreign key violation)");
    }

    @Test
    @Order(5)
    @DisplayName("saveGameSession() can save multiple sessions for same user")
    void testSaveMultipleSessionsSameUser() {
        GameSession session1 = new GameSession(user1Id, Instant.now(), Instant.now(), 100, 5);
        GameSession session2 = new GameSession(user1Id, Instant.now(), Instant.now(), 200, 10);

        assertTrue(sessionDAO.saveGameSession(session1));
        assertTrue(sessionDAO.saveGameSession(session2));
        assertNotEquals(session1.getId(), session2.getId(), "Should have different IDs");
    }

    // ==================== getTotalSessionCount Tests ====================

    @Test
    @Order(6)
    @DisplayName("getTotalSessionCount() returns 0 when empty")
    void testGetTotalSessionCountEmpty() {
        int count = sessionDAO.getTotalSessionCount();

        assertEquals(0, count, "Should return 0 for empty database");
    }

    @Test
    @Order(7)
    @DisplayName("getTotalSessionCount() returns correct count")
    void testGetTotalSessionCount() {
        // Save 3 sessions
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 100, 5));
        sessionDAO.saveGameSession(new GameSession(user2Id, Instant.now(), Instant.now(), 200, 10));
        sessionDAO.saveGameSession(new GameSession(user3Id, Instant.now(), Instant.now(), 150, 7));

        int count = sessionDAO.getTotalSessionCount();

        assertEquals(3, count, "Should count all sessions");
    }

    // ==================== getUserHighScore Tests ====================

    @Test
    @Order(8)
    @DisplayName("getUserHighScore() returns 0 for user with no sessions")
    void testGetUserHighScoreNoSessions() {
        int highScore = sessionDAO.getUserHighScore(user1Id);

        assertEquals(0, highScore, "Should return 0 for user with no sessions");
    }

    @Test
    @Order(9)
    @DisplayName("getUserHighScore() returns score for single session")
    void testGetUserHighScoreSingleSession() {
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 250, 10));

        int highScore = sessionDAO.getUserHighScore(user1Id);

        assertEquals(250, highScore, "Should return the session's score");
    }

    @Test
    @Order(10)
    @DisplayName("getUserHighScore() returns highest score from multiple sessions")
    void testGetUserHighScoreMultipleSessions() {
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 100, 5));
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 500, 20));
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 250, 12));

        int highScore = sessionDAO.getUserHighScore(user1Id);

        assertEquals(500, highScore, "Should return the highest score");
    }

    @Test
    @Order(11)
    @DisplayName("getUserHighScore() isolates scores by user")
    void testGetUserHighScoreIsolation() {
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 100, 5));
        sessionDAO.saveGameSession(new GameSession(user2Id, Instant.now(), Instant.now(), 500, 20));

        int highScore = sessionDAO.getUserHighScore(user1Id);

        assertEquals(100, highScore, "Should only consider user1's scores");
    }

    // ==================== getUserGameSessions Tests ====================

    @Test
    @Order(12)
    @DisplayName("getUserGameSessions() returns empty list for user with no sessions")
    void testGetUserGameSessionsEmpty() {
        List<GameSession> sessions = sessionDAO.getUserGameSessions(user1Id);

        assertNotNull(sessions, "Should return non-null list");
        assertTrue(sessions.isEmpty(), "Should be empty for user with no sessions");
    }

    @Test
    @Order(13)
    @DisplayName("getUserGameSessions() returns user's sessions")
    void testGetUserGameSessions() {
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 100, 5));
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 200, 10));

        List<GameSession> sessions = sessionDAO.getUserGameSessions(user1Id);

        assertEquals(2, sessions.size(), "Should return both sessions");
    }

    @Test
    @Order(14)
    @DisplayName("getUserGameSessions() populates username field")
    void testGetUserGameSessionsIncludesUsername() {
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 100, 5));

        List<GameSession> sessions = sessionDAO.getUserGameSessions(user1Id);

        assertEquals(1, sessions.size());
        assertEquals("player1", sessions.get(0).getUsername(), "Should populate username from join");
    }

    @Test
    @Order(15)
    @DisplayName("getUserGameSessions() orders by score DESC, then time DESC")
    void testGetUserGameSessionsOrdering() throws InterruptedException {
        Instant time1 = Instant.now();
        Thread.sleep(10); // Ensure different timestamps
        Instant time2 = Instant.now();
        Thread.sleep(10);
        Instant time3 = Instant.now();

        // Save in random order: low score first, high score middle, medium score last
        sessionDAO.saveGameSession(new GameSession(user1Id, time1, time1, 100, 5));
        sessionDAO.saveGameSession(new GameSession(user1Id, time2, time2, 300, 15));
        sessionDAO.saveGameSession(new GameSession(user1Id, time3, time3, 200, 10));

        List<GameSession> sessions = sessionDAO.getUserGameSessions(user1Id);

        assertEquals(3, sessions.size());
        // Should be ordered by score DESC: 300, 200, 100
        assertEquals(300, sessions.get(0).getFinalScore());
        assertEquals(200, sessions.get(1).getFinalScore());
        assertEquals(100, sessions.get(2).getFinalScore());
    }

    @Test
    @Order(16)
    @DisplayName("getUserGameSessions() isolates sessions by user")
    void testGetUserGameSessionsIsolation() {
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 100, 5));
        sessionDAO.saveGameSession(new GameSession(user2Id, Instant.now(), Instant.now(), 200, 10));
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 150, 7));

        List<GameSession> sessions = sessionDAO.getUserGameSessions(user1Id);

        assertEquals(2, sessions.size(), "Should only return user1's sessions");
        assertTrue(sessions.stream().allMatch(s -> s.getUserId() == user1Id));
    }

    // ==================== getTopTenScores Tests ====================

    @Test
    @Order(17)
    @DisplayName("getTopTenScores() returns empty list when no sessions")
    void testGetTopTenScoresEmpty() {
        List<GameSession> topScores = sessionDAO.getTopTenScores();

        assertNotNull(topScores, "Should return non-null list");
        assertTrue(topScores.isEmpty(), "Should be empty when no sessions exist");
    }

    @Test
    @Order(18)
    @DisplayName("getTopTenScores() returns sessions ordered by score DESC")
    void testGetTopTenScoresOrdering() {
        // Create sessions with different scores
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 100, 5));
        sessionDAO.saveGameSession(new GameSession(user2Id, Instant.now(), Instant.now(), 300, 15));
        sessionDAO.saveGameSession(new GameSession(user3Id, Instant.now(), Instant.now(), 200, 10));

        List<GameSession> topScores = sessionDAO.getTopTenScores();

        assertEquals(3, topScores.size());
        assertEquals(300, topScores.get(0).getFinalScore(), "Highest score should be first");
        assertEquals(200, topScores.get(1).getFinalScore());
        assertEquals(100, topScores.get(2).getFinalScore(), "Lowest score should be last");
    }

    @Test
    @Order(19)
    @DisplayName("getTopTenScores() includes usernames")
    void testGetTopTenScoresIncludesUsernames() {
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 100, 5));
        sessionDAO.saveGameSession(new GameSession(user2Id, Instant.now(), Instant.now(), 200, 10));

        List<GameSession> topScores = sessionDAO.getTopTenScores();

        assertEquals(2, topScores.size());
        assertNotNull(topScores.get(0).getUsername(), "Should have username");
        assertNotNull(topScores.get(1).getUsername(), "Should have username");
    }

    @Test
    @Order(20)
    @DisplayName("getTopTenScores() limits to 10 results")
    void testGetTopTenScoresLimits() {
        // Create 15 sessions
        for (int i = 0; i < 15; i++) {
            int userId = (i % 3 == 0) ? user1Id : (i % 3 == 1) ? user2Id : user3Id;
            sessionDAO.saveGameSession(new GameSession(userId, Instant.now(), Instant.now(), i * 10, i));
        }

        List<GameSession> topScores = sessionDAO.getTopTenScores();

        assertEquals(10, topScores.size(), "Should limit to exactly 10 results");
    }

    @Test
    @Order(21)
    @DisplayName("getTopTenScores() returns top 10 by score")
    void testGetTopTenScoresReturnsHighest() {
        // Create 15 sessions with scores 0, 10, 20, ..., 140
        for (int i = 0; i < 15; i++) {
            int userId = (i % 3 == 0) ? user1Id : (i % 3 == 1) ? user2Id : user3Id;
            sessionDAO.saveGameSession(new GameSession(userId, Instant.now(), Instant.now(), i * 10, i));
        }

        List<GameSession> topScores = sessionDAO.getTopTenScores();

        // Top 10 should be: 140, 130, 120, ..., 50
        assertEquals(140, topScores.get(0).getFinalScore(), "Should have highest score");
        assertEquals(50, topScores.get(9).getFinalScore(), "10th should be score 50");
    }

    @Test
    @Order(22)
    @DisplayName("getTopTenScores() handles ties with time ordering")
    void testGetTopTenScoresTieBreaking() throws InterruptedException {
        Instant time1 = Instant.now();
        Thread.sleep(10);
        Instant time2 = Instant.now();

        // Two sessions with same score but different times
        sessionDAO.saveGameSession(new GameSession(user1Id, time1, time1, 100, 5));
        sessionDAO.saveGameSession(new GameSession(user2Id, time2, time2, 100, 5));

        List<GameSession> topScores = sessionDAO.getTopTenScores();

        assertEquals(2, topScores.size());
        // Both have same score, but ordering by end_time should differentiate them
        assertEquals(100, topScores.get(0).getFinalScore());
        assertEquals(100, topScores.get(1).getFinalScore());
    }

    // ==================== Integration Tests ====================

    @Test
    @Order(23)
    @DisplayName("Complete leaderboard scenario with multiple users")
    void testCompleteLeaderboardScenario() {
        // User 1: 3 games
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 100, 5));
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 300, 15));
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 200, 10));

        // User 2: 2 games
        sessionDAO.saveGameSession(new GameSession(user2Id, Instant.now(), Instant.now(), 350, 17));
        sessionDAO.saveGameSession(new GameSession(user2Id, Instant.now(), Instant.now(), 150, 7));

        // User 3: 1 game
        sessionDAO.saveGameSession(new GameSession(user3Id, Instant.now(), Instant.now(), 250, 12));

        // Total sessions
        assertEquals(6, sessionDAO.getTotalSessionCount());

        // User high scores
        assertEquals(300, sessionDAO.getUserHighScore(user1Id));
        assertEquals(350, sessionDAO.getUserHighScore(user2Id));
        assertEquals(250, sessionDAO.getUserHighScore(user3Id));

        // User sessions
        assertEquals(3, sessionDAO.getUserGameSessions(user1Id).size());
        assertEquals(2, sessionDAO.getUserGameSessions(user2Id).size());
        assertEquals(1, sessionDAO.getUserGameSessions(user3Id).size());

        // Leaderboard
        List<GameSession> leaderboard = sessionDAO.getTopTenScores();
        assertEquals(6, leaderboard.size());
        assertEquals(350, leaderboard.get(0).getFinalScore(), "user2's best should be #1");
    }

    @Test
    @Order(24)
    @DisplayName("Session data integrity after save and retrieve")
    void testSessionDataIntegrity() {
        Instant start = Instant.parse("2024-06-15T14:30:00Z");
        Instant end = Instant.parse("2024-06-15T14:31:00Z");
        GameSession original = new GameSession(user1Id, start, end, 450, 18);

        sessionDAO.saveGameSession(original);
        List<GameSession> retrieved = sessionDAO.getUserGameSessions(user1Id);

        assertEquals(1, retrieved.size());
        GameSession saved = retrieved.get(0);

        assertEquals(user1Id, saved.getUserId());
        assertEquals("player1", saved.getUsername());
        assertEquals(450, saved.getFinalScore());
        assertEquals(18, saved.getQuestionsAnswered());
        assertNotNull(saved.getStartTime());
        assertNotNull(saved.getEndTime());
        assertTrue(saved.getId() > 0);
    }

    @Test
    @Order(25)
    @DisplayName("Cascade delete: deleting user deletes sessions")
    void testCascadeDelete() throws SQLException {
        // Create session for user1
        sessionDAO.saveGameSession(new GameSession(user1Id, Instant.now(), Instant.now(), 100, 5));
        assertEquals(1, sessionDAO.getTotalSessionCount());

        // Delete user1
        var stmt = connection.createStatement();
        stmt.execute("DELETE FROM users WHERE id = " + user1Id);
        stmt.close();

        // Sessions should be deleted too (CASCADE)
        assertEquals(0, sessionDAO.getTotalSessionCount(), "Sessions should be deleted with user");
    }
}
