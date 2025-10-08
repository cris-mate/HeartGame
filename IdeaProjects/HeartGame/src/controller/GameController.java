package controller;

import model.Question;
import service.GameAPIService;
import service.ScoringService;
import view.GameGUI;

import java.awt.event.ActionEvent;

/**
 * Controller for handling the main game logic
 */
public class GameController {

    private final GameGUI gameView;
    private final GameAPIService apiService;
    private final ScoringService scoringService;
    private Question currentQuestion;

    /**
     * Constructs a new GameController
     * @param gameView The game view it controls
     */
    public GameController(GameGUI gameView) {
        this.gameView = gameView;
        this.apiService = new GameAPIService();
        this.scoringService = new ScoringService();
        initController();
        loadNextGame();
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
     * Loads the next game question from the API service and updates the view
     * If a question cannot be loaded, it displays an error message
     */
    public void loadNextGame() {
        currentQuestion = apiService.getRandomGame();
        if (currentQuestion != null) {
            gameView.updateQuestion(currentQuestion.getImage(), scoringService.getScore());
        } else {
            gameView.showError("Failed to load the next game.");
        }
    }

    /**
     * Handles the user's answer submission. It checks if the answer is correct,
     * updates the score, provides feedback to the user, and loads the next question
     * @param e The ActionEvent triggered by the user's button click
     */
    public void handleAnswer( ActionEvent e ) {
        int solution = Integer.parseInt(e.getActionCommand());
        if (solution == currentQuestion.getSolution()) {
            scoringService.increaseScore();
            gameView.updateInfo("Good! Score: " + scoringService.getScore());
            loadNextGame();
        } else {
            gameView.updateInfo("Oops. Try again! Score: " + scoringService.getScore());
        }
    }
}
