package com.heartgame.controller;

import com.heartgame.model.GameSession;
import com.heartgame.model.User;
import com.heartgame.persistence.GameSessionDAO;
import com.heartgame.view.HomeGUI;
import com.heartgame.view.LeaderboardGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;

/**
 * Controller for handling leaderboard actions
 * Manages data retrieval and user interactions
 */
public class LeaderboardController {

    private static final Logger logger = LoggerFactory.getLogger(LeaderboardController.class);
    private final LeaderboardGUI leaderboardView;
    private final User currentUser;
    private final GameSessionDAO gameSessionDAO;

    /**
     * Constructs a new LeaderboardController
     * @param leaderboardView The leaderboard view it controls
     * @param currentUser The currently logged-in user
     */
    public LeaderboardController(LeaderboardGUI leaderboardView, User currentUser) {
        this.leaderboardView = leaderboardView;
        this.currentUser = currentUser;
        this.gameSessionDAO = new GameSessionDAO();

        initController();
        loadLeaderboard();
    }

    /**
     * Initializes the controller by adding action listeners
     */
    private void initController() {
        leaderboardView.getRefreshButton().addActionListener(e -> loadLeaderboard());
        leaderboardView.getBackButton().addActionListener(e -> handleBack());

        logger.debug("LeaderboardController initialized for user '{}'", currentUser.getUsername());
    }

    /**
     * Loads leaderboard data from database and updates the view
     */
    private void loadLeaderboard() {
        logger.info("Loading leaderboard data");

        // Show loading state (optional enhancement)
        leaderboardView.getRefreshButton().setEnabled(false);
        leaderboardView.getRefreshButton().setText("Loading...");

        // Load data in background to avoid UI freezing
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<GameSession> topScores;
            private int userHighScore;
            private int userTotalGames;

            @Override
            protected Void doInBackground() {
                // Fetch top 10 scores
                topScores = gameSessionDAO.getTopTenScores();

                // Fetch user-specific stats
                userHighScore = gameSessionDAO.getUserHighScore(currentUser.getId());
                userTotalGames = gameSessionDAO.getUserGameSessions(currentUser.getId()).size();

                return null;
            }

            @Override
            protected void done() {
                try {
                    // Update view with data
                    if (topScores.isEmpty()) {
                        logger.info("No scores found in database");
                    } else {
                        leaderboardView.updateLeaderboard(topScores);
                        leaderboardView.updateUserStats(userHighScore, userTotalGames);
                        logger.info("Leaderboard loaded: {} entries", topScores.size());
                    }
                } catch (Exception e) {
                    logger.error("Error updating leaderboard view", e);
                    JOptionPane.showMessageDialog(
                            leaderboardView,
                            "Failed to load leaderboard. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    // Restore button state
                    leaderboardView.getRefreshButton().setEnabled(true);
                    leaderboardView.getRefreshButton().setText("Refresh");
                }
            }
        };

        worker.execute();
    }

    /**
     * Handles the back button - returns to home screen
     */
    private void handleBack() {
        logger.debug("User '{}' navigating back to home", currentUser.getUsername());

        // Close leaderboard window
        leaderboardView.dispose();

        // Open home window
        SwingUtilities.invokeLater(() -> {
            HomeGUI homeGUI = new HomeGUI(currentUser);
            homeGUI.setVisible(true);
        });
    }
}