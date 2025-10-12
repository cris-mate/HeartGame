package com.heartgame.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the subscription and publication of game events
 * Implemented as a Singleton to ensure a single event bus for the application
 */
public final class GameEventManager {

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
     *
     * @param eventType The event type to listen for
     * @param listener  The listener to be notified
     */
    public void subscribe(GameEventType eventType, GameEventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Publishes an event to all subscribed listeners.
     *
     * @param eventType The type of event to publish.
     * @param data      The data to pass to the listeners.
     */
    public void publish(GameEventType eventType, Object data) {
        List<GameEventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            for (GameEventListener listener : eventListeners) {
                listener.onGameEvent(eventType, data);
            }
        }
    }
}
