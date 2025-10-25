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
 * Includes error handling to prevent one faulty listener from breaking the event system
 */
public final class GameEventManager {

    private static final Logger logger = LoggerFactory.getLogger(GameEventManager.class);
    private static GameEventManager instance;
    private final Map<GameEventType, List<GameEventListener>> listeners = new HashMap<>();

    private GameEventManager() {}

    /**
     * @return The single instance of the GameEventManager
     */
    public static GameEventManager getInstance() {
        if (instance == null) {
            instance = new GameEventManager();
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
     * @param eventType The type of event to publish
     * @param data      The data to pass to the listeners
     */
    public void publish(GameEventType eventType, Object data) {
        List<GameEventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            logger.debug("Publishing event: {} with {} listeners", eventType, eventListeners.size());
            for (GameEventListener listener : eventListeners) {
                try {
                    listener.onGameEvent(eventType, data);
                } catch (Exception e) {
                    // Log the error but continue notifying other listeners
                    logger.error("Error in event listener {} for event type {}: {}",
                            listener.getClass().getSimpleName(),
                            eventType,
                            e.getMessage(),
                            e);
                }
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
