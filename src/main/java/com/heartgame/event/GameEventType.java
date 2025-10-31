package com.heartgame.event;

/**
 * Defines the types of events that can be published within the game
 * Includes navigation events for decoupled screen transitions
 */
public enum GameEventType {

    /**
     * Published when a correct answer is submitted by the player
     * Data: Integer (new score after correct answer)
     */
    CORRECT_ANSWER_SUBMITTED,

    /**
     * Published when an incorrect answer is submitted by the player
     * Data: Integer (current score, unchanged)
     */
    INCORRECT_ANSWER_SUBMITTED,

    /**
     * Published when the game starts (after user logs in and game initializes)
     * Data: User (the player who started the game)
     */
    GAME_STARTED,

    /**
     * Published when the game ends (timer expired or user quit)
     * Data: Integer (final score)
     */
    GAME_ENDED,

    /**
     * Published when a player successfully logs into the system
     * Data: User (the authenticated user)
     */
    PLAYER_LOGGED_IN,

    /**
     * Published when a user logs out of the system
     * Data: User (the user who logged out) or null
     */
    PLAYER_LOGGED_OUT,

    /**
     * Published when a new question has been loaded and displayed
     * Data: Question (the loaded question object)
     */
    QUESTION_LOADED,

    /**
     * Published when an API error occurs (network failure, parsing error, etc.)
     * Data: String (error message)
     */
    API_ERROR,

    /**
     * Published every second as the game timer counts down
     * Data: Integer (remaining seconds)
     */
    TIMER_TICK,

    /**
     * Published when the game timer reaches zero
     * Data: null
     */
    TIMER_EXPIRED,

    /**
     * Published when the game is paused
     * Data: Integer (remaining seconds when paused)
     */
    GAME_PAUSED,

    /**
     * Published when the game is resumed from pause
     * Data: Integer (remaining seconds when resumed)
     */
    GAME_RESUMED,

    // ========== Navigation Events ==========

    /**
     * Published to request navigation to the login screen
     * Data: null
     */
    NAVIGATE_TO_LOGIN,

    /**
     * Published to request navigation to the home screen
     * Data: null
     */
    NAVIGATE_TO_HOME,

    /**
     * Published to request navigation to the game screen
     * Data: null
     */
    NAVIGATE_TO_GAME,

    /**
     * Published to request navigation to the leaderboard screen
     * Data: null
     */
    NAVIGATE_TO_LEADERBOARD,

    /**
     * Published to request navigation to the registration screen
     * Data: null
     */
    NAVIGATE_TO_REGISTER
}
