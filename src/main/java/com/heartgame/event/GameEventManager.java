package com.heartgame.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the subscription and publication of game events
 * Implemented as a Singleton to ensure a single event bus for the application
 * Includes error handling to prevent one faulty listener from breaking the event system,
 * concurrent modification protection and detailed error tracking
 */
public final class GameEventManager {

    private static final Logger logger = LoggerFactory.getLogger(GameEventManager.class);
    private static volatile GameEventManager instance;
    private final Map<GameEventType, List<GameEventListener>> listeners = new HashMap<>();

    private GameEventManager() {}

    /**
     * Thread-safe singleton implementation using double-checked locking
     * @return The single instance of the GameEventManager
     */
    public static GameEventManager getInstance() {
        if (instance == null) {
            synchronized (GameEventManager.class) {
                if (instance == null) {
                    instance = new GameEventManager();
                }
            }
        }
        return instance;
    }

    /**
     * Subscribes a listener to a specific event type
     * @param eventType The event type to listen for
     * @param listener  The listener to be notified
     */
    public void subscribe(GameEventType eventType, GameEventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
        logger.debug("Listener {} subscribed to event type: {}",
                listener.getClass().getSimpleName(), eventType);
    }

    /**
     * Unsubscribes a listener from a specific event type
     * Useful for cleanup to prevent memory leaks
     * @param eventType The event type to unsubscribe from
     * @param listener  The listener to remove
     */
    public void unsubscribe(GameEventType eventType, GameEventListener listener) {
        List<GameEventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            logger.debug("Listener {} unsubscribed from event type: {}",
                    listener.getClass().getSimpleName(), eventType);
        }
    }

    /**
     * Publishes an event to all subscribed listeners
     * Catches exceptions from individual listeners to prevent one
     * faulty listener from breaking the entire event chain
     * Each listener failure is logged but doesn't stop event propagation
     * Includes protection against concurrent modification during iteration
     *
     * @param eventType The type of event to publish
     * @param data      The data to pass to the listeners
     */
    public void publish(GameEventType eventType, Object data) {
        List<GameEventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            logger.debug("Publishing event: {} with {} listeners", eventType, eventListeners.size());

            int successCount = 0;
            int failureCount = 0;

            // Make a copy to avoid concurrent modification if listeners unsubscribe during iteration
            List<GameEventListener> listenersCopy = new ArrayList<>(eventListeners);

            for (GameEventListener listener : listenersCopy) {
                try {
                    listener.onGameEvent(eventType, data);
                    successCount++;
                    logger.trace("Event {} successfully processed by {}",
                            eventType, listener.getClass().getSimpleName());
                } catch (Exception e) {
                    // Log the error but continue notifying other listeners
                    failureCount++;
                    logger.error("Event listener {} failed to process event type {}: {}",
                            listener.getClass().getSimpleName(),
                            eventType,
                            e.getMessage(),
                            e);
                }
            }

            if (failureCount > 0) {
                logger.warn("Event {} completed with {} successes and {} failures",
                        eventType, successCount, failureCount);
            }
        } else {
            logger.trace("No listeners registered for event type: {}", eventType);
        }
    }

    /**
     * Clears all event listeners
     * Useful for testing or cleanup
     */
    public void clearAllListeners() {
        listeners.clear();
        logger.info("All event listeners cleared");
    }

    /**
     * Gets the number of listeners for a specific event type
     * Useful for debugging and testing
     *
     * @param eventType The event type to check
     * @return The number of listeners subscribed to this event type
     */
    public int getListenerCount(GameEventType eventType) {
        List<GameEventListener> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size() : 0;
    }
}
