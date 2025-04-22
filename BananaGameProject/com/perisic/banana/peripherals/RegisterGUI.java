package com.perisic.banana.peripherals;

import com.perisic.banana.engine.DBConfig;
import com.perisic.banana.engine.DBManager;
import com.perisic.banana.engine.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.Serial;
import java.sql.Connection;
import java.util.Arrays;

/**
 * The RegisterGUI class provides a graphical interface for user registration.
 * This class includes input fields for username, password, and email, and handles
 * validation and registration through the DBManager.
 * Upon successful registration, it redirects users to the LoginGUI.
 * It uses Swing for UI and a custom UIFactory for styled components.
 * Database connection is established using DBConfig, and session management
 * is handled via SessionManager.
 */
public class RegisterGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    private final JTextField usernameField = UIFactory.Fields.textField(15);
    private final JPasswordField passwordField = UIFactory.Fields.passwordField(15);
    private final JTextField emailField = UIFactory.Fields.textField(15);

    private DBManager dbManager;
    private SessionManager session;

    /**
     * Constructs a new RegisterGUI instance.
     * Initializes UI components, sets up database and session managers, and
     * handles connection errors. Displays a registration form for new users.
     */
    public RegisterGUI() {

        super("Authentication");

        DBConfig dbConfig = new DBConfig();
        Connection conn = dbConfig.getConnection();

        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Failed to connect to database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dbManager = new DBManager(conn);
        session = new SessionManager(conn);

        UIFactory.defaultFrame(this);

        // content panel with padding, background colour and vertical stacking
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(80, 80, 80, 80));
        contentPanel.setBackground(UIFactory.Colors.BACKGROUND);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // buttons
        RoundedButton registerButton = UIFactory.createButton(
                UIFactory.Buttons.NAV_Buttons.REGISTER,
                "REGISTER",
                e -> {
                    this.registerUser(e);
                    this.dispose();
                    new LoginGUI(session, dbManager).setVisible(true);
                }
        );

        RoundedButton cancelButton = UIFactory.createButton(
                UIFactory.Buttons.NAV_Buttons.CANCEL_REGISTRATION,
                "CANCEL",
                e -> {
                    this.dispose();
                    new LoginGUI(session, dbManager).setVisible(true);
                }
        );

        // greeting & title
        JLabel greetingLabel = UIFactory.Labels.label("Welcome to Banana Equation Game!",
                UIFactory.Fonts.GREETING);
        greetingLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);
        greetingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel titleLabel = UIFactory.Labels.label("Please Create New Account",
                UIFactory.Fonts.TITLE);
        titleLabel.setForeground(UIFactory.Colors.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // username panel
        JLabel usernameLabel = UIFactory.Labels.label("Username", UIFactory.Fonts.LABEL);
        usernameLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);
        usernamePanel.setBackground(UIFactory.Colors.BACKGROUND);

        // password panel
        JLabel passwordLabel = UIFactory.Labels.label("Password", UIFactory.Fonts.LABEL);
        passwordLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        passwordPanel.setBackground(UIFactory.Colors.BACKGROUND);

        // email panel
        JLabel emailLabel = UIFactory.Labels.label("Email", UIFactory.Fonts.LABEL);
        emailLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);
        JPanel emailPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
        emailPanel.add(emailLabel);
        emailPanel.add(emailField);
        emailPanel.setBackground(UIFactory.Colors.BACKGROUND);

        // registration panel
        JPanel registrationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        registrationPanel.add(registerButton);
        registrationPanel.setBackground(UIFactory.Colors.BACKGROUND);

        // cancel panel
        JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 30));
        cancelPanel.add(cancelButton);
        cancelPanel.setBackground(UIFactory.Colors.BACKGROUND);

        // add all content to frame
        contentPanel.add(greetingLabel);
        contentPanel.add(Box.createVerticalStrut(60));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(usernamePanel);
        contentPanel.add(passwordPanel);
        contentPanel.add(emailPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(registrationPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(cancelPanel);
        setContentPane(contentPanel);
    }

    /**
     * Handles the user registration process.
     * Validates user input fields, attempts to register a new user via DBManager,
     * and provides feedback via dialog boxes. Transitions to LoginGUI on success.
     *
     * @param e the ActionEvent triggered by the register button.
     */
    private void registerUser(ActionEvent e) {

        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars).trim();
        Arrays.fill(passwordChars, ' ');
        String email = emailField.getText().trim();

        if (!isValidInput(username, password, email)) {
            JOptionPane.showMessageDialog(this, "Invalid input! Please check all fields and try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isRegistered = dbManager.registerUser(username, password, email);
        if (isRegistered) {
            JOptionPane.showMessageDialog(this,
                    "Registration successful for " + username + "!\nPlease login to continue",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Registration failed,!\nUsername already in use",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            usernameField.setText("");
            passwordField.setText("");
            emailField.setText("");
            usernameField.requestFocus();
        }
        System.out.println("User registered: " + username);
    }

    /**
     * Validates the user input for registration.
     * Ensures that username, password, and email meet specified constraints and
     * the email format is valid.
     *
     * @param username the entered username
     * @param password the entered password
     * @param email    the entered email
     * @return true if all fields are valid; false otherwise
     */
    private boolean isValidInput(String username, String password, String email) {
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields");
            return false;
        }
        if (username.length() < 3 || username.length() > 15) {
            JOptionPane.showMessageDialog(this, "Please enter a username 3 to 15 characters.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
        if (password.length() < 8 || password.length() > 15) {
            JOptionPane.showMessageDialog(this, "Password must have 8 to 15 characters", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!email.matches("^[\\w\\-\\.]+@([\\w\\-]+\\.)+[\\w\\-]{2,4}$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Main method to launch the RegisterGUI as a standalone frame.
     *
     * @param args the command line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterGUI().setVisible(true));
    }
}
