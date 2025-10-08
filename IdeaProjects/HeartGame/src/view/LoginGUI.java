package view;
/*
 * Code adapted from https://best-programming-tricks.blogspot.com/2011/07/how-to-make-login-form-with-java-gui.html
 */

import controller.LoginController;

import javax.swing.*;
import java.io.Serial;

/**
 * The view for the login screen.
 */
public class LoginGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = -6921462126880570161L;

    private final JButton loginButton = new JButton("Login");
    private final JTextField userField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);

    LoginGUI() {
        super("Login Authentication");
        setSize(300, 200);
        setLocation(500, 280);
        JPanel panel = new JPanel();
        panel.setLayout(null);

        userField.setBounds(70, 30, 150, 20);
        passwordField.setBounds(70, 65, 150, 20);
        loginButton.setBounds(110, 100, 80, 20);

        panel.add(loginButton);
        panel.add(userField);
        panel.add(passwordField);

        getContentPane().add(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        new LoginController(this);
    }

    public String getUsername() { return userField.getText(); }

    public String getPassword() { return String.valueOf(passwordField.getPassword()); }

    public JButton getLoginButton() { return loginButton; }

    public void clearFields() {
        userField.setText("");
        passwordField.setText("");
        userField.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginGUI::new);
    }
}
