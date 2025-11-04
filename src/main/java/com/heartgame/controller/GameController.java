package com.heartgame.controller;

import com.heartgame.model.GameSession;
import com.heartgame.model.Question;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.persistence.GameSessionDAO;
import com.heartgame.service.HeartGameAPIService;
import com.heartgame.service.ScoringService;
import com.heartgame.service.GameTimer;
import com.heartgame.view.GameGUI;
import com.heartgame.event.GameEventType;
import com.heartgame.event.GameEventManager;
import com.heartgame.event.GameEventListener;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for handling the main game logic
 * Publishes events based on user actions
 * Uses UserSession to access current user information
 * Listens to game events and updates view accordingly
 * Handles GAME_ENDED event published by GameTimer
 */
public class GameController implements GameEventListener {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final GameGUI gameView;
    private final HeartGameAPIService apiService;
    private final ScoringService scoringService;
    private final GameTimer gameTimer;
    private final GameSessionDAO gameSessionDAO;
    private final Instant gameStartTime;

    private Question currentQuestion;
    private boolean isGameActive;
    private int questionsAnsweredCount;

    /**
     * Constructs a new GameController
     * Initializes services, links the controller to the view,
     * loads the first question and publishes the GAME_STARTED event
     * Uses UserSession to access the current authenticated user
     * Subscribes to game events to update view
     * @param gameView The game view it controls
     * @throws IllegalStateException if no user is currently logged in
     */
    public GameController(GameGUI gameView) {
        this.gameView = gameView;
        this.apiService = new HeartGameAPIService();
        this.scoringService = new ScoringService();
        this.gameTimer = new GameTimer();
        this.gameSessionDAO = new GameSessionDAO();
        this.isGameActive = true;
        this.gameStartTime = Instant.now();
        this.questionsAnsweredCount = 0;

        // Subscribe to events
        GameEventManager eventManager = GameEventManager.getInstance();
        eventManager.subscribe(GameEventType.TIMER_TICK, this);
        eventManager.subscribe(GameEventType.GAME_ENDED, this);
        eventManager.subscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, this);
        eventManager.subscribe(GameEventType.INCORRECT_ANSWER_SUBMITTED, this);

        initController();
        loadNextGame();

        // Get user from session
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            logger.error("No authenticated user found");
            throw new IllegalStateException("No authenticated user. Cannot start game.");
        }
        logger.info("Game started for user '{}'.", currentUser.getUsername());
        // this triggers GameTimer to start countdown
        eventManager.publish(GameEventType.GAME_STARTED, currentUser);
    }

    /**
     * Initializes the controller by attaching action listeners to the view's buttons
     */
    private void initController() {
        for (int i = 0; i < 10; i++) {
            gameView.getButton(i).addActionListener(this::handleAnswer);
        }
    }

    /**
     * Handles game events including timer updates and game ending
     * Updates view based on events (proper MVC pattern)
     * @param eventType The type of event that occurred
     * @param data      Event data (remainingSeconds for TIMER_TICK, score for answer events)
     */
    @Override
    public void onGameEvent(GameEventType eventType, Object data) {
        switch (eventType) {
            case TIMER_TICK:
                if (data instanceof Integer) {
                    int remainingSeconds = (Integer) data;
                    gameView.updateTimer(remainingSeconds);
                    logger.trace("Timer updated: {}s remaining.", remainingSeconds);
                }
                break;

            case GAME_ENDED:
                // GameTimer published GAME_ENDED when time expired
                logger.info("Game ended - handling cleanup");
                endGame();
                break;

            case CORRECT_ANSWER_SUBMITTED:
                // Update view with correct answer feedback (proper MVC)
                if (data instanceof Integer) {
                    gameView.showCorrectAnswerFeedback((Integer) data);
                }
                break;

            case INCORRECT_ANSWER_SUBMITTED:
                // Update view with incorrect answer feedback (proper MVC)
                if (data instanceof Integer) {
                    gameView.showIncorrectAnswerFeedback((Integer) data);
                }
                break;

            default:
                // Ignore other events
                break;
        }
    }

    /**
     * Pauses the game
     * Stops timer, disables buttons, shows pause overlay, and publishes GAME_PAUSED event
     */
    public void pauseGame() {
        if (isGameActive && !gameTimer.isPaused()) {
            gameTimer.pauseTimer();
            gameView.showPauseOverlay();
            gameView.disableSolutionButtons();
            logger.debug("Game paused");
            GameEventManager.getInstance().publish(GameEventType.GAME_PAUSED, gameTimer.getRemainingTime());
        }
    }

    /**
     * Resumes the game
     * Restarts timer, enables buttons, shows current question, and publishes GAME_RESUMED event
     */
    public void resumeGame() {
        if (isGameActive && gameTimer.isPaused()) {
            gameTimer.resumeTimer();
            gameView.hidePauseOverlay();
            gameView.enableSolutionButtons();
            logger.debug("Game resumed");
            GameEventManager.getInstance().publish(GameEventType.GAME_RESUMED, gameTimer.getRemainingTime());
        }
    }

    /**
     * Checks if game is currently paused
     * @return True if game is paused
     */
    public boolean isPaused() {
        return gameTimer.isPaused();
    }

    /**
     * Ends the game
     * Stops accepting answers, disables buttons, saves session, and shows final score
     * Note: GAME_ENDED event already published by GameTimer, so we don't publish it again
     */
    private void endGame() {
        if (!isGameActive) {
            return; // Already ended
        }

        isGameActive = false;
        gameTimer.stopTimer();
        gameView.disableSolutionButtons();

        int finalScore = scoringService.getScore();
        logger.info("Game over! Final score: {}", finalScore);

        // Save game session to database
        saveGameSession(finalScore);

        // Show game over dialog
        gameView.showGameOver(finalScore);
    }

    /**
     * Saves the game session to the database
     * Creates a GameSession object and persists it via GameSessionDAO
     * @param finalScore The final score achieved
     */
    private void saveGameSession(int finalScore) {
        User currentUser = UserSession.getInstance().getCurrentUser();

        if (currentUser == null) {
            logger.error("Cannot save game session: no user logged in");
            return;
        }

        try {
            GameSession session = new GameSession(
                    currentUser.getId(),
                    gameStartTime,
                    Instant.now(),
                    finalScore,
                    questionsAnsweredCount
            );

            boolean saved = gameSessionDAO.saveGameSession(session);

            if (saved) {
                logger.info("Game session saved successfully for user '{}': score={}, questions={}",
                        currentUser.getUsername(), finalScore, questionsAnsweredCount);
            } else {
                logger.warn("Failed to save game session for user '{}'", currentUser.getUsername());
            }

        } catch (Exception e) {
            logger.error("Error saving game session", e);
        }
    }

    /**
     * Loads the next game question from the API service and updates the view
     * Publishes QUESTION_LOADED event on success or API_ERROR event on failure
     */
    public void loadNextGame() {
        if (!isGameActive) {
            return; // Don't load new questions if game is over
        }

        try {
            currentQuestion = apiService.getNewQuestion();
            if (currentQuestion != null) {
                gameView.updateQuestion(currentQuestion.getImage());
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
     * Checks the solution and publishes either a CORRECT_ANSWER_SUBMITTED
     * or INCORRECT_ANSWER_SUBMITTED event with the relevant score data
     * Increments questions answered count
     * @param e The ActionEvent triggered by the user's button click
     */
    public void handleAnswer(ActionEvent e) {
        if (!isGameActive) {
            return; // Ignore answers if game is over
        }

        int solution = Integer.parseInt(e.getActionCommand());
        User currentUser = UserSession.getInstance().getCurrentUser();
        String username = currentUser != null ? currentUser.getUsername() : "Unknown";
        questionsAnsweredCount++;

        if (solution == currentQuestion.getSolution()) {
            logger.debug("Correct answer submitted by user '{}'.", username);
            GameEventManager.getInstance().publish(
                    GameEventType.CORRECT_ANSWER_SUBMITTED,
                    scoringService.getScore() + 1
            );
            loadNextGame();
        } else {
            logger.debug("Incorrect answer submitted by user '{}'.", username);
            GameEventManager.getInstance().publish(
                    GameEventType.INCORRECT_ANSWER_SUBMITTED,
                    scoringService.getScore()
            );
        }
    }

    /**
     * Stops the game timer and unsubscribes from events
     * Should be called when user exits the game
     */
    public void cleanup() {
        gameTimer.stopTimer();

        // Unsubscribe from events to prevent memory leaks
        GameEventManager eventManager = GameEventManager.getInstance();
        eventManager.unsubscribe(GameEventType.TIMER_TICK, this);
        eventManager.unsubscribe(GameEventType.GAME_ENDED, this);
        eventManager.unsubscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, this);
        eventManager.unsubscribe(GameEventType.INCORRECT_ANSWER_SUBMITTED, this);

        logger.debug("GameController cleanup completed");
    }
}