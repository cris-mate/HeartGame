package com.heartgame.view;

import com.heartgame.controller.LeaderboardController;
import com.heartgame.model.GameSession;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.service.AvatarService;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The leaderboard view displaying top 10 scores
 * Shows rank, avatar, username, score, and date in a clean table format
 * Avatars are loaded asynchronously with caching
 */
public class LeaderboardGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 8273645192837465L;

    private final JButton backButton = new JButton("Back to Home");
    private final JButton refreshButton = new JButton("Refresh");
    private final JTable leaderboardTable;
    private final DefaultTableModel tableModel;
    private final JLabel userStatsLabel = new JLabel();
    private final Map<String, ImageIcon> avatarCache = new HashMap<>();
    private final AvatarService avatarService = new AvatarService();

    private static final String[] COLUMN_NAMES = {"Rank", "Avatar", "Player", "Score", "Date"};
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM, yyyy HH:mm").withZone(ZoneId.systemDefault());

    /**
     * Constructs the leaderboard GUI
     * Uses UserSession to access the current authenticated user
     */
    public LeaderboardGUI() {
        super("Leaderboard - HeartGame");

        setSize(900, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        mainPanel.setBackground(new Color(240, 240, 240));

        // ========== TOP PANEL: Title ==========
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 240, 240));

        JLabel titleLabel = new JLabel("Heart Game: Top 10 Leaderboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(220, 53, 69));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ========== CENTER PANEL: Leaderboard Table ==========
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                BorderFactory.createEmptyBorder(20, 40, 20, 40)
        ));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setOpaque(true);

        // Create table model
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) { // Avatar column
                    return ImageIcon.class;
                }
                return Object.class;
            }
        };

        // Create table
        leaderboardTable = new JTable(tableModel);
        leaderboardTable.setFont(new Font("Arial", Font.PLAIN, 14));
        leaderboardTable.setRowHeight(40); // Increased for avatars
        leaderboardTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leaderboardTable.setGridColor(new Color(210, 210, 210));

        // --- Header Styling ---
        JTableHeader header = leaderboardTable.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setBackground(new Color(0, 123, 255));
        header.setForeground(new Color(240, 240, 240));
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setVerticalAlignment(SwingConstants.CENTER);
        renderer.setBorder(BorderFactory.createCompoundBorder(
                renderer.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Set column widths
        leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // Rank
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(50);  // Avatar
        leaderboardTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Player
        leaderboardTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Score
        leaderboardTable.getColumnModel().getColumn(4).setPreferredWidth(200); // Date

        // Center align rank and score columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        leaderboardTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Rank
        leaderboardTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Player
        leaderboardTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Score
        leaderboardTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Date

        // Avatar column renderer (center-aligned)
        leaderboardTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel label = new JLabel();
                label.setHorizontalAlignment(SwingConstants.CENTER);
                if (value instanceof ImageIcon) {
                    label.setIcon((ImageIcon) value);
                } else {
                    label.setText("...");
                }
                return label;
            }
        });

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

        // --- Control Button Styling ---
        Color controlBackground = new Color(230, 230, 230);
        Color controlHover = new Color(200, 200, 200);
        Color controlForeground = new Color(0, 123, 255);
        Color controlBorder = Color.LIGHT_GRAY;
        Font controlFont = new Font("Arial", Font.BOLD, 16);
        Border roundedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(controlBorder, 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Refresh button
        refreshButton.setBackground(controlBackground);
        refreshButton.setForeground(controlForeground);
        refreshButton.setFont(controlFont);
        refreshButton.setPreferredSize(new Dimension(150, 45));
        refreshButton.setBorder(roundedBorder);
        refreshButton.setOpaque(true);
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (refreshButton.isEnabled()) {
                    refreshButton.setBackground(controlHover);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (refreshButton.isEnabled()) {
                    refreshButton.setBackground(controlBackground);
                }
            }
        });

        // Back button
        backButton.setBackground(controlBackground);
        backButton.setForeground(controlForeground);
        backButton.setFont(controlFont);
        backButton.setPreferredSize(new Dimension(150, 45));
        backButton.setBorder(roundedBorder);
        backButton.setOpaque(true);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (backButton.isEnabled()) {
                    backButton.setBackground(controlHover);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (backButton.isEnabled()) {
                    backButton.setBackground(controlBackground);
                }
            }
        });

        bottomPanel.add(refreshButton);
        bottomPanel.add(backButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);

        // Initialize controller
        new LeaderboardController(this);
    }

    /**
     * Updates the leaderboard table with game session data
     * Loads avatars asynchronously for each player
     * @param sessions List of game sessions (top 10)
     */
    public void updateLeaderboard(List<GameSession> sessions) {
        // Clear existing rows
        tableModel.setRowCount(0);

        // Add new rows with placeholder avatars
        int rank = 1;
        for (GameSession session : sessions) {
            Object[] rowData = {
                    rank++,
                    createPlaceholderIcon(session.getUsername()), // Placeholder initially
                    session.getUsername(),
                    session.getFinalScore(),
                    DATE_FORMATTER.format(session.getEndTime())
            };
            tableModel.addRow(rowData);

            // Load avatar asynchronously
            int finalRow = rank - 2; // Current row index
            loadAvatarAsync(session.getUsername(), finalRow);
        }

        // Highlight current user's rows
        highlightCurrentUser();
    }

    /**
     * Creates a placeholder icon with the first letter of the username
     * @param username The username
     * @return ImageIcon with first letter
     */
    private ImageIcon createPlaceholderIcon(String username) {
        BufferedImage placeholder = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(new Color(0, 123, 255));
        g2d.fillRect(0, 0, 32, 32);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String initial = username.substring(0, 1).toUpperCase();
        FontMetrics fm = g2d.getFontMetrics();
        int x = (32 - fm.stringWidth(initial)) / 2;
        int y = ((32 - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(initial, x, y);
        g2d.dispose();
        return new ImageIcon(placeholder);
    }

    /**
     * Loads avatar asynchronously and updates the table when complete
     * Uses caching to avoid redundant fetches
     * @param username The username to fetch avatar for
     * @param row The table row to update
     */
    private void loadAvatarAsync(String username, int row) {
        // Check cache first
        if (avatarCache.containsKey(username)) {
            tableModel.setValueAt(avatarCache.get(username), row, 1);
            return;
        }

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                BufferedImage avatar = avatarService.fetchAvatar(username);
                if (avatar != null) {
                    // Scale to table size
                    Image scaled = avatar.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        // Cache and update
                        avatarCache.put(username, icon);
                        if (row < tableModel.getRowCount()) {
                            tableModel.setValueAt(icon, row, 1);
                        }
                    }
                } catch (Exception e) {
                    // Keep placeholder on error
                }
            }
        };

        worker.execute();
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
                String username = (String) table.getValueAt(row, 2);

                // Highlight if current user
                User currentUser = UserSession.getInstance().getCurrentUser();
                if (currentUser != null && username.equals(currentUser.getUsername())) {
                    c.setBackground(new Color(255, 243, 205)); // Light yellow
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setBackground(Color.WHITE);
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }

                // Center align columns (except avatar)
                if (column != 1) {
                    ((DefaultTableCellRenderer) c).setHorizontalAlignment(SwingConstants.CENTER);
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