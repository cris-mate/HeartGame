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
    private final JTextField userField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JLabel userLabel = new JLabel("Username:");
    private final JLabel passwordLabel = new JLabel("Password:");
    private final JLabel orLabel = new JLabel("OR");

    /**
     * Constructs the login GUI, initializes all UI components,
     * and links this view to its controller
     */
    public LoginGUI() {
        super("Login Authentication");
        setSize(350, 300);
        setLocation(500, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        // Username label and field
        userLabel.setBounds(50, 30, 80, 20);
        userField.setBounds(140, 30, 150, 20);

        // Password label and field
        passwordLabel.setBounds(50, 65, 80, 20);
        passwordField.setBounds(140, 65, 150, 20);

        // Traditional login button
        loginButton.setBounds(105, 100, 140, 25);

        // OR separator
        orLabel.setBounds(155, 135, 40, 20);
        orLabel.setFont(new Font("Arial", Font.BOLD, 12));
        orLabel.setForeground(Color.GRAY);

        // Google OAuth button
        googleLoginButton.setBounds(75, 165, 200, 35);
        googleLoginButton.setBackground(new Color(66, 133, 244)); // Google blue
        googleLoginButton.setForeground(Color.WHITE);
        googleLoginButton.setFocusPainted(false);
        googleLoginButton.setFont(new Font("Arial", Font.BOLD, 12));

        // Add components to panel
        panel.add(userLabel);
        panel.add(userField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(orLabel);
        panel.add(googleLoginButton);

        getContentPane().add(panel);
        setVisible(true);

        // Initialize controller
        new LoginController(this);
    }

    /**
     * Gets the username entered by the user
     * @return The username as a String
     */
    public String getUsername() {
        return userField.getText();
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
        userField.setText("");
        passwordField.setText("");
        userField.requestFocus();
    }

    /**
     * Disables the login form while OAuth authentication is in progress
     */
    public void disableForm() {
        loginButton.setEnabled(false);
        googleLoginButton.setEnabled(false);
        userField.setEnabled(false);
        passwordField.setEnabled(false);
    }

    /**
     * Re-enables the login form after authentication attempt completes
     */
    public void enableForm() {
        loginButton.setEnabled(true);
        googleLoginButton.setEnabled(true);
        userField.setEnabled(true);
        passwordField.setEnabled(true);
    }

    /**
     * Shows a loading message during OAuth authentication
     * @param message The message to display
     */
    public void showLoadingMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Please Wait", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Main entry point to launch the login GUI
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginGUI::new);
    }
}
