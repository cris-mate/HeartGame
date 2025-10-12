package com.heartgame.service;

import com.heartgame.event.GameEventType;
import com.heartgame.event.GameEventListener;
import com.heartgame.event.GameEventManager;

/**
 * Manages the player's score by listening to game events
 */
public class ScoringService implements GameEventListener {

    private int score = 0;

    public ScoringService() {
        // Subscribe to the event that affects the score
        GameEventManager.getInstance().subscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, this);
    }

    /**
     * Handles game events to update the score
     * Increments the score when a CORRECT_ANSWER_SUBMITTED event is received
     * @param eventType The type of event that occurred
     * @param data      Optional data associated with the event
     */
    @Override
    public void onGameEvent(GameEventType eventType, Object data) {
        if (eventType == GameEventType.CORRECT_ANSWER_SUBMITTED) {
            score++;
        }
    }

    /**
     * @return The current score
     */
    public int getScore() { return score; }

    /**
     * Resets the score to zero
     */
    public void resetScore() { score = 0; }
}
