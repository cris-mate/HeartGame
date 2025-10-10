package controller;

import model.User;
import view.GameGUI;
import view.LoginGUI;
import event.GameEventType;
import event.GameEventManager;

import javax.swing.*;

/*
 * Basic class, TODO:
 * link against external database.
 * signup mechanism to create account.
 * Encryption
 * Etc.
 */

/**
 * Controller for handling login logic
 * Publishes an event on successful login
 */
public class LoginController {

    private final LoginGUI loginView;

    /**
     * Constructs a new LoginController
     * @param loginView The login view it controls
     */
    public LoginController(LoginGUI loginView) {
        this.loginView = loginView;
        initController();
    }

    /**
     * Initializes the controller by adding an action listener to the login button
     */
    private void initController() {
        loginView.getLoginButton().addActionListener(e -> performLogin());
    }

    /**
     * Performs the login action. It retrieves user credentials from the view,
     * validates them, and either proceeds to the main game or shows an error message
     */
    private void performLogin() {
        String username = loginView.getUsername();
        String password = loginView.getPassword();

        if (checkPassword(username, password)) {
            User user = new User(username);
            GameEventManager.getInstance().publish(GameEventType.PLAYER_LOGGED_IN, user);
            loginView.dispose();
            // In a real application, you would pass the user object to the GameGUI
            SwingUtilities.invokeLater(() -> new GameGUI().setVisible(true));
        } else {
            JOptionPane.showMessageDialog(loginView, "Wrong username or password! Please try again");
            loginView.clearFields();
        }
    }

    /**
     * Checks if the provided password matches the username
     * Note: This is a placeholder for a real authentication service
     * @param username The username to check
     * @param passwd   The password to check
     * @return True if the credentials are valid, false otherwise
     */
    boolean checkPassword(String username, String passwd) {
        // Basic authentication logic, to be replaced with a proper authentication service
        return username.equals("Jo") && passwd.equals("hello25");
    }
}

