package com.heartgame.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for GameSession model
 * Tests constructors, getters, setters, and duration calculation
 */
@DisplayName("GameSession Model Tests")
class GameSessionTest {

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Should create game session without ID (for new sessions)")
    void testConstructorWithoutId() {
        Instant startTime = Instant.now();
        Instant endTime = startTime.plusSeconds(60);

        GameSession session = new GameSession(1, startTime, endTime, 5, 10);

        assertEquals(1, session.getUserId());
        assertEquals(startTime, session.getStartTime());
        assertEquals(endTime, session.getEndTime());
        assertEquals(5, session.getFinalScore());
        assertEquals(10, session.getQuestionsAnswered());
    }

    @Test
    @DisplayName("Should create game session with ID and username (from database)")
    void testConstructorWithId() {
        Instant startTime = Instant.now();
        Instant endTime = startTime.plusSeconds(60);

        GameSession session = new GameSession(100, 1, "testUser", startTime, endTime, 5, 10);

        assertEquals(100, session.getId());
        assertEquals(1, session.getUserId());
        assertEquals("testUser", session.getUsername());
        assertEquals(startTime, session.getStartTime());
        assertEquals(endTime, session.getEndTime());
        assertEquals(5, session.getFinalScore());
        assertEquals(10, session.getQuestionsAnswered());
    }

    // ========== Getter Tests ==========

    @Test
    @DisplayName("Should get ID")
    void testGetId() {
        GameSession session = new GameSession(42, 1, "user", Instant.now(), Instant.now(), 5, 10);

        assertEquals(42, session.getId());
    }

    @Test
    @DisplayName("Should get user ID")
    void testGetUserId() {
        GameSession session = new GameSession(1, 99, "user", Instant.now(), Instant.now(), 5, 10);

        assertEquals(99, session.getUserId());
    }

    @Test
    @DisplayName("Should get username")
    void testGetUsername() {
        GameSession session = new GameSession(1, 1, "playerOne", Instant.now(), Instant.now(), 5, 10);

        assertEquals("playerOne", session.getUsername());
    }

    @Test
    @DisplayName("Should get start time")
    void testGetStartTime() {
        Instant startTime = Instant.parse("2024-01-01T10:00:00Z");
        GameSession session = new GameSession(1, 1, "user", startTime, Instant.now(), 5, 10);

        assertEquals(startTime, session.getStartTime());
    }

    @Test
    @DisplayName("Should get end time")
    void testGetEndTime() {
        Instant endTime = Instant.parse("2024-01-01T10:01:00Z");
        GameSession session = new GameSession(1, 1, "user", Instant.now(), endTime, 5, 10);

        assertEquals(endTime, session.getEndTime());
    }

    @Test
    @DisplayName("Should get final score")
    void testGetFinalScore() {
        GameSession session = new GameSession(1, 1, "user", Instant.now(), Instant.now(), 42, 10);

        assertEquals(42, session.getFinalScore());
    }

    @Test
    @DisplayName("Should get questions answered")
    void testGetQuestionsAnswered() {
        GameSession session = new GameSession(1, 1, "user", Instant.now(), Instant.now(), 5, 25);

        assertEquals(25, session.getQuestionsAnswered());
    }

    // ========== Setter Tests ==========

    @Test
    @DisplayName("Should set ID")
    void testSetId() {
        GameSession session = new GameSession(1, Instant.now(), Instant.now(), 5, 10);
        session.setId(100);

        assertEquals(100, session.getId());
    }

    @Test
    @DisplayName("Should set user ID")
    void testSetUserId() {
        GameSession session = new GameSession(1, Instant.now(), Instant.now(), 5, 10);
        session.setUserId(200);

        assertEquals(200, session.getUserId());
    }

    @Test
    @DisplayName("Should set username")
    void testSetUsername() {
        GameSession session = new GameSession(1, Instant.now(), Instant.now(), 5, 10);
        session.setUsername("newUser");

        assertEquals("newUser", session.getUsername());
    }

    @Test
    @DisplayName("Should set start time")
    void testSetStartTime() {
        GameSession session = new GameSession(1, Instant.now(), Instant.now(), 5, 10);
        Instant newStartTime = Instant.parse("2024-01-01T10:00:00Z");

        session.setStartTime(newStartTime);

        assertEquals(newStartTime, session.getStartTime());
    }

    @Test
    @DisplayName("Should set end time")
    void testSetEndTime() {
        GameSession session = new GameSession(1, Instant.now(), Instant.now(), 5, 10);
        Instant newEndTime = Instant.parse("2024-01-01T10:01:00Z");

        session.setEndTime(newEndTime);

        assertEquals(newEndTime, session.getEndTime());
    }

    @Test
    @DisplayName("Should set final score")
    void testSetFinalScore() {
        GameSession session = new GameSession(1, Instant.now(), Instant.now(), 5, 10);
        session.setFinalScore(99);

        assertEquals(99, session.getFinalScore());
    }

    @Test
    @DisplayName("Should set questions answered")
    void testSetQuestionsAnswered() {
        GameSession session = new GameSession(1, Instant.now(), Instant.now(), 5, 10);
        session.setQuestionsAnswered(50);

        assertEquals(50, session.getQuestionsAnswered());
    }

    // ========== Duration Calculation Tests ==========

    @Test
    @DisplayName("Should calculate duration in seconds for 60-second game")
    void testGetDurationSeconds() {
        Instant startTime = Instant.parse("2024-01-01T10:00:00Z");
        Instant endTime = Instant.parse("2024-01-01T10:01:00Z");

        GameSession session = new GameSession(1, 1, "user", startTime, endTime, 5, 10);

        assertEquals(60, session.getDurationSeconds(),
                "Duration should be 60 seconds");
    }

    @Test
    @DisplayName("Should calculate duration for multi-minute game")
    void testGetDurationSecondsLongerGame() {
        Instant startTime = Instant.parse("2024-01-01T10:00:00Z");
        Instant endTime = Instant.parse("2024-01-01T10:05:30Z");

        GameSession session = new GameSession(1, 1, "user", startTime, endTime, 10, 20);

        assertEquals(330, session.getDurationSeconds(),
                "Duration should be 330 seconds (5.5 minutes)");
    }

    @Test
    @DisplayName("Should return 0 duration when start time is null")
    void testGetDurationSecondsNullStartTime() {
        Instant endTime = Instant.now();
        GameSession session = new GameSession(1, 1, "user", null, endTime, 5, 10);

        assertEquals(0, session.getDurationSeconds(),
                "Duration should be 0 when start time is null");
    }

    @Test
    @DisplayName("Should return 0 duration when end time is null")
    void testGetDurationSecondsNullEndTime() {
        Instant startTime = Instant.now();
        GameSession session = new GameSession(1, 1, "user", startTime, null, 5, 10);

        assertEquals(0, session.getDurationSeconds(),
                "Duration should be 0 when end time is null");
    }

    @Test
    @DisplayName("Should return 0 duration when both times are null")
    void testGetDurationSecondsBothNull() {
        GameSession session = new GameSession(1, 1, "user", null, null, 5, 10);

        assertEquals(0, session.getDurationSeconds(),
                "Duration should be 0 when both times are null");
    }

    @Test
    @DisplayName("Should calculate 0 duration for instant completion")
    void testGetDurationSecondsInstant() {
        Instant time = Instant.parse("2024-01-01T10:00:00Z");
        GameSession session = new GameSession(1, 1, "user", time, time, 0, 0);

        assertEquals(0, session.getDurationSeconds(),
                "Duration should be 0 for same start and end time");
    }

    @Test
    @DisplayName("Should handle negative duration (end before start)")
    void testGetDurationSecondsNegative() {
        Instant startTime = Instant.parse("2024-01-01T10:01:00Z");
        Instant endTime = Instant.parse("2024-01-01T10:00:00Z");

        GameSession session = new GameSession(1, 1, "user", startTime, endTime, 5, 10);

        assertEquals(-60, session.getDurationSeconds(),
                "Should calculate negative duration when end is before start");
    }

    // ========== toString Tests ==========

    @Test
    @DisplayName("Should generate toString output")
    void testToString() {
        Instant startTime = Instant.parse("2024-01-01T10:00:00Z");
        Instant endTime = Instant.parse("2024-01-01T10:01:00Z");
        GameSession session = new GameSession(1, 5, "playerOne", startTime, endTime, 10, 15);

        String toString = session.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("1"), "toString should contain ID");
        assertTrue(toString.contains("playerOne"), "toString should contain username");
        assertTrue(toString.contains("10"), "toString should contain final score");
        assertTrue(toString.contains("15"), "toString should contain questions answered");
        assertTrue(toString.contains("60"), "toString should contain duration");
    }

    // ========== Edge Case Tests ==========

    @Test
    @DisplayName("Should handle zero score")
    void testZeroScore() {
        GameSession session = new GameSession(1, Instant.now(), Instant.now(), 0, 0);

        assertEquals(0, session.getFinalScore());
        assertEquals(0, session.getQuestionsAnswered());
    }

    @Test
    @DisplayName("Should handle negative score")
    void testNegativeScore() {
        GameSession session = new GameSession(1, Instant.now(), Instant.now(), -5, 10);

        assertEquals(-5, session.getFinalScore());
    }

    @Test
    @DisplayName("Should handle very high score")
    void testVeryHighScore() {
        GameSession session = new GameSession(1, Instant.now(), Instant.now(), 999999, 100000);

        assertEquals(999999, session.getFinalScore());
        assertEquals(100000, session.getQuestionsAnswered());
    }

    @Test
    @DisplayName("Should handle null username")
    void testNullUsername() {
        GameSession session = new GameSession(1, 1, null, Instant.now(), Instant.now(), 5, 10);

        assertNull(session.getUsername());
    }

    @Test
    @DisplayName("Should handle empty username")
    void testEmptyUsername() {
        GameSession session = new GameSession(1, 1, "", Instant.now(), Instant.now(), 5, 10);

        assertEquals("", session.getUsername());
    }

    // ========== Data Integrity Tests ==========

    @Test
    @DisplayName("Should maintain data integrity after multiple setters")
    void testDataIntegrityAfterMultipleSetters() {
        GameSession session = new GameSession(1, Instant.now(), Instant.now(), 5, 10);

        session.setId(100);
        session.setUserId(200);
        session.setUsername("testUser");
        session.setFinalScore(50);
        session.setQuestionsAnswered(75);

        assertEquals(100, session.getId());
        assertEquals(200, session.getUserId());
        assertEquals("testUser", session.getUsername());
        assertEquals(50, session.getFinalScore());
        assertEquals(75, session.getQuestionsAnswered());
    }

    @Test
    @DisplayName("Should calculate correct duration after time updates")
    void testDurationAfterTimeUpdates() {
        GameSession session = new GameSession(1, Instant.now(), Instant.now(), 5, 10);

        Instant newStartTime = Instant.parse("2024-01-01T10:00:00Z");
        Instant newEndTime = Instant.parse("2024-01-01T10:02:00Z");

        session.setStartTime(newStartTime);
        session.setEndTime(newEndTime);

        assertEquals(120, session.getDurationSeconds(),
                "Duration should be updated to 120 seconds after time changes");
    }
}
