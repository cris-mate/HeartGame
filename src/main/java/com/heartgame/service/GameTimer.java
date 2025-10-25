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
 * Game ends when timer reaches zero
 * Publishes TIMER_EXPIRED event when time runs out
 */
public class GameTimer implements GameEventListener {

    private static final Logger logger = LoggerFactory.getLogger(GameTimer.class);
    private static final int TIME_LIMIT_SECONDS = 60;
    private static final int WARNING_THRESHOLD = 10; // Last 10 seconds show warning

    private int remainingTime;
    private Timer swingTimer;
    private final TimerUpdateListener updateListener;

    /**
     * Interface for receiving timer updates
     */
    public interface TimerUpdateListener {
        /**
         * Called every second as the timer counts down
         * @param remainingSeconds Time remaining in seconds
         * @param isWarning True if in warning state (last 10 seconds)
         */
        void onTimerUpdate(int remainingSeconds, boolean isWarning);

        /**
         * Called when timer reaches zero
         */
        void onTimerExpired();
    }

    /**
     * Constructs a new GameTimer
     * Automatically subscribes to GAME_STARTED event to begin countdown
     * @param updateListener Listener for timer UI updates
     */
    public GameTimer(TimerUpdateListener updateListener) {
        this.updateListener = updateListener;

        // Subscribe to game start event only
        GameEventManager.getInstance().subscribe(GameEventType.GAME_STARTED, this);

        logger.debug("GameTimer initialized with {}s session time limit", TIME_LIMIT_SECONDS);
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

        remainingTime = TIME_LIMIT_SECONDS;
        logger.debug("Starting timer: {}s", remainingTime);

        // Create Swing timer that fires every second
        swingTimer = new Timer(1000, e -> {
            remainingTime--;
            boolean isWarning = remainingTime <= WARNING_THRESHOLD;

            logger.trace("Timer tick: {}s remaining (warning: {})", remainingTime, isWarning);

            // Notify UI of update
            updateListener.onTimerUpdate(remainingTime, isWarning);

            // Check if time expired
            if (remainingTime <= 0) {
                logger.info("Timer expired - no answer submitted in time");
                stopTimer();
                GameEventManager.getInstance().publish(GameEventType.TIMER_EXPIRED, null);
            }
        });

        swingTimer.start();

        // Send initial update immediately
        updateListener.onTimerUpdate(remainingTime, false);
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
    }

    /**
     * @return The time limit in seconds for each question
     */
    public static int getTimeLimit() {
        return TIME_LIMIT_SECONDS;
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
}
