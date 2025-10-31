package com.heartgame.view;

import com.heartgame.controller.LoginController;
import com.heartgame.event.GameEventManager;
import com.heartgame.event.GameEventType;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.Serial;

/**
 * The view for the login screen
 * Supports both traditional password login and Google OAuth 2.0
 * Uses event-driven navigation (publishes NAVIGATE_TO_REGISTER event)
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
        titleLabel.setForeground(new Color(220, 53, 69));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Username
        usernameLabel.setBounds(80, 75, 120, 20);
        usernameField.setBounds(160, 75, 170, 25);

        // Password
        passwordLabel.setBounds(80, 110, 120, 20);
        passwordField.setBounds(160, 110, 170, 25);

        // Login button
        loginButton.setBackground(new Color(230, 230, 230));
        loginButton.setForeground(new Color(0, 123, 255));
        loginButton.setBounds(150, 160, 100, 40);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        Border roundedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
        loginButton.setBorder(roundedBorder);
        loginButton.setOpaque(true);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (loginButton.isEnabled()) {
                    loginButton.setBackground(new Color(200, 200, 200));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (loginButton.isEnabled()) {
                    loginButton.setBackground(new Color(230, 230, 230));
                }
            }
        });

        // Separator line
        JSeparator separator = new JSeparator();
        separator.setBounds(80, 220, 250, 10);

        // Register button
        JLabel registerLabel = new JLabel("Don't have an account?");
        registerLabel.setBounds(120, 240, 170, 35);
        registerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        registerLabel.setForeground(Color.DARK_GRAY);

        registerButton.setBounds(130, 290, 140, 30);
        registerButton.setFont(new Font("Arial", Font.PLAIN, 14));
        registerButton.setBackground(new Color(230, 230, 230));
        registerButton.setForeground(new Color(0, 123, 255));
        registerButton.setBorder(roundedBorder);
        registerButton.setOpaque(true);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> openRegisterWindow());
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

        // OR separator
        JLabel orLabel = new JLabel("OR");
        orLabel.setBounds(180, 325, 40, 20);
        orLabel.setForeground(Color.GRAY);

        // Google OAuth button
        googleLoginButton.setBounds(100, 350, 200, 30);
        googleLoginButton.setFont(new Font("Arial", Font.PLAIN, 14));
        googleLoginButton.setBackground(new Color(230, 230, 230));
        googleLoginButton.setForeground(new Color(0, 123, 255));
        googleLoginButton.setBorder(roundedBorder);
        googleLoginButton.setOpaque(true);
        googleLoginButton.setFocusPainted(false);
        googleLoginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        googleLoginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (googleLoginButton.isEnabled()) {
                    googleLoginButton.setBackground(new Color(200, 200, 200));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (googleLoginButton.isEnabled()) {
                    googleLoginButton.setBackground(new Color(230, 230, 230));
                }
            }
        });

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
        panel.add(registerLabel);
        panel.add(registerButton);

        getContentPane().add(panel);
        setVisible(true);

        // Initialize controller
        new LoginController(this);
    }

    /**
     * Opens the registration window using navigation event
     */
    private void openRegisterWindow() {
        // Publish navigation event (event-driven approach)
        GameEventManager.getInstance().publish(GameEventType.NAVIGATE_TO_REGISTER, null);
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