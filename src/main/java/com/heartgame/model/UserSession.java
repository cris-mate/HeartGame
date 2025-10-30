package com.heartgame.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.Duration;
import java.util.Optional;

/**
 * Manages the current user session as a Singleton
 * Provides centralized access to the logged-in user's information throughout the application
 * Includes session validation, timestamp tracking and utility methods
 */
public final class UserSession {

    private static final Logger logger = LoggerFactory.getLogger(UserSession.class);
    private static UserSession instance;

    private User currentUser;
    private Instant loginTimestamp;
    private Instant lastActivityTimestamp;

    private static final long SESSION_TIMEOUT_MINUTES = 60;
    private static final boolean ENABLE_TIMEOUT = false;
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
     * Records login timestamp and initializes activity tracking
     * @param user The user to log in
     */
    public void login(User user) {
        if (user == null) {
            logger.warn("Attempted to login with null user");
            return;
        }

        this.currentUser = user;
        this.loginTimestamp = Instant.now();
        this.lastActivityTimestamp = Instant.now();

        logger.info("User '{}' logged in at {}", user.getUsername(), loginTimestamp);
    }

    /**
     * @return The currently logged-in user, or null if no user is logged in
     */
    public User getCurrentUser() {
        // Update last activity timestamp when user is accessed
        if (currentUser != null) {
            updateActivity();
        }
        return currentUser;
    }

    /**
     * @return True if a user is currently logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null && !isSessionExpired();
    }

    /**
     * Checks if the current session has expired due to inactivity
     * @return True if session has expired, false otherwise
     */
    public boolean isSessionExpired() {
        if (!ENABLE_TIMEOUT || currentUser == null || lastActivityTimestamp == null) {
            return false;
        }

        Duration inactivityDuration = Duration.between(lastActivityTimestamp, Instant.now());
        boolean expired = inactivityDuration.toMinutes() > SESSION_TIMEOUT_MINUTES;

        if (expired) {
            logger.warn("Session expired for user '{}' after {} minutes of inactivity",
                    currentUser.getUsername(), inactivityDuration.toMinutes());
        }

        return expired;
    }

    /**
     * Updates the last activity timestamp
     * Called automatically when getCurrentUser() is accessed
     */
    public void updateActivity() {
        if (currentUser != null) {
            this.lastActivityTimestamp = Instant.now();
            logger.trace("Activity updated for user '{}'", currentUser.getUsername());
        }
    }

    /**
     * Logs out the current user by clearing the session
     * Also clears all session metadata
     */
    public void logout() {
        if (currentUser != null) {
            logger.info("User '{}' logged out. Session duration: {} minutes",
                    currentUser.getUsername(), getSessionDurationMinutes());
        }

        this.currentUser = null;
        this.loginTimestamp = null;
        this.lastActivityTimestamp = null;
    }

    /**
     * @return The username of the currently logged-in user, or null if no user is logged in
     */
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    /**
     * Gets the login timestamp for the current session
     * @return Optional containing the login timestamp, or empty if not logged in
     */
    public Optional<Instant> getLoginTimestamp() {
        return Optional.ofNullable(loginTimestamp);
    }

    /**
     * Gets the last activity timestamp for the current session
     * @return Optional containing the last activity timestamp, or empty if not logged in
     */
    public Optional<Instant> getLastActivityTimestamp() {
        return Optional.ofNullable(lastActivityTimestamp);
    }

    /**
     * Calculates the duration of the current session in minutes
     * @return Session duration in minutes, or 0 if not logged in
     */
    public long getSessionDurationMinutes() {
        if (loginTimestamp == null) {
            return 0;
        }
        return Duration.between(loginTimestamp, Instant.now()).toMinutes();
    }

    /**
     * Calculates the time since last activity in minutes
     * @return Minutes since last activity, or 0 if not logged in
     */
    public long getInactivityDurationMinutes() {
        if (lastActivityTimestamp == null) {
            return 0;
        }
        return Duration.between(lastActivityTimestamp, Instant.now()).toMinutes();
    }

    /**
     * Validates the current session
     * Checks if user is logged in and session hasn't expired
     * @return True if session is valid, false otherwise
     */
    public boolean validateSession() {
        if (currentUser == null) {
            logger.debug("Session validation failed: No user logged in");
            return false;
        }

        if (isSessionExpired()) {
            logger.warn("Session validation failed: Session expired for user '{}'", currentUser.getUsername());
            logout(); // Auto-logout on expired session
            return false;
        }

        logger.trace("Session validated for user '{}'", currentUser.getUsername());
        return true;
    }

    /**
     * Gets session information as a formatted string
     * Useful for debugging and logging
     * @return Session information string
     */
    public String getSessionInfo() {
        if (currentUser == null) {
            return "No active session";
        }

        return String.format("User: %s, Login: %s, Duration: %d min, Last Activity: %d min ago",
                currentUser.getUsername(),
                loginTimestamp,
                getSessionDurationMinutes(),
                getInactivityDurationMinutes());
    }

    /**
     * Checks if the current user is authenticated via OAuth
     * @return True if OAuth user, false otherwise or if not logged in
     */
    public boolean isOAuthUser() {
        return currentUser != null && currentUser.isOAuthUser();
    }

    /**
     * Gets the authentication provider for the current user
     * @return OAuth provider name (e.g., "google") or "password", or null if not logged in
     */
    public String getAuthProvider() {
        return currentUser != null ? currentUser.getOauthProvider() : null;
    }
}