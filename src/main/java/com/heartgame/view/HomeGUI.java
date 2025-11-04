package com.heartgame.view;

import com.heartgame.controller.HomeController;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.service.AvatarService;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * The home screen view displayed after successful login
 * Shows user information, game controls, and game instructions
 * Displays user avatar fetched asynchronously
 */
public class HomeGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 8394756201928374650L;

    private final JButton startGameButton = new JButton("Start Game");
    private final JButton leaderboardButton = new JButton("Leaderboard");
    private final JButton logoutButton = new JButton("Logout");
    private final JButton exitButton = new JButton("Exit");
    private final JLabel avatarLabel = new JLabel();

    /**
     * Constructs the home GUI with user information and controls
     * Uses UserSession to access the current authenticated user
     */
    public HomeGUI() {
        super("Heart Game - Home");

        // Get user from session
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No user logged in. Cannot create HomeGUI.");
        }

        setSize(860, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(248, 249, 250));

        // ========== TOP PANEL: User Info with Avatar ==========
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        topPanel.setBackground(new Color(248, 249, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Avatar (placeholder initially)
        avatarLabel.setPreferredSize(new Dimension(48, 48));
        avatarLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 255), 2));
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(Color.WHITE);
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setText("...");
        topPanel.add(avatarLabel);

        // User info
        JLabel userLabel = new JLabel("Logged in as: " + user.getUsername());
        userLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        userLabel.setForeground(new Color(0, 123, 255));
        topPanel.add(userLabel);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Load avatar asynchronously
        loadUserAvatar(user.getUsername());

        // ========== CENTER PANEL: Info and Instructions ==========
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // Title
        JLabel titleLabel = new JLabel("Welcome to Heart Game!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(220, 53, 69));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Description and Instructions
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14));
        infoArea.setBackground(new  Color(240, 240, 240));
        infoArea.setOpaque(true);
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
                "5. Each correct answer adds 1 point to your score, whereas wrong answers don't affect your score.\n\n" +
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

        // --- Control Button Styling ---
        Color controlBackground = new Color(230, 230, 230);
        Color controlHover = new Color(200, 200, 200);
        Color controlForeground = new Color(0, 123, 255);
        Color controlBorder = Color.LIGHT_GRAY;
        Font controlFont = new Font("Arial", Font.BOLD, 16);
        Border roundedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(controlBorder, 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Start Game button
        startGameButton.setBackground(controlBackground);
        startGameButton.setForeground(controlForeground);
        startGameButton.setFont(controlFont);
        startGameButton.setPreferredSize(new Dimension(150, 45));
        startGameButton.setBorder(roundedBorder);
        startGameButton.setOpaque(true);
        startGameButton.setFocusPainted(false);
        startGameButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startGameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (startGameButton.isEnabled()) {
                    startGameButton.setBackground(controlHover);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (startGameButton.isEnabled()) {
                    startGameButton.setBackground(controlBackground);
                }
            }
        });

        // Leaderboard button
        leaderboardButton.setBackground(controlBackground);
        leaderboardButton.setForeground(controlForeground);
        leaderboardButton.setFont(controlFont);
        leaderboardButton.setPreferredSize(new Dimension(150, 45));
        leaderboardButton.setBorder(roundedBorder);
        leaderboardButton.setOpaque(true);
        leaderboardButton.setFocusPainted(false);
        leaderboardButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        leaderboardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (leaderboardButton.isEnabled()) {
                    leaderboardButton.setBackground(controlHover);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (leaderboardButton.isEnabled()) {
                    leaderboardButton.setBackground(controlBackground);
                }
            }
        });

        // Logout button
        logoutButton.setBackground(controlBackground);
        logoutButton.setForeground(controlForeground);
        logoutButton.setFont(controlFont);
        logoutButton.setPreferredSize(new Dimension(150, 45));
        logoutButton.setBorder(roundedBorder);
        logoutButton.setOpaque(true);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (logoutButton.isEnabled()) {
                    logoutButton.setBackground(controlHover);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (logoutButton.isEnabled()) {
                    logoutButton.setBackground(controlBackground);
                }
            }
        });

        // Exit button
        exitButton.setBackground(controlBackground);
        exitButton.setForeground(controlForeground);
        exitButton.setFont(controlFont);
        exitButton.setPreferredSize(new Dimension(150, 45));
        exitButton.setBorder(roundedBorder);
        exitButton.setOpaque(true);
        exitButton.setFocusPainted(false);
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (exitButton.isEnabled()) {
                    exitButton.setBackground(controlHover);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (exitButton.isEnabled()) {
                    exitButton.setBackground(controlBackground);
                }
            }
        });

        bottomPanel.add(startGameButton);
        bottomPanel.add(leaderboardButton);
        bottomPanel.add(logoutButton);
        bottomPanel.add(exitButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        getContentPane().add(mainPanel);

        new HomeController(this);
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
}