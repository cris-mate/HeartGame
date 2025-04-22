package com.perisic.banana.peripherals;

import com.perisic.banana.engine.DBConfig;
import com.perisic.banana.engine.DBManager;
import com.perisic.banana.engine.SessionManager;

import javax.swing.*;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The LoginGUI class represents the login window of the Banana Equation Game.
 * It provides the user interface for user authentication including login,
 * registration redirection, and application exit.
 * It uses the SessionManager to validate user credentials and
 * launches the main game GUI upon successful login.
 * Dependencies include DBManager, SessionManager, and UI elements from UIFactory.
 */
public class LoginGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    private final SessionManager session;
    private final DBManager dbManager;
    String currentUser;

    private final JTextField usernameField = UIFactory.Fields.textField(15);
    private final JPasswordField passwordField = UIFactory.Fields.passwordField(15);
    private final RoundedButton loginButton =
            UIFactory.createButton(UIFactory.Buttons.NAV_Buttons.LOGIN, "LOGIN", e -> {});
    private final RoundedButton registerButton =
            UIFactory.createButton(UIFactory.Buttons.ACC_Buttons.CREATE_ACCOUNT, "CREATE_ACCOUNT", e -> {});
    private final RoundedButton exitButton =
            UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.EXIT_GAME, "EXIT_GAME", e -> {});

    /**
     * Constructs the Login GUI window.
     *
     * @param session the session manager for handling login and session states
     * @param dbManager the database manager for accessing user data
     */
    public LoginGUI(SessionManager session, DBManager dbManager) {
        super("Authentication");

        this.session = session;
        this.dbManager = dbManager;

        initUI();
        registerActions();
    }

    /**
     * Initializes and lays out the UI components of the login screen.
     */
    private void initUI() {

        UIFactory.defaultFrame(this);

        // content panel with padding, background colour and vertical stacking
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(80, 80, 80, 80));
        contentPanel.setBackground(UIFactory.Colors.BACKGROUND);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = UIFactory.Labels.label("Welcome to Banana Equation Game!",
                UIFactory.Fonts.GREETING);
        titleLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = UIFactory.Labels.label("Please authenticate",
                UIFactory.Fonts.TITLE);
        subtitleLabel.setForeground(UIFactory.Colors.TEXT_PRIMARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel usernameLabel = UIFactory.Labels.label("Username:", UIFactory.Fonts.LABEL);
        usernameLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        userPanel.add(usernameLabel);
        userPanel.add(usernameField);
        userPanel.setBackground(UIFactory.Colors.BACKGROUND);

        JLabel passwordLabel = UIFactory.Labels.label("Password:", UIFactory.Fonts.LABEL);
        passwordLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);
        JPanel passPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        passPanel.add(passwordLabel);
        passPanel.add(passwordField);
        passPanel.setBackground(UIFactory.Colors.BACKGROUND);

        // authentication panel
        JPanel authenticationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        authenticationPanel.add(loginButton);
        authenticationPanel.add(registerButton);
        authenticationPanel.setBackground(UIFactory.Colors.BACKGROUND);

        // exit panel
        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 30));
        exitPanel.setBackground(UIFactory.Colors.BACKGROUND);
        exitPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.EXIT_GAME,
                "EXIT_GAME",
                e -> {
                        this.dispose();
                        session.exitGame();
                }));

        // add all content to frame
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(60));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(userPanel);
        contentPanel.add(passPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(authenticationPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(exitPanel);

        setContentPane(contentPanel);
        getRootPane().setDefaultButton(loginButton);
        setVisible(true);
    }

    /**
     * Registers event listeners for the login, register, and exit buttons.
     * Handles login validation, navigation to register screen, and exiting the application.
     */
    public void registerActions() {
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            try {
                if (session.validateLogin(username, password)) {
                    session.setCurrentUser(username);
                    session.loginUser(username);
                    new GameGUI(username, dbManager, session).setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        "Login failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Database error: ", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(ae -> {
            RegisterGUI registerGUI = new RegisterGUI();
            registerGUI.setVisible(true);
            dispose();
        });

        exitButton.addActionListener(e -> {
            if (dbManager != null && currentUser != null) {
                session.logoutUser(currentUser);
            }
            System.exit(0);
        });
    }

    /**
     * The main entry point to launch the Login GUI independently.
     * Initializes the DBConfig, SessionManager, and DBManager.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        DBConfig config = new DBConfig();
        Connection connection = config.getConnection();

        SessionManager session = new SessionManager(connection);
        DBManager dbManager = new DBManager(connection);
        SwingUtilities.invokeLater(() -> new LoginGUI(session, dbManager).setVisible(true));
    }
}

