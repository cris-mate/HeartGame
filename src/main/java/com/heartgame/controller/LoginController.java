package com.heartgame.controller;

import com.heartgame.model.User;
import com.heartgame.persistence.DatabaseManager;
import com.heartgame.view.GameGUI;
import com.heartgame.view.LoginGUI;
import com.heartgame.event.GameEventType;
import com.heartgame.event.GameEventManager;

import javax.swing.*;
import java.sql.*;
import java.time.Instant;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Basic class, TODO:
 * link against external database.
 * signup mechanism to create account.
 * Encryption
 * Etc.
 */

/**
 * Controller for handling login with database authentication
 * Publishes an event on successful login
 */
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final LoginGUI loginView;
    private final Connection dbConnection;

    /**
     * Constructs a new LoginController
     * @param loginView The login view it controls
     */
    public LoginController(LoginGUI loginView) {
        this.loginView = loginView;
        this.dbConnection = DatabaseManager.getInstance().getConnection();
        initController();
    }

    /**
     * Initializes the controller by adding an action listener to the login button
     */
    private void initController() {
        loginView.getLoginButton().addActionListener(e -> performLogin());
    }

    /**
     * Performs the login action validating user credentials:
     * On success, publishes a PLAYER_LOGGED_IN event before transitioning to the main game view.
     * On failure, it displays an error message.
     */
    private void performLogin() {
        String username = loginView.getUsername();
        String password = loginView.getPassword();

        if (checkPassword(username, password)) {
            logger.info("User '{}' successfully authenticated", username);
            updateLastLogin(username);
            User user = new User(username);
            GameEventManager.getInstance().publish(GameEventType.PLAYER_LOGGED_IN, user);
            loginView.dispose();
            // Pass the user object to the GameGUI
            SwingUtilities.invokeLater(() -> new GameGUI(user).setVisible(true));
        } else {
            logger.warn("Failed login attempt for user '{}'.", username);
            JOptionPane.showMessageDialog(loginView, "Wrong username or password! Please try again");
            loginView.clearFields();
        }
    }

    /**
     * Updates the last_login timestamp for the given user to the current time
     * @param username The username of the user to update
     */
    private void updateLastLogin(String username) {
        String sql = "UPDATE users SET last_login = ? WHERE username = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.from(Instant.now()));
            stmt.setString(2, username);
            stmt.executeUpdate();
            logger.info("Updated last_login for user '{}'.", username);
        } catch (SQLException e) {
            logger.error("Failed to update last_login for user '{}'", username, e);
        }
    }

    /**
     * Checks if the provided password matches the username
     * Note: This is a placeholder for a real authentication service
     * @param username  The username to check
     * @param password  The password to check
     * @return True if the credentials are valid, false otherwise
     */
    boolean checkPassword(String username, String password) {
        if (dbConnection == null) {
            logger.error("Cannot authenticate user. No database connection available.");
            return false;
        }

        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password_hash");
                return BCrypt.checkpw(password, hashedPassword);
            } else {
                return false; // User not found
            }
        } catch (SQLException e) {
            logger.error("Error during authentication for user '{}'", username, e);
            return false;
        }
    }
}

