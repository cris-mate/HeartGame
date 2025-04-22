package com.perisic.banana.engine;

import com.perisic.banana.peripherals.UIFactory;

import javax.swing.*;


/**
 * Manages a countdown timer for the Banana Game.
 * This class handles the timer logic, updates UI labels,
 * and provides methods to control time during gameplay.
 */
public class GameTimer {
    private javax.swing.Timer timer;
    private int sessionDuration = 180;
    private JLabel timerLabel;
    private JLabel messageLabel;
    private long startTime;

    /**
     * Constructs a GameTimer with the given timer and message labels.
     *
     * @param timerLabel   the label to display the remaining time.
     * @param messageLabel the label to display timeout messages.
     */
    public GameTimer(JLabel timerLabel, JLabel messageLabel) {
        this.timerLabel = timerLabel;
        this.messageLabel = messageLabel;
    }

    /**
     * Constructs a GameTimer for logic-only use (non-UI).
     * This constructor initializes the start time without requiring UI labels.
     */
    public GameTimer() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Starts the countdown timer from the default duration (180 seconds).
     * Updates the timer label every second and triggers an end message when time runs out.
     */
    public void start() {
        startTime = System.currentTimeMillis();
        timer = new javax.swing.Timer(1000, e -> {
            sessionDuration--;

            if (timerLabel != null) {
                timerLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);
                timerLabel.setText("Remaining Seconds: " + sessionDuration);
            }

            if (sessionDuration <= 0) {
                timer.stop();
                if (timerLabel != null) {
                    timerLabel.setText("Time's up!");
                    timerLabel.setForeground(UIFactory.Colors.ERROR);
                }
                if (messageLabel != null) {
                    messageLabel.setText("Oops! Time's up!");
                    messageLabel.setForeground(UIFactory.Colors.ERROR);
                }
            }
        });
        timer.start();
    }

    /**
     * Stops the countdown timer if it is currently running.
     */
    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * Determines if session is expired by time or max attempts.
     */
    public boolean isExpired() {
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        return elapsedSeconds >= sessionDuration;
    }

    /**
     * Returns the start time of the current session in milliseconds since epoch.
     *
     * @return the session's start time in milliseconds
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Subtracts the specified number of seconds from the remaining time.
     * Ensures that the time does not fall below zero.
     *
     * @param seconds number of seconds to subtract.
     */
    public void subtractTime(int seconds) {
        sessionDuration = Math.max(0, sessionDuration - seconds);
    }
}
