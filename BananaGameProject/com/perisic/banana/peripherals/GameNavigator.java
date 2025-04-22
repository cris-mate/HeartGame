package com.perisic.banana.peripherals;

import com.perisic.banana.engine.DBManager;
import com.perisic.banana.engine.SessionManager;

import javax.swing.SwingUtilities;

/**
 * The GameNavigator class is responsible for controlling the flow between
 * various GUI screens in the Banana Equation Game application.
 * It acts as a central navigation controller and ensures that the correct GUI screen
 * is displayed based on user interaction, while sharing session and database context
 * across different screens.
 */
public class GameNavigator {
    private final DBManager dbManager;
    private final SessionManager session;
    private final String currentPlayer;

    /**
     * Constructs a GameNavigator instance for managing GUI transitions within the Banana Equation Game.
     * It initializes the navigator with the active player, a database manager and a session manager.
     *
     * @param currentPlayer the username of the currently logged-in player
     * @param dbManager     the database manager responsible for data access and storage
     * @param session       the session manager that handles session-related data
     */
    public GameNavigator(String currentPlayer, DBManager dbManager, SessionManager session) {
        this.currentPlayer = currentPlayer;
        this.dbManager = dbManager;
        this.session = session;
    }

    /**
     * Opens the Home screen GUI for the currently logged-in player.
     */
    public void openHome() {
        SwingUtilities.invokeLater(() -> {
            HomeGUI homeGUI = new HomeGUI(currentPlayer, dbManager, session);
            homeGUI.setVisible(true);
        });
    }

    /**
     * Opens the main game interface where the user plays the Banana Equation Game.
     */
    public void openGame() {
        SwingUtilities.invokeLater(() -> {
            GameGUI gameGUI = new GameGUI(currentPlayer, dbManager, session);
            gameGUI.setVisible(true);
        });


    }

    /**
     * Opens the Hall of Fame screen showing top-scoring players.
     */
    public void openHallOfFame() {
        SwingUtilities.invokeLater(() -> {
            HallOfFame hallOfFame = new  HallOfFame(currentPlayer, dbManager, session);
            hallOfFame.setVisible(true);
        });
    }

    /**
     * Opens the Milestones screen displaying the player's achievements.
     */
    public void openMilestones() {
        SwingUtilities.invokeLater(() -> {
            Milestones milestones = new Milestones(currentPlayer, dbManager, session);
            milestones.setVisible(true);
        });
    }

    /**
     * Opens the Update Account screen where the player can modify their email
     * and/or password. The screen is initialized with the current user's context
     * and uses the existing database manager for data persistence.
     */
    public void openUpdateAccount() {
        SwingUtilities.invokeLater(() -> {
            UpdateAccount updateAccount = new UpdateAccount(currentPlayer, dbManager, session);
            updateAccount.setVisible(true);
        });
    }

    /**
     * Retrieves the username of the currently logged-in player.
     *
     * @return the current player's username
     */
    public String getCurrentPlayer() {
        return currentPlayer;
    }
}