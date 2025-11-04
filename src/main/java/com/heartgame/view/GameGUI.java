package com.heartgame.view;

import com.heartgame.controller.GameController;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.event.GameEventType;
import com.heartgame.event.GameEventManager;
import com.heartgame.service.AvatarService;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * The main view for the game screen
 * Reacts to events updating the UI (updated by GameController)
 * Uses UserSession for accessing current user information
 * Uses navigation events for screen transitions
 * Includes pause/resume and stop game functionality
 */
public class GameGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = -107785653906635L;

    private final JLabel questArea = new JLabel();
    private final JLabel timerLabel = new JLabel("Starting Time: 60s");
    private final JLabel scoreLabel = new JLabel("Score: 0");
    private final JButton[] solutionButton = new JButton[10];
    private final GameController controller;
    private final JLabel avatarLabel = new JLabel();

    // Control buttons
    private final JButton startNewGameButton = new JButton("Start New Game");
    private final JButton pauseResumeButton = new JButton("Pause Game");
    private final JButton stopGameButton = new JButton("Stop Playing");

    // Store current game state for display
    private int currentScore = 0;
    private BufferedImage currentQuestionImage = null;

    /**
     * Constructs the main game GUI, initializes all UI components,
     * and links this view to its controller
     * Uses UserSession to access the current authenticated user
     */
    public GameGUI() {
        super("Heart Game");

        // Get user from session
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No user logged in. Cannot create GameGUI.");
        }

        setSize(860, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ========== TOP PANEL: User Info + Control Buttons ==========
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // User info (left side)
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));

        // --- Avatar Label ---
        avatarLabel.setPreferredSize(new Dimension(48, 48));
        avatarLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 255), 2));
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(Color.WHITE);
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setText("...");
        userPanel.add(avatarLabel);

        // --- Username Label ---
        JLabel userLabel = new JLabel("Playing as: " + user.getUsername());
        userLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        userLabel.setForeground(new Color(0, 123, 255));
        userPanel.add(userLabel);
        topPanel.add(userPanel, BorderLayout.WEST);

        // --- Load Avatar ---
        loadUserAvatar(user.getUsername());

        // Control buttons (right side)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        // --- Control Button Styling ---
        Color controlBackground = new Color(230, 230, 230);
        Color controlHover = new Color(200, 200, 200);
        Color controlForeground = new Color(0, 123, 255);
        Color controlBorder = Color.LIGHT_GRAY;
        Font controlFont = new Font("Arial", Font.BOLD, 16);
        Border roundedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(controlBorder, 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );

        // Start New Game button
        startNewGameButton.setBackground(controlBackground);
        startNewGameButton.setForeground(controlForeground);
        startNewGameButton.setFont(controlFont);
        startNewGameButton.setPreferredSize(new Dimension(150, 45));
        startNewGameButton.setBorder(roundedBorder);
        startNewGameButton.setOpaque(true);
        startNewGameButton.setFocusPainted(false);
        startNewGameButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startNewGameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (startNewGameButton.isEnabled()) {
                    startNewGameButton.setBackground(controlHover);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (startNewGameButton.isEnabled()) {
                    startNewGameButton.setBackground(controlBackground);
                }
            }
        });
        startNewGameButton.addActionListener(e -> handleStartNewGame());
        controlPanel.add(startNewGameButton);

        // Pause/Resume button
        pauseResumeButton.setBackground(controlBackground);
        pauseResumeButton.setForeground(controlForeground);
        pauseResumeButton.setFont(controlFont);
        pauseResumeButton.setPreferredSize(new Dimension(150, 45));
        pauseResumeButton.setBorder(roundedBorder);
        pauseResumeButton.setOpaque(true);
        pauseResumeButton.setFocusPainted(false);
        pauseResumeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pauseResumeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (pauseResumeButton.isEnabled()) {
                    pauseResumeButton.setBackground(controlHover);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (pauseResumeButton.isEnabled()) {
                    pauseResumeButton.setBackground(controlBackground);
                }
            }
        });
        pauseResumeButton.addActionListener(e -> handlePauseResume());
        controlPanel.add(pauseResumeButton);

        // Stop Playing button
        stopGameButton.setBackground(controlBackground);
        stopGameButton.setForeground(controlForeground);
        stopGameButton.setFont(controlFont);
        stopGameButton.setPreferredSize(new Dimension(150, 45));
        stopGameButton.setBorder(roundedBorder);
        stopGameButton.setOpaque(true);
        stopGameButton.setFocusPainted(false);
        stopGameButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        stopGameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (stopGameButton.isEnabled()) {
                    stopGameButton.setBackground(controlHover);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (stopGameButton.isEnabled()) {
                    stopGameButton.setBackground(controlBackground);
                }
            }
        });
        stopGameButton.addActionListener(e -> handleStopGame());
        controlPanel.add(stopGameButton);

        topPanel.add(controlPanel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ========== CENTER PANEL: Question Display ==========
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.GRAY, 1),
                        "How many hearts are there?",
                        0,
                        0,
                        new Font("Arial", Font.PLAIN, 20),
                        new Color(220, 53, 69)
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        questArea.setHorizontalAlignment(SwingConstants.CENTER);
        questArea.setVerticalAlignment(SwingConstants.CENTER);
        questArea.setPreferredSize(new Dimension(400, 350));

        JScrollPane questPane = new JScrollPane(questArea);
        questPane.setPreferredSize(new Dimension(450, 400));
        centerPanel.add(questPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ========== BOTTOM PANEL: Timer + Score + Solution Buttons ==========
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Timer and Score panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Timer (left side)
        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        timerPanel.add(timerLabel);

        // Score (right side)
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        scoreLabel.setForeground(new Color(0, 123, 255));
        scorePanel.add(scoreLabel);

        infoPanel.add(timerPanel, BorderLayout.WEST);
        infoPanel.add(scorePanel, BorderLayout.EAST);
        bottomPanel.add(infoPanel, BorderLayout.NORTH);

        // Solution buttons panel
        JPanel solutionButtonsPanel = new JPanel(new GridLayout(1, 10, 10, 10));
        solutionButtonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Solution Button Styling ---
        Color solutionBackground = new Color(210, 210, 210);
        Color solutionHover = new Color(190, 190, 190);
        Color solutionBorderColor = new Color(170, 170, 170);
        Font solutionFont = new Font("Arial", Font.BOLD, 20);

        Border solutionBorder = BorderFactory.createLineBorder(solutionBorderColor, 2);

        for (int i = 0; i < 10; i++) {
            solutionButton[i] = new JButton(String.valueOf(i));
            solutionButton[i].setActionCommand(String.valueOf(i));
            solutionButton[i].setFont(solutionFont);
            solutionButton[i].setPreferredSize(new Dimension(40, 40));
            solutionButton[i].setBorder(roundedBorder);
            solutionButton[i].setFocusPainted(false);
            solutionButton[i].setOpaque(true);
            solutionButton[i].setBorder(BorderFactory.createRaisedBevelBorder());

            // Set solution button colors and border
            solutionButton[i].setBackground(solutionBackground);
            solutionButton[i].setForeground(new Color(220, 53, 69));
            solutionButton[i].setBorder(solutionBorder);
            solutionButton[i].setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Add hover effect
            final int index = i;
            solutionButton[i].addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (solutionButton[index].isEnabled()) {
                        solutionButton[index].setBackground(solutionHover);
                    }
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (solutionButton[index].isEnabled()) {
                        solutionButton[index].setBackground(solutionBackground);
                    }
                }
            });
            solutionButtonsPanel.add(solutionButton[i]);

        }

        bottomPanel.add(solutionButtonsPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        getContentPane().add(mainPanel);

        // Initialize controller & start the game
        controller = new GameController(this);
    }

    /**
     * Loads user avatar asynchronously
     * Shows loading state and handles errors gracefully
     * @param username The username to fetch avatar for
     */
    private void loadUserAvatar(String username) {
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                AvatarService avatarService = new AvatarService();
                return avatarService.fetchAvatar(username);
            }

            @Override
            protected void done() {
                try {
                    BufferedImage avatar = get();
                    if (avatar != null) {
                        // Scale avatar to fit
                        Image scaledAvatar = avatar.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                        avatarLabel.setIcon(new ImageIcon(scaledAvatar));
                        avatarLabel.setText(null);
                    } else {
                        // Fallback to initial letter
                        avatarLabel.setText(username.substring(0, 1).toUpperCase());
                        avatarLabel.setFont(new Font("Arial", Font.BOLD, 20));
                    }
                } catch (Exception e) {
                    // Error loading avatar - use fallback
                    avatarLabel.setText(username.substring(0, 1).toUpperCase());
                    avatarLabel.setFont(new Font("Arial", Font.BOLD, 20));
                }
            }
        };

        worker.execute();
    }

    /**
     * Shows feedback for a correct answer
     * Called by GameController (proper MVC pattern)
     * @param score The new score after correct answer
     */
    public void showCorrectAnswerFeedback(int score) {
        updateScoreWithFeedback(score, "Good! Keep going!   Current ", new Color(34, 139, 34));
    }

    /**
     * Shows feedback for an incorrect answer
     * Called by GameController (proper MVC pattern)
     * @param score The current score (unchanged)
     */
    public void showIncorrectAnswerFeedback(int score) {
        updateScoreWithFeedback(score, "Oops! Try again!   Current ", new Color(220, 53, 69));
    }

    /**
     * Updates the score with temporary feedback message
     * Feedback automatically resets to normal display after 2 seconds
     * Cancels any previous feedback timer to handle rapid consecutive answers
     * @param score The new score
     * @param feedback The feedback message to show temporarily
     * @param color The color for the feedback message
     */
    private void updateScoreWithFeedback(int score, String feedback, Color color) {
        this.currentScore = score;
        scoreLabel.setText(feedback + "Score: " + score);
        scoreLabel.setForeground(color);
    }

    /**
     * Updates the timer display
     * @param secondsRemaining The number of seconds remaining
     */
    public void updateTimer(int secondsRemaining) {
        timerLabel.setText("Time: " + secondsRemaining + "s");
        // Change to warning color in last 10 seconds
        if (secondsRemaining <= 10) {
            timerLabel.setForeground(new Color(220, 53, 69));
        } else {
            timerLabel.setForeground(new Color(0, 123, 255));
        }
    }

    /**
     * Handles the Start New Game button using navigation events
     * Restarts the game with a fresh session
     */
    private void handleStartNewGame() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to start a new game?\nYour current progress will be lost.",
                "Start New Game?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Cleanup current controller
            if (controller != null) {
                controller.cleanup();
            }

            // Close this window
            this.dispose();

            // Navigate to new game using event
            GameEventManager.getInstance().publish(GameEventType.NAVIGATE_TO_GAME, null);
        }
    }

    /**
     * Handles the Pause/Resume button
     * Toggles between paused and playing states
     */
    private void handlePauseResume() {
        if (controller.isPaused()) {
            controller.resumeGame();
            pauseResumeButton.setText("Pause Game");
        } else {
            controller.pauseGame();
            pauseResumeButton.setText("Resume Game");
        }
    }

    /**
     * Handles the Stop Game button using navigation events
     * Returns to home screen with confirmation
     */
    private void handleStopGame() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to stop playing?\nYour current game will be lost.",
                "Stop Playing?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Cleanup controller
            if (controller != null) {
                controller.cleanup();
            }

            this.dispose();

            // Return to home screen using navigation event
            GameEventManager.getInstance().publish(GameEventType.NAVIGATE_TO_HOME, null);
        }
    }

    /**
     * Shows a pause overlay message in the quest area
     */
    public void showPauseOverlay() {
        // Store current question image
        if (questArea.getIcon() != null) {
            currentQuestionImage = (BufferedImage) ((ImageIcon) questArea.getIcon()).getImage();
        }

        // Create pause message
        String pauseMessage = "<html><div style='text-align: center;'>" +
                "<h1 style='color: #0066cc; font-size: 32px; margin: 20px;'>Game is paused</h1>" +
                "<p style='font-size: 18px; color: #666; margin: 10px;'>Current Score: " + currentScore + "</p>" +
                "<p style='font-size: 16px; color: #999; margin: 20px;'>Click 'Resume Game' to continue</p>" +
                "</div></html>";

        questArea.setIcon(null);
        questArea.setText(pauseMessage);
        questArea.setHorizontalAlignment(SwingConstants.CENTER);
        questArea.setVerticalAlignment(SwingConstants.CENTER);
    }

    /**
     * Hides the pause overlay and restores the current question
     */
    public void hidePauseOverlay() {
        questArea.setText("");
        if (currentQuestionImage != null) {
            questArea.setIcon(new ImageIcon(currentQuestionImage));
        }
    }

    /**
     * Updates the question area with a new image
     * @param image The new question image
     */
    public void updateQuestion(BufferedImage image) {
        ImageIcon ii = new ImageIcon(image);
        questArea.setIcon(ii);
    }

    /**
     * Returns the button at the specified index
     * @param index The index of the button (0-9)
     * @return The JButton at that index
     */
    public JButton getButton(int index) {
        return solutionButton[index];
    }

    /**
     * Displays an error message in a dialog box
     * @param message The error message to show
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Enables all solution buttons
     */
    public void enableSolutionButtons() {
        for (JButton button : solutionButton) {
            button.setEnabled(true);
            button.setBackground(new Color(0, 123, 255));
        }
    }

    /**
     * Disables all solution buttons
     */
    public void disableSolutionButtons() {
        for (JButton button : solutionButton) {
            button.setEnabled(false);
            button.setBackground(Color.GRAY);
        }
    }

    /**
     * Shows the game over message with final score
     * Uses navigation events for screen transitions
     * @param finalScore The player's final score
     */
    public void showGameOver(int finalScore) {
        // Create a custom panel for better formatting
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel messageLabel = new JLabel("<html><center>" +
                "<h2>Time's Up!</h2>" +
                "<p style='font-size:14px; margin-top:10px;'>Your final score:</p>" +
                "<p style='font-size:36px; color:#0066cc; font-weight:bold;'>" + finalScore + "</p>" +
                "</center></html>");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(messageLabel, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(
                this,
                panel,
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE
        );

        // Ask if user wants to play again
        int playAgain = JOptionPane.showConfirmDialog(
                this,
                "Would you like to play again?",
                "Play Again?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (playAgain == JOptionPane.YES_OPTION) {
            if (controller != null) {
                controller.cleanup();
            }

            // Restart the game using navigation event
            this.dispose();
            GameEventManager.getInstance().publish(GameEventType.NAVIGATE_TO_GAME, null);
        } else {
            // Return to HomeGUI screen using navigation event
            if (controller != null) {
                controller.cleanup();
            }

            this.dispose();
            GameEventManager.getInstance().publish(GameEventType.NAVIGATE_TO_HOME, null);
        }
    }
}