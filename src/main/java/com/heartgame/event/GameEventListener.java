package com.heartgame.event;

/**
 * An interface for classes that want to listen to game events
 */
public interface GameEventListener {
    /**
     * Called when a subscribed event is published
     *
     * @param eventType The type of event that occurred
     * @param data      Optional data associated with the event
     */
    void onGameEvent(GameEventType eventType, Object data);
}
