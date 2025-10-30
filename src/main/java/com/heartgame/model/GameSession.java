package com.heartgame.model;

import java.time.Instant;

/**
 * Represents a single game session with score and metadata
 * Used for leaderboard and game history tracking
 */
public class GameSession {

    private int id;
    private int userId;
    private String username; // Denormalized for convenience
    private Instant startTime;
    private Instant endTime;
    private int finalScore;
    private int questionsAnswered;

    /**
     * Constructor for creating a new game session (before database save)
     * @param userId The ID of the user who played
     * @param startTime When the game started
     * @param endTime When the game ended
     * @param finalScore The final score achieved
     * @param questionsAnswered Total questions answered
     */
    public GameSession(int userId, Instant startTime, Instant endTime, int finalScore, int questionsAnswered) {
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.finalScore = finalScore;
        this.questionsAnswered = questionsAnswered;
    }

    /**
     * Constructor for loading from database (with ID and username)
     * @param id The database ID
     * @param userId The user ID
     * @param username The username (from join)
     * @param startTime When the game started
     * @param endTime When the game ended
     * @param finalScore The final score achieved
     * @param questionsAnswered Total questions answered
     */
    public GameSession(int id, int userId, String username, Instant startTime, Instant endTime,
                       int finalScore, int questionsAnswered) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.startTime = startTime;
        this.endTime = endTime;
        this.finalScore = finalScore;
        this.questionsAnswered = questionsAnswered;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public int getQuestionsAnswered() {
        return questionsAnswered;
    }

    public void setQuestionsAnswered(int questionsAnswered) {
        this.questionsAnswered = questionsAnswered;
    }

    /**
     * Calculates the game duration in seconds
     * @return Duration in seconds, or 0 if times not set
     */
    public long getDurationSeconds() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return endTime.getEpochSecond() - startTime.getEpochSecond();
    }

    @Override
    public String toString() {
        return "GameSession{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", finalScore=" + finalScore +
                ", questionsAnswered=" + questionsAnswered +
                ", duration=" + getDurationSeconds() + "s" +
                '}';
    }
}
