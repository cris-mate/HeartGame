package com.heartgame.controller;

import com.heartgame.model.User;
import com.heartgame.persistence.UserDAO;
import com.heartgame.view.LoginGUI;
import com.heartgame.view.RegisterGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.regex.Pattern;

/**
 * Controller for handling user registration
 * Validates input, creates new users in the database, and manages navigation
 */
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MIN_USERNAME_LENGTH = 3;

    private final RegisterGUI registerView;
    private final UserDAO userDAO;

    /**
     * Constructs a new RegisterController
     * @param registerView The registration view it controls
     */
    public RegisterController(RegisterGUI registerView) {
        this.registerView = registerView;
        this.userDAO = new UserDAO();
        initController();
    }

    /**
     * Initializes the controller by adding action listeners to the buttons
     */
    private void initController() {
        registerView.getRegisterButton().addActionListener(e -> performRegistration());
        registerView.getBackToLoginButton().addActionListener(e -> navigateToLogin());
    }

    /**
     * Performs user registration with validation
     * Creates a new user account if all validations pass
     */
    private void performRegistration() {
        String username = registerView.getUsername();
        String email = registerView.getEmail();
        String password = registerView.getPassword();
        String confirmPassword = registerView.getConfirmPassword();

        // Validate inputs
        String validationError = validateInput(username, email, password, confirmPassword);
        if (validationError != null) {
            JOptionPane.showMessageDialog(
                    registerView,
                    validationError,
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Check if username already exists
        if (userDAO.usernameExists(username)) {
            logger.warn("Registration failed: Username '{}' already exists", username);
            JOptionPane.showMessageDialog(
                    registerView,
                    "Username '" + username + "' is already taken. Please choose another.",
                    "Username Unavailable",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Create new user with username as display name
        User newUser = new User(username, email, "password", null);

        registerView.disableForm();

        if (userDAO.createUser(newUser, password)) {
            logger.info("User '{}' registered successfully", username);
            JOptionPane.showMessageDialog(
                    registerView,
                    "Account created successfully!\nYou can now login.",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Navigate back to login
            navigateToLogin();
        } else {
            logger.error("Failed to create user '{}' in database", username);
            JOptionPane.showMessageDialog(
                    registerView,
                    "Failed to create account. Please try again.",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE
            );
            registerView.enableForm();
        }
    }

    /**
     * Validates registration input fields
     * @return Error message if validation fails, null if all validations pass
     */
    private String validateInput(String username, String email, String password, String confirmPassword) {
        // Check for empty required fields
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            return "All fields are required except Display Name.";
        }

        // Validate username length
        if (username.length() < MIN_USERNAME_LENGTH) {
            return "Username must be at least " + MIN_USERNAME_LENGTH + " characters long.";
        }

        // Validate username format (alphanumeric and underscore only)
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return "Username can only contain letters, numbers, and underscores.";
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Please enter a valid email address.";
        }

        // Validate password length
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.";
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }

        return null; // All validations passed
    }

    /**
     * Closes the registration screen and navigates to the login screen
     */
    private void navigateToLogin() {
        registerView.dispose();
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}
