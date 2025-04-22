package com.perisic.banana.peripherals;

import com.perisic.banana.engine.DBConfig;
import com.perisic.banana.engine.DBManager;
import com.perisic.banana.engine.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * HomeGUI class is the main dashboard interface of the Banana Game application.
 * It allows users to navigate through various functionalities including starting a game,
 * viewing achievements, accessing the Hall of Fame, switching accounts, updating or deleting
 * account details, and exiting the application.
 * This GUI is constructed using Java Swing components and is styled using the UIFactory class
 * for consistent visual design across the application.
 */
public class HomeGUI extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    private final DBManager dbManager;
    private final SessionManager session;
    private final String username;
    private final GameNavigator navigator;

    /**
     * Constructs a new HomeGUI window for the given user.
     *
     * @param username   the current logged-in user's username
     * @param dbManager  the database manager for handling account operations
     * @param session    the session manager for tracking user interaction
     */
    public HomeGUI(String username, DBManager dbManager, SessionManager session) {
        super("Six Equations Solver");

        this.username = username;
        this.dbManager = dbManager;
        this.session = session;
        this.navigator = new GameNavigator(username, dbManager, session);

        UIFactory.defaultFrame(this);
        buildComponents();
    }

    /**
     * Builds and assembles the main GUI layout with all sub-panels.
     */
    private void buildComponents() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIFactory.Colors.BACKGROUND);
        contentPanel.add(buildUserPanel());
        contentPanel.add(buildControlPanel());
        contentPanel.add(buildInfoPanel());
        contentPanel.add(buildUpdateAccountPanel());
        contentPanel.add(buildExitPanel());
        add(contentPanel);
    }

    /**
     * Builds a user panel showing information about the logged-in user.
     *
     * @return a JPanel displaying the username or relevant user info
     */
    private JPanel buildUserPanel() {
        return UIFactory.buildUserPanel(username);
    }

    /**
     * Builds a panel with control buttons to navigate key features like Play,
     * Hall of Fame, Milestones, and switching accounts.
     *
     * @return a JPanel with main navigation buttons
     */
    private JPanel buildControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        controlPanel.setBackground(UIFactory.Colors.BACKGROUND);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // creating control buttons
        controlPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.PLAY_GAME,
                "PLAY_GAME", e -> {
                    this.dispose();
                    session.playGame(navigator);
                }));
        controlPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.HALL_OF_FAME,
                "HALL_OF_FAME", e -> {
                    this.dispose();
                    navigator.openHallOfFame();
                }));
        controlPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.MILESTONES,
                "MILESTONES", e -> {
                    this.dispose();
                    navigator.openMilestones();
                }));
        controlPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.SWITCH_ACCOUNT,
                "SWITCH_ACCOUNT", e -> {
                    this.dispose();
                    session.switchAccount(dbManager);
                }));
        return controlPanel;
    }

    /**
     * Builds an informative panel introducing the game and its mechanics.
     *
     * @return a JPanel containing game description and welcome message
     */
    private JPanel buildInfoPanel() {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        infoPanel.setBackground(UIFactory.Colors.BACKGROUND);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(40, 10, 10, 10));

        JLabel titleLabel = UIFactory.Labels.label(
                "<html><div style='text-align: center;'>Welcome to the Banana Game!</div><br></html>",
                UIFactory.Fonts.TITLE.deriveFont(Font.BOLD, 22f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel textLabel = UIFactory.Labels.
                label(
                "<html>" +
                        "<div style='text-align: center; width: 600px;'>" +
                        "Get ready for a challenge with math-based puzzles!<br>" +
                        "The Six Equations Game — also known as Banana Game — tests your ability to find solutions " +
                        "to visual equations using logic, pattern recognition and basic arithmetic.<br><br>" +
                        "<b>How it works:</b><br><br>" +
                        "\uD83C\uDF4C Select the correct solution (from 0 to 9) using on-screen buttons.<br>" +
                        "\uD83C\uDF4C You have 180 seconds to solve as many equations as you can before time runs out.<br>" +
                        "\uD83C\uDF4C Solve as many equations as possible and unlock a variety of achievement milestones.<br>" +
                        "\uD83C\uDF4C Track your progress and aim to beat your highest score each session!<br><br>" +
                        "\uD83C\uDF4C Climb your way into the international <i>Hall of Fame</i> and show off your skills!<br><br>" +
                        "Click <b>Play Game</b> to begin your journey!" +
                        "</div>" +
                        "</html>"
                , UIFactory.Fonts.TEXT);
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(UIFactory.Colors.TEXT_PRIMARY);
        textLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);

        infoPanel.add(titleLabel);
        infoPanel.add(textLabel);
        return infoPanel;
    }

    /**
     * Builds a panel containing buttons for account update and deletion.
     *
     * @return a JPanel with update and delete account functionality
     */
    private JPanel buildUpdateAccountPanel() {
        JPanel updateAccountPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        updateAccountPanel.setBackground(UIFactory.Colors.BACKGROUND);
        updateAccountPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        updateAccountPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        updateAccountPanel.add(UIFactory.
                createButton(UIFactory.Buttons.ACC_Buttons.UPDATE_ACCOUNT,
                        "UPDATE_ACCOUNT", e -> {
                            this.dispose();
                            navigator.openUpdateAccount();
                        }));

        updateAccountPanel.add(UIFactory.
                createButton(UIFactory.Buttons.ACC_Buttons.DELETE_ACCOUNT,
                        "DELETE_ACCOUNT",
                        e -> dbManager.deleteAccount(username)));

        return updateAccountPanel;
    }

    /**
     * Builds the panel containing the exit button to close the application.
     *
     * @return a JPanel with the exit game button
     */
    private JPanel buildExitPanel() {
        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        exitPanel.setBackground(UIFactory.Colors.BACKGROUND);
        exitPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        exitPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        exitPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.EXIT_GAME,
                "EXIT_GAME", e -> {
                    this.dispose();
                    session.exitGame();
                }));
        return exitPanel;
    }

    /**
     * Entry point for launching the HomeGUI from a standalone context.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        DBConfig dbConfig = new DBConfig();
        DBManager dbManager = new DBManager(dbConfig.getConnection());
        SessionManager session = new SessionManager(dbConfig.getConnection());
        SwingUtilities.invokeLater(() -> new HomeGUI(session.getCurrentUser(), dbManager, session).setVisible(true));
    }
}
