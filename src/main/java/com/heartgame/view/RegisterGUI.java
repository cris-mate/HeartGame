package com.heartgame.view;

import com.heartgame.controller.RegisterController;
import com.heartgame.event.GameEventManager;
import com.heartgame.event.GameEventType;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.Serial;

/**
 * The view for the registration screen
 * Allows new users to create an account with username, email, and password
 * Uses event-driven navigation (publishes NAVIGATE_TO_LOGIN event)
 */
public class RegisterGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = -3847562190845701L;

    private final JButton registerButton = new JButton("Create Account");
    private final JButton backToLoginButton = new JButton("Back to Login");
    private final JTextField usernameField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JPasswordField confirmPasswordField = new JPasswordField(15);

    private final JLabel titleLabel = new JLabel("Create New Account");
    private final JLabel usernameLabel = new JLabel("Username:");
    private final JLabel emailLabel = new JLabel("Email:");
    private final JLabel passwordLabel = new JLabel("Password:");
    private final JLabel confirmPasswordLabel = new JLabel("Confirm Password:");

    /**
     * Constructs the registration GUI, initializes all UI components,
     * and links this view to its controller
     */
    public RegisterGUI() {
        super("Register - HeartGame");
        setSize(400, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        // Title
        titleLabel.setBounds(100, 20, 200, 25);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(220, 53, 69));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Username
        usernameLabel.setBounds(50, 75, 120, 20);
        usernameField.setBounds(180, 75, 170, 25);

        // Email
        emailLabel.setBounds(50, 110, 120, 20);
        emailField.setBounds(180, 110, 170, 25);

        // Password
        passwordLabel.setBounds(50, 145, 120, 20);
        passwordField.setBounds(180, 145, 170, 25);

        // Confirm Password
        confirmPasswordLabel.setBounds(50, 180, 120, 20);
        confirmPasswordField.setBounds(180, 180, 170, 25);

        // Register button
        registerButton.setBounds(120, 250, 160, 30);
        registerButton.setBackground(new Color(230, 230, 230));
        registerButton.setForeground(new Color(0, 123, 255));
        Border roundedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
        registerButton.setBorder(roundedBorder);
        registerButton.setOpaque(true);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (registerButton.isEnabled()) {
                    registerButton.setBackground(new Color(200, 200, 200));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (registerButton.isEnabled()) {
                    registerButton.setBackground(new Color(230, 230, 230));
                }
            }
        });

        // Back to Login button
        backToLoginButton.setBounds(120, 285, 160, 30);
        backToLoginButton.setBackground(new Color(230, 230, 230));
        backToLoginButton.setForeground(new Color(0, 123, 255));
        backToLoginButton.setBorder(roundedBorder);
        backToLoginButton.setOpaque(true);
        backToLoginButton.setFocusPainted(false);
        backToLoginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToLoginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (backToLoginButton.isEnabled()) {
                    backToLoginButton.setBackground(new Color(200, 200, 200));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (backToLoginButton.isEnabled()) {
                    backToLoginButton.setBackground(new Color(230, 230, 230));
                }
            }
        });

        // Add hint text
        JLabel hintLabel = new JLabel("All fields are required");
        hintLabel.setBounds(50, 340, 300, 20);
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        hintLabel.setForeground(Color.GRAY);

        // Add all components to panel
        panel.add(titleLabel);
        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(confirmPasswordLabel);
        panel.add(confirmPasswordField);
        panel.add(registerButton);
        panel.add(backToLoginButton);
        panel.add(hintLabel);

        getContentPane().add(panel);
        setVisible(true);

        // Initialize controller
        new RegisterController(this);
    }

    /**
     * Gets the username entered by the user
     * @return The username as a String
     */
    public String getUsername() {
        return usernameField.getText().trim();
    }

    /**
     * Gets the email entered by the user
     * @return The email as a String
     */
    public String getEmail() {
        return emailField.getText().trim();
    }

    /**
     * Gets the password entered by the user
     * @return The password as a String
     */
    public String getPassword() {
        return String.valueOf(passwordField.getPassword());
    }

    /**
     * Gets the confirmation password entered by the user
     * @return The confirmation password as a String
     */
    public String getConfirmPassword() {
        return String.valueOf(confirmPasswordField.getPassword());
    }

    /**
     * Returns the register button component
     * @return The register JButton
     */
    public JButton getRegisterButton() {
        return registerButton;
    }

    /**
     * Returns the back to login button component
     * @return The back to login JButton
     */
    public JButton getBackToLoginButton() {
        return backToLoginButton;
    }

    /**
     * Clears all input fields
     */
    public void clearFields() {
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        usernameField.requestFocus();
    }

    /**
     * Disables the registration form while processing
     */
    public void disableForm() {
        registerButton.setEnabled(false);
        backToLoginButton.setEnabled(false);
        usernameField.setEnabled(false);
        emailField.setEnabled(false);
        passwordField.setEnabled(false);
        confirmPasswordField.setEnabled(false);
    }

    /**
     * Re-enables the registration form after processing
     */
    public void enableForm() {
        registerButton.setEnabled(true);
        backToLoginButton.setEnabled(true);
        usernameField.setEnabled(true);
        emailField.setEnabled(true);
        passwordField.setEnabled(true);
        confirmPasswordField.setEnabled(true);
    }

    /**
     * Main entry point to launch the registration GUI
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegisterGUI::new);
    }
}