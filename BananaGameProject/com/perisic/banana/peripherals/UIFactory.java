package com.perisic.banana.peripherals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Central utility class responsible for standardizing the creation of UI components across the Banana Game.
 * It encapsulates reusable styles and component builders to ensure visual consistency
 * and modular design throughout the Swing-based GUI.
 */
public class UIFactory {

    /**
     * Contains standard fonts used throughout the application for different UI components.
     */
    public static class Fonts {
        public static final Font BUTTON = new Font("Monospaced", Font.PLAIN, 18);
        public static final Font LABEL = new Font("SansSerif", Font.PLAIN, 20);
        public static final Font TITLE = new Font("Serif", Font.PLAIN, 28);
        public static final Font MESSAGE = new Font("SansSerif", Font.PLAIN, 22);
        public static final Font TEXT = new Font("SansSerif", Font.PLAIN, 18);
        public static final Font USER = new Font("Monospaced", Font.PLAIN, 16);
        public static final Font GREETING = new Font("Monospaced", Font.PLAIN, 20);
    }

    /**
     * Defines standard colors used throughout the UI, maintaining theme consistency.
     */
    public static class Colors {
        public static final Color BUTTON = Color.getHSBColor(50f / 360, 0.30f, 0.95f);     // Soft Banana Yellow
        public static final Color BACKGROUND = Color.getHSBColor(190f / 360, 0.40f, 0.85f); // Rich Sky Blue

        public static final Color TEXT_PRIMARY = new Color(33, 33, 33);   // Charcoal Black
        public static final Color TEXT_SECONDARY = new Color(66, 66, 66); // Slate Gray
        public static final Color ERROR = new Color(198, 40, 40);         // Crimson Red
        public static final Color SUCCESS = new Color(27, 94, 32);        // Forest Green
        public static final Color FEEDBACK = new Color(13, 71, 161);      // Cobalt Blue  // Azure Blue
    }

    /**
     * Categorized button labels used in various parts of the UI, providing semantic grouping.
     */
    public static class Buttons {

        public static class CTRL_Buttons {
            public static final String PLAY_GAME = "Play Game";
            public static final String NEXT_GAME = "Next Game";
            public static final String HALL_OF_FAME = "Hall of Fame";
            public static final String MILESTONES = "Milestones";
            public static final String QUIT_GAME = "Quit Game";
            public static final String EXIT_GAME = "Exit Game";
            public static final String HOME = "Home";
            public static final String SWITCH_ACCOUNT = "Switch Account";
        }

        public static class SOL_Buttons {
            public static final String ZERO = "0";
            public static final String ONE = "1";
            public static final String TWO = "2";
            public static final String THREE = "3";
            public static final String FOUR = "4";
            public static final String FIVE = "5";
            public static final String SIX = "6";
            public static final String SEVEN = "7";
            public static final String EIGHT = "8";
            public static final String NINE = "9";

            public static final String [] ALL = {
                    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE
            };
        }

        public static class NAV_Buttons {
            public static final String REGISTER = "Register Account";
            public static final String CANCEL_REGISTRATION = "Cancel Registration";
            public static final String UPDATE = "Submit Update";
            public static final String CANCEL_UPDATE = "Cancel Update";
            public static final String LOGIN = "Login";
        }

        public static class ACC_Buttons {
            public static final String CREATE_ACCOUNT = "Create Account";
            public static final String UPDATE_ACCOUNT = "Update Password or Email";
            public static final String DELETE_ACCOUNT = "Delete Account";
        }
    }

    /**
     * Creates a styled RoundedButton with pre-defined font, background color, and action listener.
     *
     * @param name the label displayed on the button
     * @param actionCommand the action command identifier for event handling
     * @param listener the ActionListener to trigger on button press
     * @return a configured RoundedButton instance
     */
    public static RoundedButton createButton(String name, String actionCommand, ActionListener listener) {
        RoundedButton button = new RoundedButton(name);
        button.setActionCommand(actionCommand);
        button.addActionListener(listener);
        button.setFont(Fonts.BUTTON);
        button.setBackground(Colors.BUTTON);
        button.setFocusPainted(false);
        button.setOpaque(true);
        return button;
    }

    /**
     * Factory methods for creating user input fields.
     */
    public static class Fields {
        public static JTextField textField(int columns) {
            return new JTextField(columns);
        }

        public static JPasswordField passwordField(int columns) {
            return new JPasswordField(columns);
        }
    }

    public static class Labels {
        /**
         * Creates a JLabel with the specified text and font.
         *
         * @param text the label's visible text
         * @param font the font to be applied to the label
         * @return a configured JLabel instance
         */
        public static JLabel label(String text, Font font) {
            JLabel label = new JLabel(text);
            label.setFont(font);
            return label;
        }
    }

    /**
     * A message library containing randomized encouragement or feedback strings for the game.
     */
    public static class MessagePool {
        public static final List<String> POSITIVE = List.of(
                "Nice work! Try the next one!",
                "Right answer! You're on fire!",
                "Correct! Here's your next challenge.",
                "Crushed it! Go for the next one.",
                "You're killing it — next up!",
                "Well done! Ready for the next?",
                "Right answer! Let’s go again!",
                "Correct solution! Solve the next one.",
                "Well done! Keep going!",
                "Another one down! Ready for the next?"
        );
        public static final List<String> SUPPORTIVE = List.of(
                "Almost there! Try again — you’ve got this!",
                "Don’t worry, keep going!",
                "Mistakes are just steps to success! Try next equation",
                "Stay focused — you can do it!",
                "Close! Think it through one more time.",
                "Keep it up — you’re learning fast!",
                "No worries! Try the next one!",
                "You're doing great, don't stop now!",
                "Stay sharp! You’re making progress.",
                "Just a small hiccup — bounce back!"
        );
        public static final List<String> NEXT_GAME = List.of(
                "Ready for your next challenge?",
                "Here comes another one!",
                "Let’s crack the next equation!",
                "New equation loaded — go get it!",
                "Another challenge — you're built for this!",
                "Level up — next puzzle!",
                "Fresh round! Solve this one!",
                "Keep climbing — one equation at a time!"
        );
    }

    /**
     * Applies default window properties to a JFrame such as size, position, and close behavior.
     *
     * @param frame the target JFrame to be styled
     */
    public static void defaultFrame(JFrame frame) {
        frame.setSize(800, 660);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
    }

    /**
     * Builds a user information panel with a username and icon, aligned to the right.
     *
     * @param username the currently logged-in user's name
     * @return a JPanel representing the user info display
     */
    public static JPanel buildUserPanel(String username) {
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        userPanel.setBackground(Colors.BACKGROUND);
        userPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        userPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JLabel icon = Labels.label("👤 ", Fonts.LABEL.deriveFont(16f));
        icon.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel currentUser = Labels.label(username, Fonts.USER);
        currentUser.setForeground(Colors.TEXT_SECONDARY);
        currentUser.setHorizontalAlignment(SwingConstants.RIGHT);

        userPanel.add(icon);
        userPanel.add(currentUser);
        return userPanel;
    }
}
