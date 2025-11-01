package com.heartgame.service;

import com.heartgame.event.GameEventManager;
import com.heartgame.event.GameEventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ScoringService
 * Tests score tracking through event-driven architecture
 */
@DisplayName("ScoringService Tests")
class ScoringServiceTest {

    private ScoringService scoringService;
    private GameEventManager eventManager;

    @BeforeEach
    void setUp() {
        eventManager = GameEventManager.getInstance();
        eventManager.clearAllListeners();
        scoringService = new ScoringService();
    }

    @AfterEach
    void tearDown() {
        eventManager.clearAllListeners();
    }

    // ========== Initialization Tests ==========

    @Test
    @DisplayName("Should start with score of zero")
    void testInitialScore() {
        assertEquals(0, scoringService.getScore(), "Initial score should be 0");
    }

    @Test
    @DisplayName("Should subscribe to CORRECT_ANSWER_SUBMITTED event on creation")
    void testSubscribesToCorrectAnswerEvent() {
        // Create a new service (setUp already created one, but let's verify)
        int listenerCount = eventManager.getListenerCount(GameEventType.CORRECT_ANSWER_SUBMITTED);

        assertTrue(listenerCount > 0, "Should have at least one listener for CORRECT_ANSWER_SUBMITTED");
    }

    // ========== Score Increment Tests ==========

    @Test
    @DisplayName("Should increment score when CORRECT_ANSWER_SUBMITTED event is published")
    void testScoreIncrementsOnCorrectAnswer() {
        int initialScore = scoringService.getScore();

        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);

        assertEquals(initialScore + 1, scoringService.getScore(),
                "Score should increment by 1 on correct answer");
    }

    @Test
    @DisplayName("Should increment score multiple times for multiple correct answers")
    void testMultipleCorrectAnswers() {
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);

        assertEquals(3, scoringService.getScore(),
                "Score should be 3 after three correct answers");
    }

    @Test
    @DisplayName("Should not change score on INCORRECT_ANSWER_SUBMITTED event")
    void testScoreUnchangedOnIncorrectAnswer() {
        int initialScore = scoringService.getScore();

        eventManager.publish(GameEventType.INCORRECT_ANSWER_SUBMITTED, null);

        assertEquals(initialScore, scoringService.getScore(),
                "Score should not change on incorrect answer");
    }

    @Test
    @DisplayName("Should not change score on unrelated events")
    void testScoreUnchangedOnUnrelatedEvents() {
        int initialScore = scoringService.getScore();

        eventManager.publish(GameEventType.GAME_STARTED, null);
        eventManager.publish(GameEventType.GAME_ENDED, null);
        eventManager.publish(GameEventType.TIMER_TICK, 30);
        eventManager.publish(GameEventType.QUESTION_LOADED, null);

        assertEquals(initialScore, scoringService.getScore(),
                "Score should not change on unrelated events");
    }

    // ========== Score Reset Tests ==========

    @Test
    @DisplayName("Should reset score to zero")
    void testResetScore() {
        // Increment score first
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);

        assertEquals(3, scoringService.getScore());

        // Reset
        scoringService.resetScore();

        assertEquals(0, scoringService.getScore(), "Score should be 0 after reset");
    }

    @Test
    @DisplayName("Should reset score when already at zero")
    void testResetScoreWhenZero() {
        assertEquals(0, scoringService.getScore());

        scoringService.resetScore();

        assertEquals(0, scoringService.getScore(), "Score should remain 0 after reset");
    }

    @Test
    @DisplayName("Should allow score to increment after reset")
    void testScoreIncrementsAfterReset() {
        // Increment score
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);

        // Reset
        scoringService.resetScore();
        assertEquals(0, scoringService.getScore());

        // Increment again
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);

        assertEquals(1, scoringService.getScore(),
                "Score should increment from 0 after reset");
    }

    // ========== Mixed Scenario Tests ==========

    @Test
    @DisplayName("Should handle mixed correct and incorrect answers")
    void testMixedAnswers() {
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);
        eventManager.publish(GameEventType.INCORRECT_ANSWER_SUBMITTED, null);
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);
        eventManager.publish(GameEventType.INCORRECT_ANSWER_SUBMITTED, null);
        eventManager.publish(GameEventType.INCORRECT_ANSWER_SUBMITTED, null);
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);

        assertEquals(3, scoringService.getScore(),
                "Score should be 3 after 3 correct and 3 incorrect answers");
    }

    @Test
    @DisplayName("Should maintain score across multiple game lifecycle events")
    void testScoreAcrossGameLifecycle() {
        // Start game
        eventManager.publish(GameEventType.GAME_STARTED, null);
        assertEquals(0, scoringService.getScore());

        // Player answers
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);
        assertEquals(2, scoringService.getScore());

        // Timer events
        eventManager.publish(GameEventType.TIMER_TICK, 30);
        assertEquals(2, scoringService.getScore());

        // More answers
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);
        assertEquals(3, scoringService.getScore());

        // Game ends
        eventManager.publish(GameEventType.GAME_ENDED, null);
        assertEquals(3, scoringService.getScore(), "Score should persist after game ends");
    }

    // ========== Edge Case Tests ==========

    @Test
    @DisplayName("Should handle event with non-null data")
    void testCorrectAnswerEventWithData() {
        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, 100);

        assertEquals(1, scoringService.getScore(),
                "Score should increment regardless of event data");
    }

    @Test
    @DisplayName("Should handle rapid score increments")
    void testRapidScoreIncrements() {
        for (int i = 0; i < 100; i++) {
            eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);
        }

        assertEquals(100, scoringService.getScore(),
                "Score should handle 100 rapid increments");
    }

    @Test
    @DisplayName("Should support multiple ScoringService instances independently")
    void testMultipleInstances() {
        ScoringService service1 = new ScoringService();
        ScoringService service2 = new ScoringService();

        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);

        // Both services listen to the same event, so both should increment
        assertEquals(1, service1.getScore(), "Service 1 should have score 1");
        assertEquals(1, service2.getScore(), "Service 2 should have score 1");

        service1.resetScore();

        assertEquals(0, service1.getScore(), "Service 1 should be reset to 0");
        assertEquals(1, service2.getScore(), "Service 2 should still be 1");
    }

    // ========== Event Listener Interface Tests ==========

    @Test
    @DisplayName("Should correctly implement GameEventListener interface")
    void testGameEventListenerInterface() {
        // Verify the service can handle events through the interface
        scoringService.onGameEvent(GameEventType.CORRECT_ANSWER_SUBMITTED, null);

        assertEquals(1, scoringService.getScore(),
                "Direct event call should increment score");
    }

    @Test
    @DisplayName("Should handle null event type gracefully")
    void testNullEventType() {
        int initialScore = scoringService.getScore();

        // This should not crash
        assertDoesNotThrow(() -> scoringService.onGameEvent(null, null),
                "Should handle null event type without throwing");

        assertEquals(initialScore, scoringService.getScore(),
                "Score should not change on null event");
    }
}
