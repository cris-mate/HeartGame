package service;

/**
 * Manages the player's score.
 */
public class ScoringService {
    private int score = 0;

    /**
     * Increases the score by one.
     */
    public void increaseScore() { score++; }

    /**
     * @return The current score.
     */
    public int getScore() { return score; }

    /**
     * Resets the score to zero.
     */
    public void resetScore() { score = 0; }
}
