package com.heartgame.controller;

import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.persistence.UserDAO;
import com.heartgame.service.GoogleAuthService;
import com.heartgame.view.GameGUI;
import com.heartgame.view.LoginGUI;
import com.heartgame.event.GameEventType;
import com.heartgame.event.GameEventManager;

import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for handling login with both password-based and OAuth authentication
 * Publishes an event on successful login
 */
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final LoginGUI loginView;
    private final UserDAO userDAO;
    private final GoogleAuthService googleAuthService;

    /**
     * Constructs a new LoginController
     * @param loginView The login view it controls
     */
    public LoginController(LoginGUI loginView) {
        this.loginView = loginView;
        this.userDAO = new UserDAO();
        this.googleAuthService = new GoogleAuthService();
        initController();
    }

    /**
     * Initializes the controller by adding action listeners to the login buttons
     */
    private void initController() {
        loginView.getLoginButton().addActionListener(e -> performPasswordLogin());
        loginView.getGoogleLoginButton().addActionListener(e -> performGoogleLogin());
    }

    /**
     * Performs traditional password-based login
     * On success, publishes a PLAYER_LOGGED_IN event before transitioning to the main game view
     * On failure, displays an error message
     */
    private void performPasswordLogin() {
        String username = loginView.getUsername();
        String password = loginView.getPassword();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(loginView, "Please enter both username and password");
            return;
        }

        if (userDAO.verifyPassword(username, password)) {
            logger.info("User '{}' successfully authenticated via password", username);

            // Fetch full user details from database
            userDAO.findByUsername(username).ifPresent(user -> {
                userDAO.updateLastLogin(username);
                loginSuccessful(user);
            });
        } else {
            logger.warn("Failed password login attempt for user '{}'", username);
            JOptionPane.showMessageDialog(loginView, "Wrong username or password! Please try again");
            loginView.clearFields();
        }
    }

    /**
     * Performs Google OAuth 2.0 login
     * Initiates OAuth flow, retrieves user info, creates/updates user in database
     */
    private void performGoogleLogin() {
        // Disable form during OAuth process
        loginView.disableForm();

        // Run OAuth in separate thread to prevent UI blocking
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                logger.info("Initiating Google OAuth 2.0 flow...");
                return googleAuthService.authenticateUser();
            }

            @Override
            protected void done() {
                try {
                    User googleUser = get();

                    if (googleUser != null) {
                        handleGoogleUser(googleUser);
                    } else {
                        logger.error("Google OAuth authentication failed");
                        JOptionPane.showMessageDialog(loginView,
                                "Google authentication failed. Please try again.",
                                "Authentication Error",
                                JOptionPane.ERROR_MESSAGE);
                        loginView.enableForm();
                    }
                } catch (Exception e) {
                    logger.error("Error during Google OAuth", e);
                    JOptionPane.showMessageDialog(loginView,
                            "An error occurred during Google authentication: " + e.getMessage(),
                            "Authentication Error",
                            JOptionPane.ERROR_MESSAGE);
                    loginView.enableForm();
                }
            }
        };

        worker.execute();
    }

    /**
     * Handles a user authenticated via Google OAuth
     * Creates new user if not exists, or updates existing user
     * @param googleUser User object from Google OAuth
     */
    private void handleGoogleUser(User googleUser) {
        // Check if user already exists by OAuth ID
        var existingUser = userDAO.findByOAuthId("google", googleUser.getOauthId());

        if (existingUser.isPresent()) {
            // Existing Google user - login
            User user = existingUser.get();
            logger.info("Existing Google user '{}' logged in", user.getUsername());
            userDAO.updateLastLogin(user.getUsername());
            loginSuccessful(user);
        } else {
            // New Google user - check if username is taken
            String originalUsername = googleUser.getUsername();
            String username = originalUsername;
            int suffix = 1;

            while (userDAO.usernameExists(username)) {
                username = originalUsername + suffix;
                suffix++;
            }

            // Update username if it was modified
            if (!username.equals(originalUsername)) {
                googleUser = new User(
                        username,
                        googleUser.getEmail(),
                        googleUser.getOauthProvider(),
                        googleUser.getOauthId()
                );
            }

            // Create new user in database
            if (userDAO.createUser(googleUser, null)) {
                logger.info("New Google user '{}' created and logged in", googleUser.getUsername());
                loginSuccessful(googleUser);
            } else {
                logger.error("Failed to create Google user in database");
                JOptionPane.showMessageDialog(loginView,
                        "Failed to create user account. Please try again.",
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                loginView.enableForm();
            }
        }
    }

    /**
     * Common method for successful login (password or OAuth)
     * Stores user in session, publishes event, and transitions to game view
     * @param user The authenticated user
     */
    private void loginSuccessful(User user) {
        // Store user in session
        UserSession.getInstance().login(user);

        // Publish login event
        GameEventManager.getInstance().publish(GameEventType.PLAYER_LOGGED_IN, user);

        // Close login window
        loginView.dispose();

        // Open game window
        SwingUtilities.invokeLater(() -> new GameGUI().setVisible(true));
    }
}
