package controller;

import model.Question;
import service.GameAPIService;
import service.ScoringService;
import view.GameGUI;
import event.GameEventType;
import event.GameEventManager;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Controller for handling the main game logic
 * Publishes events based on user actions
 */
public class GameController {

    private static final Logger logger = Logger.getLogger(GameController.class.getName());
    private final GameGUI gameView;
    private final GameAPIService apiService;
    private final ScoringService scoringService;
    private Question currentQuestion;

    /**
     * Constructs a new GameController
     * It initializes the services,links the controller to the view,
     * loads the first question and publishes the GAME_STARTED event.
     * @param gameView The game view it controls
     */
    public GameController(GameGUI gameView) {
        this.gameView = gameView;
        this.apiService = new GameAPIService();
        this.scoringService = new ScoringService();
        initController();
        loadNextGame();
        GameEventManager.getInstance().publish(GameEventType.GAME_STARTED, null);
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
     * If a question cannot be loaded, it displays an error message
     */
    public void loadNextGame() {
        try {
            currentQuestion = apiService.getNewQuestion();
            if (currentQuestion != null) {
                gameView.updateQuestion(currentQuestion.getImage(), scoringService.getScore());
            } else {
                gameView.showError("Failed to load a valid game question.");
            }
        } catch (IOException e) {
            logger.severe("Failed to fetch a new question from the API." + e.getMessage());
            gameView.showError("Could not fetch a new game. Please check your connection.");
        }
    }

    /**
     * Handles the user's answer submission
     * t checks the solution and publishes either a CORRECT_ANSWER_SUBMITTED or
     * INCORRECT_ANSWER_SUBMITTED event with the relevant score data
     * @param e The ActionEvent triggered by the user's button click
     */
    public void handleAnswer( ActionEvent e ) {
        int solution = Integer.parseInt(e.getActionCommand());
        if (solution == currentQuestion.getSolution()) {
            GameEventManager.getInstance().publish(GameEventType.CORRECT_ANSWER_SUBMITTED, scoringService.getScore() + 1);
            loadNextGame();
        } else {
            GameEventManager.getInstance().publish(GameEventType.INCORRECT_ANSWER_SUBMITTED, scoringService.getScore());
        }
    }
}
