package com.heartgame.model;

/**
 * Manages the current user session as a Singleton
 * Provides centralized access to the logged-in user's information throughout the application
 */
public final class UserSession {

    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    /**
     * @return The single instance of UserSession
     */
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Logs in a user by storing their information in the session
     * @param user The user to log in
     */
    public void login(User user) {
        this.currentUser = user;
    }

    /**
     * @return The currently logged-in user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * @return True if a user is currently logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Logs out the current user by clearing the session
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * @return The username of the currently logged-in user, or null if no user is logged in
     */
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
}