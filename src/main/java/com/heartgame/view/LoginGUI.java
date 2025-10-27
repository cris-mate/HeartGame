package com.heartgame.view;

import com.heartgame.controller.LoginController;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * The view for the login screen
 * Supports both traditional password login and Google OAuth 2.0
 */
public class LoginGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = -6921462126880570161L;

    private final JButton loginButton = new JButton("Login");
    private final JButton googleLoginButton = new JButton("Sign in with Google");
    private final JLabel usernameLabel = new JLabel("Username:");
    private final JTextField usernameField = new JTextField(15);
    private final JLabel passwordLabel = new JLabel("Password:");
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JLabel titleLabel = new JLabel("Login into Heart Game");
    private final JButton registerButton = new JButton("Create Account");

    /**
     * Constructs the login GUI, initializes all UI components,
     * and links this view to its controller
     */
    public LoginGUI() {
        super("Login - HeartGame");
        setSize(400, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        // Title
        titleLabel.setBounds(100, 20, 200, 25);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Username
        usernameLabel.setBounds(80, 75, 120, 20);
        usernameField.setBounds(160, 75, 170, 25);

        // Password
        passwordLabel.setBounds(80, 110, 120, 20);
        passwordField.setBounds(160, 110, 170, 25);

        // Login button
        loginButton.setBounds(150, 160, 100, 40);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));

        // Separator line
        JSeparator separator = new JSeparator();
        separator.setBounds(80, 220, 250, 10);

        // Register button
        JLabel newUserLabel = new JLabel("Don't have an account?");
        newUserLabel.setBounds(120, 240, 170, 35);
        newUserLabel.setFont(new Font("Arial", Font.BOLD, 14));
        newUserLabel.setForeground(Color.DARK_GRAY);

        registerButton.setBounds(130, 290, 140, 30);
        registerButton.setFont(new Font("Arial", Font.PLAIN, 14));
        registerButton.setBackground(new Color(108, 117, 125)); // Gray
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(e -> openRegisterWindow());

        // OR separator
        JLabel orLabel = new JLabel("OR");
        orLabel.setBounds(180, 325, 40, 20);
        orLabel.setFont(new Font("Arial", Font.BOLD, 12));
        orLabel.setForeground(Color.GRAY);

        // Google OAuth button
        googleLoginButton.setBounds(100, 350, 200, 30);
        googleLoginButton.setFont(new Font("Arial", Font.PLAIN, 14));
        googleLoginButton.setBackground(new Color(66, 133, 244)); // Google blue
        googleLoginButton.setFocusPainted(false);

        // Add components to panel
        panel.add(titleLabel);
        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(orLabel);
        panel.add(googleLoginButton);
        panel.add(separator);
        panel.add(newUserLabel);
        panel.add(registerButton);

        getContentPane().add(panel);
        setVisible(true);

        // Initialize controller
        new LoginController(this);
    }

    /**
     * Opens the registration window and closes the login window
     */
    private void openRegisterWindow() {
        this.dispose();
        SwingUtilities.invokeLater(() -> new RegisterGUI().setVisible(true));
    }


    /**
     * Gets the username entered by the user
     * @return The username as a String
     */
    public String getUsername() {
        return usernameField.getText();
    }

    /**
     * Gets the password entered by the user
     * @return The password as a String
     */
    public String getPassword() {
        return String.valueOf(passwordField.getPassword());
    }

    /**
     * Returns the traditional login button component
     * @return The login JButton
     */
    public JButton getLoginButton() {
        return loginButton;
    }

    /**
     * Returns the Google OAuth login button component
     * @return The Google login JButton
     */
    public JButton getGoogleLoginButton() {
        return googleLoginButton;
    }

    /**
     * Clears the username and password fields and sets focus on the username field
     */
    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocus();
    }

    /**
     * Disables the login form while OAuth authentication is in progress
     */
    public void disableForm() {
        loginButton.setEnabled(false);
        googleLoginButton.setEnabled(false);
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
    }

    /**
     * Re-enables the login form after authentication attempt completes
     */
    public void enableForm() {
        loginButton.setEnabled(true);
        googleLoginButton.setEnabled(true);
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
    }

    /**
     * Main entry point to launch the login GUI
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginGUI::new);
    }
}
