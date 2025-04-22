package com.perisic.banana.peripherals;

import com.perisic.banana.engine.DBManager;
import com.perisic.banana.engine.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * A Swing-based GUI window that allows users to update their Banana Game account credentials,
 * including their password and email address. It provides a clean and user-friendly layout
 * and handles update submission, navigation back to the home screen, or canceling updates.
 * The class supports partial updates—users may leave either field blank to retain the current setting.
 * On a successful update, the user is returned to the login screen where he needs to authenticate.
 */
public class UpdateAccount extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    private final DBManager dbManager;
    private final SessionManager session;
    private final String username;
    private final GameNavigator navigator;

    private JPasswordField passwordField;
    private JTextField emailField;

    /**
     * Constructs the update account UI for a specific user session.
     *
     * @param username the username of the logged-in user
     * @param dbManager the database manager instance
     * @param session the session manager for user session context
     */
    public UpdateAccount(String username, DBManager dbManager, SessionManager session) {
        super("Six Equations Solver");

        this.username = username;
        this.dbManager = dbManager;
        this.session = session;
        this.navigator = new GameNavigator(username, dbManager, session);

        UIFactory.defaultFrame(this);
        buildComponents();
    }

    /**
     * Builds and assembles all the panels required to render the update account form.
     */
    private void buildComponents() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIFactory.Colors.BACKGROUND);
        contentPanel.add(buildUserPanel());
        contentPanel.add(buildInfoPanel());
        contentPanel.add(buildUpdateAccountPanel());
        contentPanel.add(buildUpdateButtonsPanel());
        add(contentPanel);
    }

    /**
     * Constructs a panel displaying the current user information.
     *
     * @return a JPanel with the user display
     */
    private JPanel buildUserPanel() {
        return UIFactory.buildUserPanel(username);
    }

    /**
     * Constructs the information panel explaining how to use the update feature.
     *
     * @return a JPanel with explanatory text and instructions
     */
    private JPanel buildInfoPanel() {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        infoPanel.setBackground(UIFactory.Colors.BACKGROUND);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(40, 10, 10, 10));
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        JLabel titleLabel = UIFactory.Labels.label(
                "<html><div style='text-align: center;'>Update Your Banana Game Account</div><br><br></html>",
                UIFactory.Fonts.TITLE.deriveFont(Font.BOLD, 22f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel textLabel = UIFactory.Labels.
                label(
                        "<html>" +
                                "<div style='text-align: center; width: 600px;'>" +
                                "Here you can update both your password and email address for your account.<br>" +
                                "Alternatively, you may choose to update just one of them — it's entirely up to you.<br>" +
                                "Please let the field empty in case you choose to keep the old credential setting.<br><br><br>" +
                                "<b>Why update?</b><br><br>" +
                                "Keeping your credentials up-to-date ensures better security and communication.<br><br>" +
                                "Once you're done, click the <b>Submit Update</b> button to save your changes.<br>" +
                                "Changed your mind? Click <b>Cancel Update</b> to return without saving changes." +
                                "</div>" +
                                "</html>"
                        , UIFactory.Fonts.TEXT);
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(UIFactory.Colors.TEXT_PRIMARY);
        textLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);

        infoPanel.add(titleLabel);
        infoPanel.add(textLabel);
        return infoPanel;
    }

    /**
     * Constructs the panel that contains the input fields for password and email updates.
     *
     * @return a JPanel with password and email input fields
     */
    private JPanel buildUpdateAccountPanel() {
        JPanel updateAccountPanel = new JPanel();
        updateAccountPanel.setLayout(new BoxLayout(updateAccountPanel, BoxLayout.Y_AXIS));
        updateAccountPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        updateAccountPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel passwordLabel = UIFactory.Labels.label("Password ", UIFactory.Fonts.LABEL);
        passwordLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);
        passwordField = new JPasswordField(15);
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        passwordPanel.setBackground(UIFactory.Colors.BACKGROUND);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);

        JLabel emailLabel = UIFactory.Labels.label("Email ", UIFactory.Fonts.LABEL);
        emailLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emailLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);
        emailField = new JTextField(15);
        JPanel emailPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        emailPanel.setBackground(UIFactory.Colors.BACKGROUND);
        emailPanel.add(emailLabel);
        emailPanel.add(emailField);

        updateAccountPanel.add(passwordPanel);
        updateAccountPanel.add(emailPanel);
        return updateAccountPanel;
    }

    /**
     * Constructs the panel containing the action buttons for submitting or canceling the update.
     * Submitting triggers the update process in the DBManager and starts a new game session.
     * Canceling will return the user to the home screen without making changes.
     *
     * @return a JPanel with submit and cancel buttons
     */
    private JPanel buildUpdateButtonsPanel() {
        JPanel updateButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        updateButtonsPanel.setBackground(UIFactory.Colors.BACKGROUND);
        updateButtonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        updateButtonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        updateButtonsPanel.add(UIFactory.createButton(UIFactory.Buttons.NAV_Buttons.CANCEL_UPDATE,
                "CANCEL_UPDATE", e -> {
                    this.dispose();
                    navigator.openHome();
                }));

        updateButtonsPanel.add(UIFactory.createButton(UIFactory.Buttons.NAV_Buttons.UPDATE,
                "UPDATE", e -> {
                    String updatedPassword = new String(passwordField.getPassword());
                    String updatedEmail = emailField.getText();
                    dbManager.updateAccount(username, updatedPassword, updatedEmail);
                    this.dispose();
                    new LoginGUI(session, dbManager).setVisible(true);
                }));
        return updateButtonsPanel;
    }
}
