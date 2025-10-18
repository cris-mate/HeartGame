package com.heartgame.controller;

import com.heartgame.model.User;
import com.heartgame.persistence.UserRepository;
import com.heartgame.view.GameGUI;
import com.heartgame.view.LoginGUI;
import com.heartgame.event.GameEventType;
import com.heartgame.event.GameEventManager;

import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for handling user login
 * Coordinates between the LoginGUI view and UserRepository for authentication
 * Publishes PLAYER_LOGGED_IN event on successful login
 */
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final LoginGUI loginView;
    private final UserRepository userRepository;

    /**
     * Constructs a new LoginController
     * @param loginView The login view this controller manages
     * @param userRepository The repository for user data access
     */
    public LoginController(LoginGUI loginView, UserRepository userRepository) {
        this.loginView = loginView;
        this.userRepository = userRepository;
        initController();
    }

    /**
     * Initializes the controller by adding an action listener to the view's components
     */
    private void initController() {
        loginView.getLoginButton().addActionListener(e -> performLogin());
    }

    /**
     * Performs the login action validating user credentials:
     * On success, publishes a PLAYER_LOGGED_IN event before transitions to the game view.
     * On failure, it displays an error message to the user.
     */
    private void performLogin() {
        String username = loginView.getUsername();
        String password = loginView.getPassword();

        // validate input
        if (username == null || username.trim().isEmpty()) {
            loginView.showError("Please enter a username");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            loginView.showError("Please enter a password");
            return;
        }

        // authenticate through repository
        if (userRepository.authenticateUser(username, password)) {
            logger.info("User '{}' successfully authenticated", username);

            // update last login timestamp
            userRepository.updateLastLogin(username);

            // create User object and publish login event
            User user = new User(username);
            GameEventManager.getInstance().publish(GameEventType.PLAYER_LOGGED_IN, user);

            // transition to game view
            loginView.dispose();
            SwingUtilities.invokeLater(() -> new GameGUI(user).setVisible(true));

        } else {
            logger.warn("Failed login attempt for user '{}'", username);
            loginView.showError("Wrong username or password! Please try again");
            loginView.clearFields();
        }
    }
}

