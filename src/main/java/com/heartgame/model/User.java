package com.heartgame.model;

/**
 * Represents a player in the game
 */
public class User {

    private final String username;

    /**
     * Constructs a new User
     * @param username The user's name
     */
    public User(String username) {
        this.username = username;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }
}
