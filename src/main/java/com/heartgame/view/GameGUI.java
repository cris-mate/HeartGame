package com.heartgame.view;

import com.heartgame.controller.GameController;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.event.GameEventType;
import com.heartgame.event.GameEventListener;
import com.heartgame.event.GameEventManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * The main view for the game screen
 * Reacts to events updating the UI
 * Uses UserSession for accessing current user information
 * Includes logout functionality
 */
public class GameGUI extends JFrame implements GameEventListener {

    @Serial
    private static final long serialVersionUID = -107785653906635L;

    private final JLabel questArea = new JLabel();
    private final JTextArea infoArea = new JTextArea(1, 40);
    private final JLabel timerLabel = new JLabel("Time: 60s");
    private final JButton[] solutionButton = new JButton[10];
    private final User user;
    private final GameController controller;

    /**
     * Constructs the main game GUI, initializes all UI components,
     * and links this view to its controller
     * @param user The logged-in user
     */
    public GameGUI(User user) {
        super("Heart Game");
        this.user = user;

        setSize(860, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main container with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ========== TOP PANEL: User Info + Control Buttons ==========

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // User info (left side)
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel userLabel = new JLabel("Playing as: " + user.getDisplayName());
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userLabel.setForeground(new Color(0, 123, 255)); // Blue
        userPanel.add(userLabel);
        topPanel.add(userPanel, BorderLayout.WEST);

        // Control buttons (right side)
        JPanel controlButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exitGameButton = new JButton("Exit Game");
        exitGameButton.setFocusPainted(false);
        exitGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        exitGameButton.addActionListener(e -> handleLogout());
        controlButtonsPanel.add(exitGameButton);
        topPanel.add(controlButtonsPanel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.GRAY, 2),
                        "How many hearts are there?",
                        0,
                        0,
                        new Font("Arial", Font.BOLD, 16)
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

        // Timer (left side)
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timerLabel.setForeground(new Color(0, 123, 255)); // Blue
        timerPanel.add(timerLabel);

        // Score (right side)
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel scoreLabel = new JLabel("Score: ");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scorePanel.add(scoreLabel);

        infoPanel.add(timerPanel, BorderLayout.WEST);
        infoPanel.add(scorePanel, BorderLayout.EAST);
        bottomPanel.add(infoPanel, BorderLayout.NORTH);

        // Solution buttons panel
        JPanel solutionButtonsPanel = new JPanel(new GridLayout(1, 10, 10, 10));
        solutionButtonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 10; i++) {
            solutionButton[i] = new JButton(String.valueOf(i));
            solutionButton[i].setActionCommand(String.valueOf(i));
            solutionButton[i].setFont(new Font("Arial", Font.BOLD, 24));
            solutionButton[i].setPreferredSize(new Dimension(40, 40));
            //solutionButton[i].setBackground(new Color(0, 123, 255)); // Blue
            solutionButton[i].setForeground(new Color(0, 123, 255));
            solutionButton[i].setFocusPainted(false);
            solutionButton[i].setBorder(BorderFactory.createRaisedBevelBorder());

            // Add hover effect
            final int index = i;
            solutionButton[i].addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (solutionButton[index].isEnabled()) {
                        solutionButton[index].setBackground(new Color(0, 86, 179));
                    }
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (solutionButton[index].isEnabled()) {
                        solutionButton[index].setBackground(new Color(0, 123, 255));
                    }
                }
            });

            solutionButtonsPanel.add(solutionButton[i]);

        }

        bottomPanel.add(solutionButtonsPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        getContentPane().add(mainPanel);

        // Subscribe to game events
        GameEventManager.getInstance().subscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, this);
        GameEventManager.getInstance().subscribe(GameEventType.INCORRECT_ANSWER_SUBMITTED, this);

        // Initialize controller & start the game
        controller = new GameController(this, user);
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
     * Handles game events to provide visual feedback to the user
     * Updates the info text based on whether a correct or incorrect solution was submitted
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
     * Handles logout button
     * Publishes logout event, cleans up, and returns to login screen
     */
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?\nYour current game will be lost.",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Cleanup controller
            if (controller != null) {
                controller.cleanup();
            }

            // Publish logout event
            GameEventManager.getInstance().publish(GameEventType.PLAYER_LOGGED_OUT, user);

            // Unsubscribe from events to prevent memory leaks
            GameEventManager.getInstance().unsubscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, this);
            GameEventManager.getInstance().unsubscribe(GameEventType.INCORRECT_ANSWER_SUBMITTED, this);

            // Clear user session
            UserSession.getInstance().logout();

            // Close this window
            this.dispose();

            // Reopen login window
            SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
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
        updateInfo("How many hearts are there?   Score: " + score, null);
    }

    /**
     * Updates the information area with a new message
     * @param message The message to display
     */
    public void updateInfo(String message) {
        infoArea.setText(message);
    }

    /**
     * Updates the information area with a new message and color
     * @param message The message to display
     * @param color The background color (null for default)
     */
    public void updateInfo(String message, Color color) {
        infoArea.setText(message);
        if (color != null) {
            infoArea.setBackground(color);
            infoArea.setForeground(Color.WHITE);
        } else {
            infoArea.setBackground(new Color(248, 249, 250));
            infoArea.setForeground(Color.BLACK);
        }
    }

    /**
     * Updates the timer display
     * @param secondsRemaining The number of seconds remaining
     */
    public void updateTimer(int secondsRemaining) {
        timerLabel.setText("⏱ Time: " + secondsRemaining + "s");
        // Change to warning color in last 10 seconds
        if (secondsRemaining <= 10) {
            timerLabel.setForeground(Color.RED);
            // Add blinking effect in last 5 seconds
            if (secondsRemaining <= 5) {
                timerLabel.setFont(new Font("Arial", Font.BOLD, 32));
            }
        } else if (secondsRemaining <= 20) {
            timerLabel.setForeground(new Color(255, 165, 0)); // Orange
        } else {
            timerLabel.setForeground(new Color(0, 123, 255)); // Blue
            timerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        }
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
        }
    }

    /**
     * Disables all solution buttons
     */
    public void disableSolutionButtons() {
        for (JButton button : solutionButton) {
            button.setEnabled(false);
        }
    }

    /**
     * Shows the game over message with final score
     * @param finalScore The player's final score
     */
    public void showGameOver(int finalScore) {
        // Create a custom panel for better formatting
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel messageLabel = new JLabel("<html><center>" +
                "<h2>⏰ Time's Up!</h2>" +
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
            // Restart the game
            this.dispose();
            SwingUtilities.invokeLater(() -> new GameGUI(user).setVisible(true));
        } else {
            // Return to login
            handleLogout();
        }
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
