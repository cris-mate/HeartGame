package com.heartgame.view;

import com.heartgame.controller.LeaderboardController;
import com.heartgame.model.GameSession;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.Serial;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The leaderboard view displaying top 10 scores
 * Shows rank, username, score, and date in a clean table format
 */
public class LeaderboardGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 8273645192837465L;

    private final JButton backButton = new JButton("Back to Home");
    private final JButton refreshButton = new JButton("Refresh");
    private final JTable leaderboardTable;
    private final DefaultTableModel tableModel;
    private final JLabel userStatsLabel = new JLabel();

    // Table column names
    private static final String[] COLUMN_NAMES = {"Rank", "Player", "Score", "Date"};

    // Date formatter for display
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm").withZone(ZoneId.systemDefault());

    /**
     * Constructs the leaderboard GUI
     * Uses UserSession to access the current authenticated user
     */
    public LeaderboardGUI() {
        super("Leaderboard - HeartGame");

        setSize(860, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(248, 249, 250));

        // ========== TOP PANEL: Title ==========
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(248, 249, 250));

        JLabel titleLabel = new JLabel("Heart Game Top 10 Leaderboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(220, 53, 69));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ========== CENTER PANEL: Leaderboard Table ==========
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        centerPanel.setBackground(Color.WHITE);

        // Create table model
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create table
        leaderboardTable = new JTable(tableModel);
        leaderboardTable.setFont(new Font("Arial", Font.PLAIN, 14));
        leaderboardTable.setRowHeight(35);
        leaderboardTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leaderboardTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        leaderboardTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        leaderboardTable.getTableHeader().setBackground(new Color(0, 123, 255));
        leaderboardTable.getTableHeader().setForeground(Color.WHITE);
        leaderboardTable.setGridColor(new Color(220, 220, 220));

        // Set column widths
        leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // Rank
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Player
        leaderboardTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Score
        leaderboardTable.getColumnModel().getColumn(3).setPreferredWidth(180); // Date

        // Center align rank and score columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        leaderboardTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Rank
        leaderboardTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Score

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // User stats label
        userStatsLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        userStatsLabel.setForeground(new Color(108, 117, 125));
        userStatsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        userStatsLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        centerPanel.add(userStatsLabel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ========== BOTTOM PANEL: Buttons ==========
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(new Color(248, 249, 250));

        // Refresh button
        refreshButton.setFont(new Font("Arial", Font.BOLD, 16));
        refreshButton.setPreferredSize(new Dimension(160, 45));
        refreshButton.setBackground(new Color(248, 249, 250));
        refreshButton.setForeground(new Color(220, 53, 69));
        refreshButton.setFocusPainted(false);
        refreshButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect for Refresh button
        refreshButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                refreshButton.setBackground(new Color(33, 136, 56));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                refreshButton.setBackground(new Color(40, 167, 69));
            }
        });

        // Back button
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setPreferredSize(new Dimension(160, 45));
        backButton.setBackground(new Color(248, 249, 250));
        backButton.setForeground(new Color(220, 53, 69));
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect for Back button
        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backButton.setBackground(new Color(33, 136, 56));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backButton.setBackground(new Color(40, 167, 69));
            }
        });

        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        getContentPane().add(mainPanel);

        // Initialize controller
        new LeaderboardController(this);
    }

    /**
     * Updates the leaderboard table with game session data
     * @param sessions List of game sessions (top 10)
     */
    public void updateLeaderboard(List<GameSession> sessions) {
        // Clear existing rows
        tableModel.setRowCount(0);

        // Add new rows
        int rank = 1;
        for (GameSession session : sessions) {
            Object[] rowData = {
                    rank++,
                    session.getUsername(),
                    session.getFinalScore(),
                    DATE_FORMATTER.format(session.getEndTime())
            };
            tableModel.addRow(rowData);
        }

        // Highlight current user's rows
        highlightCurrentUser();
    }

    /**
     * Highlights rows belonging to the current user
     */
    private void highlightCurrentUser() {
        leaderboardTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Get username from row
                String username = (String) table.getValueAt(row, 1);

                // Highlight if current user
                User currentUser = UserSession.getInstance().getCurrentUser();
                if (currentUser != null && username.equals(currentUser.getUsername())) {
                    c.setBackground(new Color(255, 243, 205)); // Light yellow
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setBackground(Color.WHITE);
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }

                // Center align rank and score
                if (column == 0 || column == 2) {
                    ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        });
    }

    /**
     * Updates the user statistics label
     * @param highScore User's highest score
     * @param totalGames Total games played by user
     */
    public void updateUserStats(int highScore, int totalGames) {
        userStatsLabel.setText(String.format(
                "Your Stats: High Score: %d | Games Played: %d",
                highScore, totalGames
        ));
    }

    /**
     * Gets the refresh button
     * @return The refresh button
     */
    public JButton getRefreshButton() {
        return refreshButton;
    }

    /**
     * Gets the back button
     * @return The back button
     */
    public JButton getBackButton() {
        return backButton;
    }
}