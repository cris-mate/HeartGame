package com.heartgame.controller;

import com.heartgame.model.Question;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.service.HeartGameAPIService;
import com.heartgame.service.ScoringService;
import com.heartgame.service.GameTimer;
import com.heartgame.view.GameGUI;
import com.heartgame.view.LoginGUI;
import com.heartgame.event.GameEventType;
import com.heartgame.event.GameEventListener;
import com.heartgame.event.GameEventManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for handling the main game logic
 * Manages game flow, timer, scoring, and user interactions
 */
public class GameController implements GameEventListener {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final GameGUI gameView;
    private final HeartGameAPIService apiService;
    private final ScoringService scoringService;
    private final GameTimer gameTimer;
    private final User user;
    private Question currentQuestion;

    /**
     * Constructs a new GameController
     * @param gameView The game view it controls
     * @param user     The logged-in user
     */
    public GameController(GameGUI gameView, User user) {
        this.gameView = gameView;
        this.user = user;
        this.apiService = new HeartGameAPIService();
        this.scoringService = new ScoringService();
        this.gameTimer = new GameTimer(gameView);

        initController();

        GameEventManager.getInstance().subscribe(GameEventType.TIMER_EXPIRED, this);
        logger.info("GameController initialized for user '{}' - waiting for manual start", user.getUsername());
    }

    /**
     * Initialises the controller by attaching action listeners to the view's buttons
     */
    private void initController() {
        for (int i = 0; i < 10; i++) {
            gameView.getButton(i).addActionListener(this::handleAnswer);
        }
    }

    /**
     * Starts the game - loads first question and publishes GAME_STARTED event
     * Called when Start button is clicked
     */
    public void startGame() {
        logger.info("Game started for user '{}'", user.getUsername());

        // Load first question
        loadNextGame();

        // Publish GAME_STARTED event - this triggers the timer to start
        GameEventManager.getInstance().publish(GameEventType.GAME_STARTED, user);
    }

    /**
     * Handles game events from the event system
     * Responds to TIMER_EXPIRED by ending the game
     * @param eventType The type of event that occurred
     * @param data      Optional data associated with the event
     */
    @Override
    public void onGameEvent(GameEventType eventType, Object data) {
        if (eventType == GameEventType.TIMER_EXPIRED) {
            handleGameOver();
        }
    }

    /**
     * Handles game over when time runs out
     * Displays final score and further options
     */
    private void handleGameOver() {
        int finalScore = scoringService.getScore();

        logger.info("Game over for user '{}' - Final score: {}", user.getUsername(), finalScore);

        // Disable answer buttons
        gameView.disableAnswerButtons();

        // Show game over message in info area
        gameView.updateInfo(String.format("TIME'S UP! Final Score: %d", finalScore));

        // Show game over dialog after brief delay to let message show
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            showGameOverDialog(finalScore);
        });
    }

    /**
     * Shows the game over dialog with final score and options
     * @param finalScore The player's final score
     */
    private void showGameOverDialog(int finalScore) {
        String message = String.format(
                "⏱️ Time's Up!\n\n" +
                        "Player: %s\n" +
                        "Final Score: %d correct answer%s\n\n" +
                        "Would you like to play again?",
                user.getUsername(),
                finalScore,
                finalScore == 1 ? "" : "s"
        );

        int choice = JOptionPane.showConfirmDialog(
                gameView,
                message,
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            // Restart game
            logger.info("Player '{}' chose to play again", user.getUsername());
            gameView.dispose();
            SwingUtilities.invokeLater(() -> new GameGUI().setVisible(true));
        } else {
            // Return to login
            logger.info("Player '{}' returning to login screen", user.getUsername());
            gameView.dispose();
            SwingUtilities.invokeLater(() -> new LoginGUI());
        }
    }

    /**
     * Loads the next game question from the API service and updates the view
     * Publishes QUESTION_LOADED event on success or API_ERROR event on failure
     */
    public void loadNextGame() {
        try {
            currentQuestion = apiService.getNewQuestion();
            if (currentQuestion != null) {
                gameView.updateQuestion(currentQuestion.getImage(), scoringService.getScore());
                GameEventManager.getInstance().publish(GameEventType.QUESTION_LOADED, currentQuestion);
                logger.debug("Question loaded successfully");
            } else {
                handleApiError("Failed to load a game question.");
            }
        } catch (IOException e) {
            logger.error("Failed to fetch a new question from the API: '{}'", e.getMessage());
            handleApiError("Could not fetch a new game. Please check your connection.");
        }
    }

    /**
     * Handles API errors by publishing an event and showing error to user
     * @param errorMessage The error message to display
     */
    private void handleApiError(String errorMessage) {
        GameEventManager.getInstance().publish(GameEventType.API_ERROR, errorMessage);
        gameView.showError(errorMessage);
    }

    /**
     * Handles the user's answer submission
     * Checks the solution and publishes appropriate event
     * Immediately loads next question to continue gameplay
     * @param e The ActionEvent triggered by the user's button click
     */
    public void handleAnswer( ActionEvent e ) {
        int solution = Integer.parseInt(e.getActionCommand());
        if (solution == currentQuestion.getSolution()) {
            logger.debug("Correct answer submitted for question by user '{}'.", user.getUsername());
            GameEventManager.getInstance().publish(
                    GameEventType.CORRECT_ANSWER_SUBMITTED,
                    scoringService.getScore() + 1
            );
            loadNextGame();
        } else {
            logger.debug("Incorrect answer submitted for question  by user '{}'.", user.getUsername());
            GameEventManager.getInstance().publish(
                    GameEventType.INCORRECT_ANSWER_SUBMITTED,
                    scoringService.getScore()
            );
        }
    }
}