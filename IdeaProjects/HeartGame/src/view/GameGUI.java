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

    public void updateQuestion(BufferedImage image, int score) {
        ImageIcon ii = new ImageIcon(image);
        questArea.setIcon(ii);
        infoArea.setText("How many hearts are there?   Score: " + score);
    }

    public void updateInfo(String message) {
        infoArea.setText(message);
    }

    public JButton getButton(int index) {
        return buttons[index];
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameGUI().setVisible(true));
    }
}
