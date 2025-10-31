package com.heartgame;

import com.heartgame.controller.NavigationController;
import com.heartgame.view.LoginGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Main entry point for the Heart Game application
 * Initializes the navigation system and displays the login screen
 */
public class HeartGameApplication {

    private static final Logger logger = LoggerFactory.getLogger(HeartGameApplication.class);

    /**
     * Application entry point
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        logger.info("Starting Heart Game Application...");

        // Set system look and feel for better native integration
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            logger.debug("System look and feel set successfully");
        } catch (Exception e) {
            logger.warn("Failed to set system look and feel, using default", e);
        }

        // Launch application on Swing EDT
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize navigation controller
                NavigationController navigationController = NavigationController.getInstance();
                logger.info("NavigationController initialized");

                // Create and display login screen
                LoginGUI loginGUI = new LoginGUI();
                navigationController.setCurrentWindow(loginGUI);

                logger.info("Heart Game Application started successfully");

            } catch (Exception e) {
                logger.error("Failed to start Heart Game Application", e);
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to start application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}
