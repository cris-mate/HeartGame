package com.heartgame.controller;

import com.heartgame.model.GameSession;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.persistence.GameSessionDAO;
import com.heartgame.view.LeaderboardGUI;
import com.heartgame.event.GameEventManager;
import com.heartgame.event.GameEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;

/**
 * Controller for handling leaderboard actions
 * Uses event-driven navigation for screen transitions (low coupling)
 * Manages data retrieval and user interactions
 */
public class LeaderboardController {

    private static final Logger logger = LoggerFactory.getLogger(LeaderboardController.class);
    private final LeaderboardGUI leaderboardView;
    private final GameSessionDAO gameSessionDAO;

    /**
     * Constructs a new LeaderboardController
     * Uses UserSession to access the current authenticated user
     * @param leaderboardView The leaderboard view it controls
     */
    public LeaderboardController(LeaderboardGUI leaderboardView) {
        this.leaderboardView = leaderboardView;
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

        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            logger.debug("LeaderboardController initialized for user '{}'", currentUser.getUsername());
        }
    }

    /**
     * Loads leaderboard data from database and updates the view
     */
    private void loadLeaderboard() {
        logger.info("Loading leaderboard data");

        // Show loading state
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
                User currentUser = UserSession.getInstance().getCurrentUser();
                if (currentUser != null) {
                    userHighScore = gameSessionDAO.getUserHighScore(currentUser.getId());
                    userTotalGames = gameSessionDAO.getUserGameSessions(currentUser.getId()).size();
                }

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
     * Handles the back button - returns to home screen using navigation event
     */
    private void handleBack() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            logger.debug("User '{}' navigating back to home", currentUser.getUsername());
        }

        // Publish navigation event (event-driven approach)
        GameEventManager.getInstance().publish(GameEventType.NAVIGATE_TO_HOME, null);
    }
}