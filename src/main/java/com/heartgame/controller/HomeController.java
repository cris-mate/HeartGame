package com.heartgame.controller;

import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.view.HomeGUI;
import com.heartgame.view.GameGUI;
import com.heartgame.view.LeaderboardGUI;
import com.heartgame.view.LoginGUI;
import com.heartgame.event.GameEventType;
import com.heartgame.event.GameEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Controller for handling home screen actions
 * Manages navigation between home, game, leaderboard and login screens
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
     * Closes home view and opens game view
     */
    private void handleStartGame() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            logger.error("No authenticated user found");
            return;
        }
        logger.info("User '{}' starting new game from home screen", currentUser.getUsername());

        // Close home window
        homeView.dispose();

        // Open game window
        SwingUtilities.invokeLater(() -> {
            GameGUI gameGUI = new GameGUI();
            gameGUI.setVisible(true);
        });
    }

    /**
     * Handles the Leaderboard button
     * Closes home view and opens leaderboard view
     */
    private void handleLeaderboard() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            logger.error("No authenticated user found");
            return;
        }
        logger.info("User '{}' viewing leaderboard from home screen", currentUser.getUsername());

        homeView.dispose();

        // Open leaderboard window
        SwingUtilities.invokeLater(() -> {
            LeaderboardGUI leaderboardGUI = new LeaderboardGUI();
            leaderboardGUI.setVisible(true);
        });
    }

    /**
     * Handles the Logout button
     * Logs out user, closes home view, and returns to login screen
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

            homeView.dispose();

            // Open login window
            SwingUtilities.invokeLater(() -> {
                LoginGUI loginGUI = new LoginGUI();
                loginGUI.setVisible(true);
            });
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