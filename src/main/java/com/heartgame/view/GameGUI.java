package com.heartgame.view;

import com.heartgame.controller.GameController;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.event.GameEventType;
import com.heartgame.event.GameEventListener;
import com.heartgame.event.GameEventManager;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * The main view for the game screen
 * Reacts to events updating the UI
 * Uses UserSession for accessing current user information
 */
public class GameGUI extends JFrame implements GameEventListener {

    @Serial
    private static final long serialVersionUID = -107785653906635L;

    private final JLabel questArea = new JLabel();
    private final JTextArea infoArea = new JTextArea(1, 40);
    private final JButton[] buttons = new JButton[10];
    private final User user;

    /**
     * Constructs the main game GUI, initializes all UI components,
     * and links this view to its controller
     * @param user The logged-in user
     */
    public GameGUI(User user) {
        super("What is the missing value? - Playing as: " + user.getUsername());
        this.user = user;

        setSize(690, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();

        infoArea.setEditable(false);
        JScrollPane infoPane = new JScrollPane(infoArea);
        panel.add(infoPane);

        questArea.setSize(330, 600);
        JScrollPane questPane = new JScrollPane(questArea);
        panel.add(questPane);

        for (int i = 0; i < 10; i++) {
            buttons[i] = new JButton(String.valueOf(i));
            buttons[i].setActionCommand(String.valueOf(i));
            panel.add(buttons[i]);
        }

        getContentPane().add(panel);

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
     * Updates the info text based on whether a correct or incorrect answer was submitted
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
        return buttons[index];
    }

    /**
     * Displays an error message in a dialog box
     * @param message The error message to show
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
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
