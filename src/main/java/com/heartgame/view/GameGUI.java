package com.heartgame.view;

import com.heartgame.controller.GameController;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.event.GameEventType;
import com.heartgame.event.GameEventListener;
import com.heartgame.event.GameEventManager;
import com.heartgame.service.GameTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * The main view for the game screen
 * Displays countdown timer, start button, questions, and answer buttons
 * Implements TimerUpdateListener to receive timer updates
 * Uses UserSession for accessing current user information
 */
public class GameGUI extends JFrame implements GameEventListener, GameTimer.TimerUpdateListener {

    @Serial
    private static final long serialVersionUID = -107785653906635L;

    private static final Logger logger = LoggerFactory.getLogger(GameGUI.class);

    private final JLabel questArea = new JLabel();
    private final JTextArea infoArea = new JTextArea(1, 40);
    private final JLabel timerLabel = new JLabel("Time: 60s");
    private final JButton startButton = new JButton("Start Game");
    private final JButton[] answerButtons = new JButton[10];
    private final User user;
    private GameController gameController;
    private boolean gameStarted = false;

    /**
     * Constructs the main game GUI, initializes all UI components,
     * @param user The logged-in user
     */
    public GameGUI(User user) {
        super("Heart Game - Playing as: " + user.getUsername());
        this.user = user;

        initializeUI();
        setupEventListeners();

        new GameController(this, user);
        logger.info("GameGUI initialized - ready to start game");
    }

    /**
     * Alternative constructor using UserSession
     * For cases where user is already in session
     */
    public GameGUI() {
        this(UserSession.getInstance().getCurrentUser());
        if (user == null) {
            throw new IllegalStateException("No user logged in. Cannot create GameGUI.");
        }
    }

    /**
     * Initializes UI components and layout
     */
    private void initializeUI() {
        setSize(720, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel - Info and Timer
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));

        infoArea.setEditable(false);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14));
        infoArea.setText("Press 'Start Game' to begin! You have 60 seconds.");
        JScrollPane infoPane = new JScrollPane(infoArea);
        infoPane.setPreferredSize(new Dimension(400, 50));

        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timerLabel.setForeground(Color.BLACK);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setPreferredSize(new Dimension(150, 50));
        timerLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        topPanel.add(infoPane, BorderLayout.CENTER);
        topPanel.add(timerLabel, BorderLayout.EAST);

        // Center panel - Question image
        questArea.setHorizontalAlignment(SwingConstants.CENTER);
        questArea.setPreferredSize(new Dimension(400, 400));
        questArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        questArea.setText("Question will appear here");
        JScrollPane questPane = new JScrollPane(questArea);
        questPane.setPreferredSize(new Dimension(400, 400));

        // Bottom panel - Start button and Answer buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        // Start button (prominent)
        startButton.setText("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 18));
        startButton.setPreferredSize(new Dimension(200, 50));
        startButton.setBackground(new Color(34, 139, 34)); // Forest green
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(true);
        startButton.setOpaque(true);
        startButton.setContentAreaFilled(true);

        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startPanel.add(startButton);

        // Answer buttons panel
        JPanel answerPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        for (int i = 0; i < 10; i++) {
            answerButtons[i] = new JButton(String.valueOf(i));
            answerButtons[i].setActionCommand(String.valueOf(i));
            answerButtons[i].setFont(new Font("Arial", Font.BOLD, 20));
            answerButtons[i].setPreferredSize(new Dimension(60, 50));
            answerButtons[i].setEnabled(false); // Disabled until game starts
            answerPanel.add(answerButtons[i]);
        }

        bottomPanel.add(startPanel, BorderLayout.NORTH);
        bottomPanel.add(answerPanel, BorderLayout.CENTER);

        // Add all panels to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(questPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    /**
     * Sets up event listeners for buttons
     */
    private void setupEventListeners() {
        // Start button listener
        startButton.addActionListener(e -> startGame());

        // Subscribe to game events
        GameEventManager.getInstance().subscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, this);
        GameEventManager.getInstance().subscribe(GameEventType.INCORRECT_ANSWER_SUBMITTED, this);
    }

    /**
     * Starts the game - called when Start button is clicked
     */
    private void startGame() {
        if (gameStarted) {
            logger.warn("Game already started - ignoring duplicate start request");
            return; // Already started
        }

        logger.info("User clicked Start Game button");
        gameStarted = true;

        // Disable start button
        startButton.setEnabled(false);
        startButton.setText("Now Playing");
        startButton.setBackground(Color.GRAY);

        // Enable answer buttons
        for (JButton button : answerButtons) {
            button.setEnabled(true);
        }

        // Update info
        infoArea.setText("How many hearts are there?   Score: 0");

        // Start the game (this will load first question and start timer)
        logger.info("Calling gameController.startGame()");
        gameController.startGame();
        logger.info("Game started successfully - timer should be running");
    }

    /**
     * Called by GameTimer every second to update the countdown display
     * @param remainingSeconds Time remaining in seconds
     * @param isWarning True if in warning state (last 10 seconds)
     */
    @Override
    public void onTimerUpdate(int remainingSeconds, boolean isWarning) {
        // Update timer text
        timerLabel.setText("Time: " + remainingSeconds + "s");

        // Change style for warning (last 10 seconds)
        if (isWarning) {
            timerLabel.setForeground(Color.RED);
            timerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        } else {
            timerLabel.setForeground(Color.BLACK);
            timerLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        }
    }

    /**
     * Handles game events to provide visual feedback
     * @param eventType The type of event that occurred
     * @param data      The current score, passed as an Integer
     */
    @Override
    public void onGameEvent(GameEventType eventType, Object data) {
        int currentScore = (int) data;
        if (eventType == GameEventType.CORRECT_ANSWER_SUBMITTED) {
            updateInfo("Good! Score: " + currentScore);
        } else if (eventType == GameEventType.INCORRECT_ANSWER_SUBMITTED) {
            updateInfo("Oops. Try again! Score: " + currentScore);
        }
    }

    /**
     * Updates the question area with a new image and refreshes the score display
     * @param image The new question image
     * @param score The current score
     */
    public void updateQuestion(BufferedImage image, int score) {
        ImageIcon ii = new ImageIcon(image);
        questArea.setIcon(ii);
        questArea.setText(null); // Clear text when image loads
        infoArea.setText("How many hearts are there?   Score: " + score);
    }

    /**
     * Updates the information area with a new message
     * @param message The message to display
     */
    public void updateInfo(String message) {
        infoArea.setText(message);
    }

    /**
     * Returns the button at the specified index
     * @param index The index of the button (0-9)
     * @return The JButton at that index
     */
    public JButton getButton(int index) {
        return answerButtons[index];
    }

    /**
     * Disables all answer buttons
     */
    public void disableAnswerButtons() {
        for (JButton button : answerButtons) {
            button.setEnabled(false);
        }
    }

    /**
     * Displays an error message in a dialog box
     * @param message The error message to show
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @return True if the game has been started
     */
    public boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * Main entry point to launch the game GUI
     * For testing purposes only - in production, use LoginGUI
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            User testUser = new User("TestPlayer");
            UserSession.getInstance().login(testUser);
            new GameGUI().setVisible(true);
        });
    }
}
