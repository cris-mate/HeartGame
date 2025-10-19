package com.heartgame.controller;

import com.heartgame.model.Question;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.service.HeartGameAPIService;
import com.heartgame.service.ScoringService;
import com.heartgame.view.GameGUI;
import com.heartgame.event.GameEventType;
import com.heartgame.event.GameEventManager;

import java.awt.event.ActionEvent;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for handling the main game logic
 * Publishes events based on user actions
 * Uses UserSession to access current user information
 */
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final GameGUI gameView;
    private final HeartGameAPIService apiService;
    private final ScoringService scoringService;
    private Question currentQuestion;

    /**
     * Constructs a new GameController
     * It initializes the services,links the controller to the view,
     * loads the first question and publishes the GAME_STARTED event.
     * @param gameView The game view it controls
     * @param user     The logged-in user
     */
    public GameController(GameGUI gameView, User user) {
        this.gameView = gameView;
        this.apiService = new HeartGameAPIService();
        this.scoringService = new ScoringService();
        initController();
        loadNextGame();

        // Get user from session or use passed parameter
        User sessionUser = UserSession.getInstance().getCurrentUser();
        User activeUser = sessionUser != null ? sessionUser : user;
        if (activeUser == null) {
            throw new IllegalStateException("No authenticated user");
        }
        logger.info("Game started for user '{}'.", activeUser.getUsername());
        GameEventManager.getInstance().publish(GameEventType.GAME_STARTED, activeUser);
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
     * It checks the solution and publishes either a CORRECT_ANSWER_SUBMITTED
     * or INCORRECT_ANSWER_SUBMITTED event with the relevant score data
     * @param e The ActionEvent triggered by the user's button click
     */
    public void handleAnswer( ActionEvent e ) {
        int solution = Integer.parseInt(e.getActionCommand());
        User currentUser = UserSession.getInstance().getCurrentUser();
        String username = currentUser != null ? currentUser.getUsername() : "Unknown";
        if (solution == currentQuestion.getSolution()) {
            logger.debug("Correct answer submitted for question by user '{}'.", username);
            GameEventManager.getInstance().publish(
                    GameEventType.CORRECT_ANSWER_SUBMITTED,
                    scoringService.getScore() + 1
            );
            loadNextGame();
        } else {
            logger.debug("Incorrect answer submitted for question  by user '{}'.", username);
            GameEventManager.getInstance().publish(
                    GameEventType.INCORRECT_ANSWER_SUBMITTED,
                    scoringService.getScore()
            );
        }
    }
}