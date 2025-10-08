package controller;

import model.User;
import view.GameGUI;
import view.LoginGUI;

import javax.swing.*;

/**
 * Basic class. TODO:
 * link against external database.
 * signup mechanism to create account.
 * Encryption
 * Etc.
 */

/**
 * Controller for handling login logic.
 */
public class LoginController {

    private final LoginGUI loginView;


    /**
     * Constructs a new LoginController
     * @param loginView The login view it controls.
     */
    public LoginController(LoginGUI loginView) {
        this.loginView = loginView;
        initController();
    }

    private void initController() {
        loginView.getLoginButton().addActionListener(e -> performLogin());
    }

    private void performLogin() {
        String username = loginView.getUsername();
        String password = loginView.getPassword();

        if (checkPassword(username, password)) {
            User user = new User(username);
            loginView.dispose();
            // In a real application, you would pass the user object to the GameGUI
            SwingUtilities.invokeLater(() -> new GameGUI().setVisible(true));
        } else {
            JOptionPane.showMessageDialog(loginView, "Wrong username or password! Please try again");
            loginView.clearFields();
        }
    }

    boolean checkPassword(String username, String passwd) {
        // Basic authentication logic, to be replaced with a proper authentication service
        return username.equals("Jo") && passwd.equals("hello25");
    }
}

