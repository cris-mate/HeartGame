package com.heartgame.view;

import com.heartgame.controller.HomeController;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * The home screen view displayed after successful login
 * Shows user information, game controls, and game instructions
 */
public class HomeGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 8394756201928374650L;

    private final User user;
    private final JButton startGameButton = new JButton("Start Game");
    private final JButton leaderboardButton = new JButton("Leaderboard");
    private final JButton logoutButton = new JButton("Logout");
    private final JButton exitButton = new JButton("Exit");

    /**
     * Constructs the home GUI with user information and controls
     * @param user The logged-in user
     */
    public HomeGUI(User user) {
        super("Heart Game - Home");
        this.user = user;

        setSize(860, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(248, 249, 250));

        // ========== TOP PANEL: User Info ==========
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(248, 249, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel userLabel = new JLabel("Logged in as: " + user.getUsername());
        userLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userLabel.setForeground(new Color(0, 123, 255)); // Blue
        topPanel.add(userLabel);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ========== CENTER PANEL: Info and Instructions ==========
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Title
        JLabel titleLabel = new JLabel("Welcome to Heart Game!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(220, 53, 69));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Description and Instructions
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14));
        infoArea.setBackground(Color.WHITE);
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String infoText = "Hello, " + user.getUsername() + "!\n\n" +
                "ABOUT THE GAME\n" +
                "Heart Game is a fast-paced pattern recognition game that challenges your counting skills and reaction time. " +
                "You'll be presented with images containing various numbers of hearts and" +
                " your task is to identify and select the correct count.\n\n" +
                "HOW TO PLAY\n" +
                "1. Click \"Start Game\" button to begin a new game session of 60 seconds\n" +
                "2. Try to answer as many questions correctly as you can before time runs out!\n" +
                "3. Each question shows an image with hearts scattered across it\n" +
                "4. Count the hearts carefully and click the corresponding number button (0-9)\n" +
                "5. Each correct answer adds 1 point to your score, whereas wrong answers don’t affect your score.\n\n" +
                "TIPS FOR SUCCESS\n" +
                "• Count systematically (left to right, top to bottom)\n" +
                "• Stay focused and avoid distractions - accuracy is more important than speed\n" +
                "• Don't rush but keep an eye on the timer, especially in the last 10 seconds\n" +
                "• Check the Leaderboard to see how you rank!\n\n" +
                "READY TO PLAY?\n" +
                "Click the \"Start Game\" button whenever you're ready. Good luck and have fun!";

        infoArea.setText(infoText);

        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        centerPanel.add(titleLabel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ========== BOTTOM PANEL: Control Buttons ==========
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(new Color(248, 249, 250));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Start Game button (Primary action)
        startGameButton.setBackground(new Color(248, 249, 250));
        startGameButton.setForeground(new Color(220, 53, 69));
        startGameButton.setFocusPainted(false);
        startGameButton.setFont(new Font("Arial", Font.BOLD, 16));
        startGameButton.setPreferredSize(new Dimension(160, 45));
        startGameButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        startGameButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect for Start Game button
        startGameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startGameButton.setBackground(new Color(33, 136, 56));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                startGameButton.setBackground(new Color(40, 167, 69));
            }
        });

        // Leaderboard button
        leaderboardButton.setBackground(new Color(50, 50, 50));
        leaderboardButton.setForeground(new Color(220, 53, 69));
        leaderboardButton.setFocusPainted(false);
        leaderboardButton.setFont(new Font("Arial", Font.BOLD, 16));
        leaderboardButton.setPreferredSize(new Dimension(140, 45));
        leaderboardButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        leaderboardButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect for Leaderboard button
        leaderboardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                leaderboardButton.setBackground(new Color(0, 86, 179));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                leaderboardButton.setBackground(new Color(0, 123, 255));
            }
        });

        // Logout button
        logoutButton.setBackground(new Color(248, 249, 250));
        logoutButton.setForeground(new Color(220, 53, 69));
        logoutButton.setFocusPainted(false);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 16));
        logoutButton.setPreferredSize(new Dimension(130, 45));
        logoutButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect for Logout button
        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(230, 170, 0));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(255, 193, 7));
            }
        });

        // Exit button
        exitButton.setBackground(new Color(248, 249, 250));
        exitButton.setForeground(new Color(220, 53, 69));
        exitButton.setFocusPainted(false);
        exitButton.setFont(new Font("Arial", Font.BOLD, 16));
        exitButton.setPreferredSize(new Dimension(130, 45));
        exitButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect for Exit button
        exitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exitButton.setBackground(new Color(180, 35, 50));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exitButton.setBackground(new Color(220, 53, 69));
            }
        });

        bottomPanel.add(startGameButton);
        bottomPanel.add(leaderboardButton);
        bottomPanel.add(logoutButton);
        bottomPanel.add(exitButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        getContentPane().add(mainPanel);

        // Initialize controller
        new HomeController(this);
    }

    /**
     * Alternative constructor using UserSession
     */
    public HomeGUI() {
        this(UserSession.getInstance().getCurrentUser());
        if (user == null) {
            throw new IllegalStateException("No user logged in. Cannot create HomeGUI.");
        }
    }

    /**
     * Gets the Start Game button
     * @return The start game button
     */
    public JButton getStartGameButton() {
        return startGameButton;
    }

    /**
     * Gets the Leaderboard button
     * @return The leaderboard button
     */
    public JButton getLeaderboardButton() {
        return leaderboardButton;
    }

    /**
     * Gets the Logout button
     * @return The logout button
     */
    public JButton getLogoutButton() {
        return logoutButton;
    }

    /**
     * Gets the Exit button
     * @return The exit button
     */
    public JButton getExitButton() {
        return exitButton;
    }

    /**
     * Gets the logged-in user
     * @return The user object
     */
    public User getUser() {
        return user;
    }

    /**
     * Main entry point for testing
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            User testUser = new User("TestPlayer");
            UserSession.getInstance().login(testUser);
            new HomeGUI().setVisible(true);
        });
    }
}