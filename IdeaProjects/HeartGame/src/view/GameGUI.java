package view;

import controller.GameController;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * The main view for the game screen.
 */
public class GameGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = -107785653906635L;

    private final JLabel questArea = new JLabel();
    private final JTextArea infoArea = new JTextArea(1, 40);
    private final JButton[] buttons = new JButton[10];

    /**
     * Constructs the main game GUI, initializes all UI components,
     * and links this view to its controller
     */
    public GameGUI() {
        super("What is the missing value?");
        setSize(690, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();

        infoArea.setEditable(false);
        JScrollPane infoPane = new JScrollPane(infoArea);
        panel.add(infoPane);

        questArea.setSize(330, 600);
        JScrollPane questPane = new JScrollPane(questArea);
        panel.add(questPane);

        for (int i = 0; i < 10; i++) {
            buttons[i] = new JButton(String.valueOf(i));
            buttons[i].setActionCommand(String.valueOf(i));
            panel.add(buttons[i]);
        }

        getContentPane().add(panel);
        new GameController(this);
    }

    /**
     * Updates the question area with a new image and refreshes the score display
     * @param image The new question image
     * @param score The current score
     */
    public void updateQuestion(BufferedImage image, int score) {
        ImageIcon ii = new ImageIcon(image);
        questArea.setIcon(ii);
        infoArea.setText("How many hearts are there?   Score: " + score);
    }

    /**
     * Updates the information area with a new message
     * @param message The message to display
     */
    public void updateInfo(String message) {
        infoArea.setText(message);
    }

    /**
     * Returns the button at the specified index
     * @param index The index of the button (0-9)
     * @return The JButton at that index
     */
    public JButton getButton(int index) {
        return buttons[index];
    }

    /**
     * Displays an error message in a dialog box
     * @param message The error message to show
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Main entry point to launch the game GUI
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameGUI().setVisible(true));
    }
}
