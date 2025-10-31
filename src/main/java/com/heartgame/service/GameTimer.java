package com.heartgame.service;

import com.heartgame.event.GameEventListener;
import com.heartgame.event.GameEventManager;
import com.heartgame.event.GameEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.Timer;

/**
 * Manages the game session timer - a countdown of the game
 * Timer starts when game begins and counts down from 60 seconds
 * Supports pause and resume functionality
 * Game ends when timer reaches zero
 * Publishes TIMER_TICK and TIMER_EXPIRED events for UI updates
 */
public class GameTimer implements GameEventListener {

    private static final Logger logger = LoggerFactory.getLogger(GameTimer.class);
    private static final int TIME_LIMIT = 60;
    private int remainingTime;
    private Timer swingTimer;
    private boolean isPaused = false;

    /**
     * Constructs a new GameTimer
     * Automatically subscribes to GAME_STARTED event to begin countdown
     * Publishes TIMER_TICK events every second and TIMER_EXPIRED when time runs out
     */
    public GameTimer() {
        GameEventManager.getInstance().subscribe(GameEventType.GAME_STARTED, this);
        logger.debug("GameTimer initialized with {}s session time limit", TIME_LIMIT);
    }

    /**
     * Handles game events to control timer behaviour
     * Starts timer when GAME_STARTED event is received
     * @param eventType The type of event that occurred
     * @param data Optional data associated with the event
     */
    @Override
    public void onGameEvent(GameEventType eventType, Object data) {
        if (eventType == GameEventType.GAME_STARTED) {
            startTimer();
        }
    }

    /**
     * Starts the countdown timer
     * Cancels any existing timer before starting a new one
     */
    private void startTimer() {
        stopTimer(); // Cancel any existing timer

        remainingTime = TIME_LIMIT;
        isPaused = false;
        logger.debug("Starting timer: {}s", remainingTime);

        // Create Swing timer that fires every second
        swingTimer = new Timer(1000, e -> {
            if (!isPaused) {
                remainingTime--;
                logger.trace("Timer tick: {}s remaining.", remainingTime);

                // Publish timer tick event
                GameEventManager.getInstance().publish(GameEventType.TIMER_TICK, remainingTime);

                // Check if time expired
                if (remainingTime <= 0) {
                    logger.info("Timer expired - game over");
                    stopTimer();
                    GameEventManager.getInstance().publish(GameEventType.TIMER_EXPIRED, null);
                }
            }
        });
        swingTimer.start();

        // Send initial update immediately
        GameEventManager.getInstance().publish(GameEventType.TIMER_TICK, remainingTime);
    }

    /**
     * Pauses the timer
     * Timer continues running but time doesn't decrease
     */
    public void pauseTimer() {
        if (swingTimer != null && swingTimer.isRunning() && !isPaused) {
            isPaused = true;
            logger.debug("Timer paused at {}s remaining", remainingTime);
        }
    }

    /**
     * Resumes the timer from paused state
     */
    public void resumeTimer() {
        if (swingTimer != null && swingTimer.isRunning() && isPaused) {
            isPaused = false;
            logger.debug("Timer resumed from {}s remaining", remainingTime);
            // Immediately update UI when resuming
            GameEventManager.getInstance().publish(GameEventType.TIMER_TICK, remainingTime);
        }
    }


    /**
     * Stops the current timer
     * Safe to call even if no timer is running
     */
    public void stopTimer() {
        if (swingTimer != null && swingTimer.isRunning()) {
            logger.debug("Stopping timer at {}s remaining", remainingTime);
            swingTimer.stop();
            swingTimer = null;
        }
        isPaused = false;
    }

    /**
     * @return The time limit in seconds for the game
     */
    public static int getTimeLimit() {
        return TIME_LIMIT;
    }

    /**
     * @return The current remaining time in seconds
     */
    public int getRemainingTime() {
        return remainingTime;
    }

    /**
     * @return True if timer is currently running
     */
    public boolean isRunning() {
        return swingTimer != null && swingTimer.isRunning();
    }

    /**
     * @return True if timer is currently paused
     */
    public boolean isPaused() {
        return isPaused;
    }
}
