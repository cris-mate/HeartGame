package com.heartgame.model;

/**
 * Manages the current state of the game, including the current question and score
 */
public class GameState {

    private int score;
    private int questionNumber;

    /**
     * Constructs a new GameState, initializing score and question number to zero
     */
    public  GameState() {
        this.score = 0;
        this.questionNumber = 0;
    }

    /**
     * @return the current score
     */
    public int getScore() { return score; }

    /**
     * Sets the current score
     * @param score the new score
     */
    public void setScore(int score) { this.score = score; }

    /**
     * @return the current question number
     */
    public int getQuestionNumber() { return questionNumber; }

    /**
     * Increments the question number
     */
    public void incrementQuestionNumber() { this.questionNumber++; }
}
