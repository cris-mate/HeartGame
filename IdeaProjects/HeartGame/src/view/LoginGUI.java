package view;

import controller.LoginController;

import javax.swing.*;
import java.io.Serial;

/**
 * The view for the login screen
 */
public class LoginGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = -6921462126880570161L;

    private final JButton loginButton = new JButton("Login");
    private final JTextField userField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);

    /**
     * Constructs the login GUI, initializes all UI components,
     * and links this view to its controller
     */
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

    /**
     * Gets the username entered by the user
     * @return The username as a String
     */
    public String getUsername() { return userField.getText(); }

    /**
     * Gets the password entered by the user
     * @return The password as a String
     */
    public String getPassword() { return String.valueOf(passwordField.getPassword()); }

    /**
     * Returns the login button component
     * @return The login JButton
     */
    public JButton getLoginButton() { return loginButton; }

    /**
     * Clears the username and password fields and sets focus on the username field
     */
    public void clearFields() {
        userField.setText("");
        passwordField.setText("");
        userField.requestFocus();
    }

    /**
     * Main entry point to launch the login GUI
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginGUI::new);
    }
}
