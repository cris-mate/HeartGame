package controller;

import model.Question;
import service.GameAPIService;
import service.ScoringService;
import view.GameGUI;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

/**
 * Controller for handling the main game logic.
 */
public class GameController {

    private final GameGUI gameView;
    private final GameAPIService apiService;
    private final ScoringService scoringService;
    private Question currentQuestion;

    /**
     * Constructs a new GameController.
     *
     * @param gameView The game view it controls.
     */
    public GameController(GameGUI gameView) {
        this.gameView = gameView;
        this.apiService = new GameAPIService();
        this.scoringService = new ScoringService();
        initController();
        loadNextGame();
    }

    private void initController() {
        for (int i = 0; i < 10; i++) {
            gameView.getButton(i).addActionListener(this::handleAnswer);
        }
    }

    public void loadNextGame() {
        currentQuestion = apiService.getRandomGame();
        if (currentQuestion != null) {
            gameView.updateQuestion(currentQuestion.getImage(), scoringService.getScore());
        } else {
            gameView.showError("Failed to load the next game.");
        }
    }


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
