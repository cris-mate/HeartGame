package com.heartgame.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for GameEventManager
 * Tests singleton pattern, event subscription, publishing, error handling, and concurrency
 */
@DisplayName("GameEventManager Tests")
class GameEventManagerTest {

    private GameEventManager eventManager;

    @BeforeEach
    void setUp() {
        eventManager = GameEventManager.getInstance();
        eventManager.clearAllListeners();
    }

    @AfterEach
    void tearDown() {
        eventManager.clearAllListeners();
    }

    // ========== Singleton Pattern Tests ==========

    @Test
    @DisplayName("Should return same instance on multiple getInstance() calls")
    void testSingleton() {
        GameEventManager instance1 = GameEventManager.getInstance();
        GameEventManager instance2 = GameEventManager.getInstance();

        assertSame(instance1, instance2, "getInstance() should return the same instance");
    }

    @Test
    @DisplayName("Should maintain singleton across threads")
    void testSingletonThreadSafety() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<GameEventManager> instances = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                instances.add(GameEventManager.getInstance());
                latch.countDown();
            }).start();
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "All threads should complete");
        assertEquals(threadCount, instances.size(), "Should have collected all instances");

        GameEventManager firstInstance = instances.get(0);
        for (GameEventManager instance : instances) {
            assertSame(firstInstance, instance, "All instances should be the same");
        }
    }

    // ========== Subscription Tests ==========

    @Test
    @DisplayName("Should successfully subscribe listener to event")
    void testSubscribe() {
        TestListener listener = new TestListener();

        eventManager.subscribe(GameEventType.GAME_STARTED, listener);

        assertEquals(1, eventManager.getListenerCount(GameEventType.GAME_STARTED),
                "Should have 1 listener after subscription");
    }

    @Test
    @DisplayName("Should allow multiple listeners for same event type")
    void testMultipleListenersForSameEvent() {
        TestListener listener1 = new TestListener();
        TestListener listener2 = new TestListener();
        TestListener listener3 = new TestListener();

        eventManager.subscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, listener1);
        eventManager.subscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, listener2);
        eventManager.subscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, listener3);

        assertEquals(3, eventManager.getListenerCount(GameEventType.CORRECT_ANSWER_SUBMITTED),
                "Should have 3 listeners for the event");
    }

    @Test
    @DisplayName("Should allow same listener for multiple event types")
    void testSameListenerForMultipleEvents() {
        TestListener listener = new TestListener();

        eventManager.subscribe(GameEventType.GAME_STARTED, listener);
        eventManager.subscribe(GameEventType.GAME_ENDED, listener);
        eventManager.subscribe(GameEventType.TIMER_TICK, listener);

        assertEquals(1, eventManager.getListenerCount(GameEventType.GAME_STARTED));
        assertEquals(1, eventManager.getListenerCount(GameEventType.GAME_ENDED));
        assertEquals(1, eventManager.getListenerCount(GameEventType.TIMER_TICK));
    }

    // ========== Unsubscription Tests ==========

    @Test
    @DisplayName("Should successfully unsubscribe listener from event")
    void testUnsubscribe() {
        TestListener listener = new TestListener();
        eventManager.subscribe(GameEventType.GAME_STARTED, listener);
        assertEquals(1, eventManager.getListenerCount(GameEventType.GAME_STARTED));

        eventManager.unsubscribe(GameEventType.GAME_STARTED, listener);

        assertEquals(0, eventManager.getListenerCount(GameEventType.GAME_STARTED),
                "Should have 0 listeners after unsubscription");
    }

    @Test
    @DisplayName("Should handle unsubscribe of non-subscribed listener")
    void testUnsubscribeNonSubscribed() {
        TestListener listener = new TestListener();

        assertDoesNotThrow(() -> eventManager.unsubscribe(GameEventType.GAME_STARTED, listener),
                "Unsubscribing non-subscribed listener should not throw");
    }

    @Test
    @DisplayName("Should only unsubscribe specific listener")
    void testUnsubscribeSpecificListener() {
        TestListener listener1 = new TestListener();
        TestListener listener2 = new TestListener();
        TestListener listener3 = new TestListener();

        eventManager.subscribe(GameEventType.GAME_STARTED, listener1);
        eventManager.subscribe(GameEventType.GAME_STARTED, listener2);
        eventManager.subscribe(GameEventType.GAME_STARTED, listener3);

        eventManager.unsubscribe(GameEventType.GAME_STARTED, listener2);

        assertEquals(2, eventManager.getListenerCount(GameEventType.GAME_STARTED),
                "Should have 2 listeners remaining");
    }

    // ========== Event Publishing Tests ==========

    @Test
    @DisplayName("Should publish event to subscribed listener")
    void testPublishToListener() {
        TestListener listener = new TestListener();
        eventManager.subscribe(GameEventType.GAME_STARTED, listener);

        eventManager.publish(GameEventType.GAME_STARTED, "test data");

        assertEquals(1, listener.getEventCount());
        assertEquals(GameEventType.GAME_STARTED, listener.getLastEventType());
        assertEquals("test data", listener.getLastEventData());
    }

    @Test
    @DisplayName("Should publish event to all subscribed listeners")
    void testPublishToMultipleListeners() {
        TestListener listener1 = new TestListener();
        TestListener listener2 = new TestListener();
        TestListener listener3 = new TestListener();

        eventManager.subscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, listener1);
        eventManager.subscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, listener2);
        eventManager.subscribe(GameEventType.CORRECT_ANSWER_SUBMITTED, listener3);

        eventManager.publish(GameEventType.CORRECT_ANSWER_SUBMITTED, null);

        assertEquals(1, listener1.getEventCount());
        assertEquals(1, listener2.getEventCount());
        assertEquals(1, listener3.getEventCount());
    }

    @Test
    @DisplayName("Should not publish to unsubscribed listeners")
    void testPublishNotToUnsubscribed() {
        TestListener listener = new TestListener();
        eventManager.subscribe(GameEventType.GAME_STARTED, listener);
        eventManager.unsubscribe(GameEventType.GAME_STARTED, listener);

        eventManager.publish(GameEventType.GAME_STARTED, null);

        assertEquals(0, listener.getEventCount(),
                "Unsubscribed listener should not receive events");
    }

    @Test
    @DisplayName("Should handle publishing event with no listeners")
    void testPublishWithNoListeners() {
        assertDoesNotThrow(() -> eventManager.publish(GameEventType.GAME_STARTED, null),
                "Publishing with no listeners should not throw");
    }

    @Test
    @DisplayName("Should pass correct event data to listeners")
    void testEventDataPropagation() {
        TestListener listener = new TestListener();
        eventManager.subscribe(GameEventType.TIMER_TICK, listener);

        Integer testData = 42;
        eventManager.publish(GameEventType.TIMER_TICK, testData);

        assertEquals(testData, listener.getLastEventData());
    }

    @Test
    @DisplayName("Should handle null event data")
    void testNullEventData() {
        TestListener listener = new TestListener();
        eventManager.subscribe(GameEventType.GAME_ENDED, listener);

        assertDoesNotThrow(() -> eventManager.publish(GameEventType.GAME_ENDED, null),
                "Should handle null event data");
        assertEquals(1, listener.getEventCount());
        assertNull(listener.getLastEventData());
    }

    // ========== Error Handling Tests ==========

    @Test
    @DisplayName("Should isolate errors from faulty listeners")
    void testErrorIsolation() {
        TestListener goodListener1 = new TestListener();
        FailingListener failingListener = new FailingListener();
        TestListener goodListener2 = new TestListener();

        eventManager.subscribe(GameEventType.GAME_STARTED, goodListener1);
        eventManager.subscribe(GameEventType.GAME_STARTED, failingListener);
        eventManager.subscribe(GameEventType.GAME_STARTED, goodListener2);

        assertDoesNotThrow(() -> eventManager.publish(GameEventType.GAME_STARTED, null),
                "Publishing should not throw even with failing listener");

        assertEquals(1, goodListener1.getEventCount(),
                "Good listener 1 should receive event");
        assertEquals(1, goodListener2.getEventCount(),
                "Good listener 2 should receive event despite failing listener");
    }

    @Test
    @DisplayName("Should continue notifying listeners after one fails")
    void testContinueAfterListenerFailure() {
        List<TestListener> listeners = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TestListener listener = new TestListener();
            listeners.add(listener);
            eventManager.subscribe(GameEventType.GAME_STARTED, listener);
        }

        // Insert failing listener in the middle
        eventManager.subscribe(GameEventType.GAME_STARTED, new FailingListener());

        eventManager.publish(GameEventType.GAME_STARTED, null);

        for (TestListener listener : listeners) {
            assertEquals(1, listener.getEventCount(),
                    "All good listeners should receive event");
        }
    }

    // ========== Concurrent Modification Protection Tests ==========

    @Test
    @DisplayName("Should handle listener unsubscribing during event processing")
    void testUnsubscribeDuringEventProcessing() {
        UnsubscribingListener unsubscribingListener = new UnsubscribingListener(eventManager);
        TestListener regularListener = new TestListener();

        eventManager.subscribe(GameEventType.GAME_STARTED, unsubscribingListener);
        eventManager.subscribe(GameEventType.GAME_STARTED, regularListener);

        assertDoesNotThrow(() -> eventManager.publish(GameEventType.GAME_STARTED, null),
                "Should handle concurrent modification during event processing");

        assertEquals(1, regularListener.getEventCount(),
                "Regular listener should still receive event");
    }

    // ========== Clear Listeners Tests ==========

    @Test
    @DisplayName("Should clear all listeners")
    void testClearAllListeners() {
        TestListener listener1 = new TestListener();
        TestListener listener2 = new TestListener();

        eventManager.subscribe(GameEventType.GAME_STARTED, listener1);
        eventManager.subscribe(GameEventType.GAME_ENDED, listener2);

        eventManager.clearAllListeners();

        assertEquals(0, eventManager.getListenerCount(GameEventType.GAME_STARTED));
        assertEquals(0, eventManager.getListenerCount(GameEventType.GAME_ENDED));
    }

    @Test
    @DisplayName("Should handle clearing when no listeners exist")
    void testClearEmptyListeners() {
        assertDoesNotThrow(() -> eventManager.clearAllListeners(),
                "Clearing empty listeners should not throw");
    }

    // ========== Listener Count Tests ==========

    @Test
    @DisplayName("Should return 0 for event type with no listeners")
    void testGetListenerCountNoListeners() {
        assertEquals(0, eventManager.getListenerCount(GameEventType.GAME_STARTED),
                "Should return 0 for event with no listeners");
    }

    @Test
    @DisplayName("Should return correct listener count")
    void testGetListenerCountAccurate() {
        for (int i = 0; i < 5; i++) {
            eventManager.subscribe(GameEventType.TIMER_TICK, new TestListener());
        }

        assertEquals(5, eventManager.getListenerCount(GameEventType.TIMER_TICK),
                "Should return correct listener count");
    }

    // ========== Concurrency Tests ==========

    @Test
    @DisplayName("Should handle concurrent event publishing")
    void testConcurrentPublishing() throws InterruptedException {
        int threadCount = 10;
        int eventsPerThread = 10;
        AtomicInteger totalEvents = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount);

        TestListener listener = new TestListener();
        eventManager.subscribe(GameEventType.GAME_STARTED, listener);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < eventsPerThread; j++) {
                    eventManager.publish(GameEventType.GAME_STARTED, null);
                    totalEvents.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "All threads should complete");
        assertEquals(threadCount * eventsPerThread, listener.getEventCount(),
                "Listener should receive all events");
    }

    // ========== Helper Classes ==========

    /**
     * Test listener that tracks events received
     */
    private static class TestListener implements GameEventListener {
        private int eventCount = 0;
        private GameEventType lastEventType;
        private Object lastEventData;

        @Override
        public void onGameEvent(GameEventType eventType, Object data) {
            eventCount++;
            lastEventType = eventType;
            lastEventData = data;
        }

        public int getEventCount() {
            return eventCount;
        }

        public GameEventType getLastEventType() {
            return lastEventType;
        }

        public Object getLastEventData() {
            return lastEventData;
        }
    }

    /**
     * Listener that always throws an exception
     */
    private static class FailingListener implements GameEventListener {
        @Override
        public void onGameEvent(GameEventType eventType, Object data) {
            throw new RuntimeException("Intentional test failure");
        }
    }

    /**
     * Listener that unsubscribes itself during event processing
     */
    private static class UnsubscribingListener implements GameEventListener {
        private final GameEventManager eventManager;

        public UnsubscribingListener(GameEventManager eventManager) {
            this.eventManager = eventManager;
        }

        @Override
        public void onGameEvent(GameEventType eventType, Object data) {
            eventManager.unsubscribe(eventType, this);
        }
    }
}
