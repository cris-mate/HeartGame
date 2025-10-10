package service;

import event.GameEventType;
import event.GameEventListener;
import event.GameEventManager;

/**
 * Manages the player's score by listening to game events
 */
public class ScoringService implements GameEventListener {

    private int score = 0;

    public ScoringService() {
        // Subscribe to the event that affects the score
        GameEventManager.getInstance().subscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, this);
    }

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
