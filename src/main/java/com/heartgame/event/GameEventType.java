package com.heartgame.event;

/**
 * Defines the types of events that can be published within the game
 */
public enum GameEventType {
    CORRECT_ANSWER_SUBMITTED,
    INCORRECT_ANSWER_SUBMITTED,
    GAME_STARTED,
    QUESTION_LOADED,
    API_ERROR,
    PLAYER_LOGGED_IN,
    PLAYER_LOGGED_OUT,
    TIMER_EXPIRED
}
