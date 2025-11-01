package com.heartgame.controller;

import com.heartgame.event.GameEventListener;
import com.heartgame.event.GameEventManager;
import com.heartgame.event.GameEventType;
import com.heartgame.view.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;

/**
 * Centralized navigation controller that handles all screen transitions
 * Implements event-driven navigation
 * Listens to navigation events and manages window lifecycle
 */
public class NavigationController implements GameEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NavigationController.class);
    private static NavigationController instance;
    private JFrame currentWindow;

    private NavigationController() {
        // Subscribe to all navigation events
        GameEventManager eventManager = GameEventManager.getInstance();
        eventManager.subscribe(GameEventType.NAVIGATE_TO_LOGIN, this);
        eventManager.subscribe(GameEventType.NAVIGATE_TO_HOME, this);
        eventManager.subscribe(GameEventType.NAVIGATE_TO_GAME, this);
        eventManager.subscribe(GameEventType.NAVIGATE_TO_LEADERBOARD, this);
        eventManager.subscribe(GameEventType.NAVIGATE_TO_REGISTER, this);

        logger.info("NavigationController initialized and subscribed to navigation events");
    }

    /**
     * Gets the singleton instance of NavigationController
     * @return The NavigationController instance
     */
    public static synchronized NavigationController getInstance() {
        if (instance == null) {
            instance = new NavigationController();
        }
        return instance;
    }

    /**
     * Handles navigation events
     * @param eventType The navigation event type
     * @param data Optional navigation data (usually null)
     */
    @Override
    public void onGameEvent(GameEventType eventType, Object data) {
        SwingUtilities.invokeLater(() -> {
            try {
                switch (eventType) {
                    case NAVIGATE_TO_LOGIN:
                        navigateToLogin();
                        break;
                    case NAVIGATE_TO_HOME:
                        navigateToHome();
                        break;
                    case NAVIGATE_TO_GAME:
                        navigateToGame();
                        break;
                    case NAVIGATE_TO_LEADERBOARD:
                        navigateToLeaderboard();
                        break;
                    case NAVIGATE_TO_REGISTER:
                        navigateToRegister();
                        break;
                    default:
                        logger.warn("Unhandled navigation event: {}", eventType);
                }
            } catch (Exception e) {
                logger.error("Navigation error for event {}", eventType, e);
                JOptionPane.showMessageDialog(
                        currentWindow,
                        "Navigation error: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    /**
     * Navigates to the login screen
     */
    private void navigateToLogin() {
        logger.info("Navigating to Login screen");
        closeCurrentWindow();
        currentWindow = new LoginGUI();
        currentWindow.setVisible(true);
    }

    /**
     * Navigates to the home screen
     */
    private void navigateToHome() {
        logger.info("Navigating to Home screen");
        closeCurrentWindow();
        currentWindow = new HomeGUI();
        currentWindow.setVisible(true);
    }

    /**
     * Navigates to the game screen
     */
    private void navigateToGame() {
        logger.info("Navigating to Game screen");
        closeCurrentWindow();
        currentWindow = new GameGUI();
        currentWindow.setVisible(true);
    }

    /**
     * Navigates to the leaderboard screen
     */
    private void navigateToLeaderboard() {
        logger.info("Navigating to Leaderboard screen");
        closeCurrentWindow();
        currentWindow = new LeaderboardGUI();
        currentWindow.setVisible(true);
    }

    /**
     * Navigates to the registration screen
     */
    private void navigateToRegister() {
        logger.info("Navigating to Register screen");
        closeCurrentWindow();
        currentWindow = new RegisterGUI();
        currentWindow.setVisible(true);
    }

    /**
     * Closes the current window if one exists
     */
    private void closeCurrentWindow() {
        if (currentWindow != null) {
            logger.debug("Closing current window: {}", currentWindow.getClass().getSimpleName());
            currentWindow.dispose();
            currentWindow = null;
        }
    }

    /**
     * Gets the currently displayed window
     * @return The current window, or null if none
     */
    public JFrame getCurrentWindow() {
        return currentWindow;
    }

    /**
     * Sets the current window (used for initial application startup)
     * @param window The window to set as current
     */
    public void setCurrentWindow(JFrame window) {
        this.currentWindow = window;
    }
}
