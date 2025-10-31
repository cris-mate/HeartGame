package com.heartgame.controller;

import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.view.HomeGUI;
import com.heartgame.event.GameEventType;
import com.heartgame.event.GameEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Controller for handling home screen actions
 * Uses event-driven navigation for screen transitions (low coupling)
 */
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private final HomeGUI homeView;

    /**
     * Constructs a new HomeController
     * @param homeView The home view it controls
     */
    public HomeController(HomeGUI homeView) {
        this.homeView = homeView;
        initController();
    }

    /**
     * Initializes the controller by adding action listeners to buttons
     */
    private void initController() {
        homeView.getStartGameButton().addActionListener(e -> handleStartGame());
        homeView.getLeaderboardButton().addActionListener(e -> handleLeaderboard());
        homeView.getLogoutButton().addActionListener(e -> handleLogout());
        homeView.getExitButton().addActionListener(e -> handleExit());

        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            logger.debug("HomeController initialized for user '{}'", currentUser.getUsername());
        }
    }

    /**
     * Handles the Start Game button
     * Publishes navigation event instead of directly creating GUI
     */
    private void handleStartGame() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            logger.error("No authenticated user found");
            return;
        }
        logger.info("User '{}' starting new game from home screen", currentUser.getUsername());

        // Publish navigation event (event-driven approach)
        GameEventManager.getInstance().publish(GameEventType.NAVIGATE_TO_GAME, null);
    }

    /**
     * Handles the Leaderboard button
     * Publishes navigation event instead of directly creating GUI
     */
    private void handleLeaderboard() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            logger.error("No authenticated user found");
            return;
        }
        logger.info("User '{}' viewing leaderboard from home screen", currentUser.getUsername());

        // Publish navigation event (event-driven approach)
        GameEventManager.getInstance().publish(GameEventType.NAVIGATE_TO_LEADERBOARD, null);
    }

    /**
     * Handles the Logout button
     * Logs out user and publishes navigation event to login screen
     */
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                homeView,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            User currentUser = UserSession.getInstance().getCurrentUser();
            if (currentUser != null) {
                logger.info("User '{}' logging out from home screen", currentUser.getUsername());
                // Publish logout event
                GameEventManager.getInstance().publish(GameEventType.PLAYER_LOGGED_OUT, currentUser);
            }

            // Clear user session
            UserSession.getInstance().logout();

            // Publish navigation event to login screen (event-driven approach)
            GameEventManager.getInstance().publish(GameEventType.NAVIGATE_TO_LOGIN, null);
        }
    }

    /**
     * Handles the Exit button
     * Closes the application completely
     */
    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(
                homeView,
                "Are you sure you want to exit the application?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            User currentUser = UserSession.getInstance().getCurrentUser();
            if (currentUser != null) {
                logger.info("User '{}' exiting application from home screen", currentUser.getUsername());
                // Publish logout event before exiting
                GameEventManager.getInstance().publish(GameEventType.PLAYER_LOGGED_OUT, currentUser);
            }

            UserSession.getInstance().logout();

            // Exit application
            System.exit(0);
        }
    }
}