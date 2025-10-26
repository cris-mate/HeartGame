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

    /**
     * Constructs the main game GUI, initializes all UI components,
     * and links this view to its controller
     * @param user The logged-in user
     */
    public GameGUI(User user) {
        super("How many hearts are there? - Playing as: " + user.getUsername());
        this.user = user;

        setSize(690, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel gamePanel = new JPanel();

        // Timer label at the top
        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timerLabel.setForeground(Color.BLACK);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(timerLabel);

        infoArea.setEditable(false);
        JScrollPane infoPane = new JScrollPane(infoArea);
        mainPanel.add(infoPane);

        questArea.setSize(330, 600);
        JScrollPane questPane = new JScrollPane(questArea);
        gamePanel.add(questPane);

        // Create solution buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 10, 5, 10));
        for (int i = 0; i < 10; i++) {
            solutionButton[i] = new JButton(String.valueOf(i));
            solutionButton[i].setActionCommand(String.valueOf(i));
            buttonPanel.add(solutionButton[i]);
        }
        gamePanel.add(buttonPanel);

        mainPanel.add(gamePanel, BorderLayout.CENTER);

        // Add logout button at the top
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(220, 53, 69)); // Bootstrap danger red
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> handleLogout());
        topPanel.add(new JLabel("User: " + user.getDisplayName() + "  "));
        topPanel.add(logoutButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        getContentPane().add(mainPanel);

        // Subscribe to game events
        GameEventManager.getInstance().subscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, this);
        GameEventManager.getInstance().subscribe(GameEventType.INCORRECT_ANSWER_SUBMITTED, this);

        new GameController(this, user);
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
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
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
     * Updates the timer display
     * @param secondsRemaining The number of seconds remaining
     */
    public void updateTimer(int secondsRemaining) {
        timerLabel.setText("Time: " + secondsRemaining + "s");
        // Change to warning color in last 10 seconds
        if (secondsRemaining <= 10) {
            timerLabel.setForeground(Color.RED);
        } else {
            timerLabel.setForeground(Color.BLACK);
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
        JOptionPane.showMessageDialog(this,
                "Time's up!\nYour final score: " + finalScore + ".",
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE);
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
