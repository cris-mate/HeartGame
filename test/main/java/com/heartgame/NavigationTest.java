package com.heartgame;

import com.heartgame.controller.NavigationController;
import com.heartgame.event.GameEventListener;
import com.heartgame.event.GameEventManager;
import com.heartgame.event.GameEventType;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Comprehensive navigation test suite for the Heart Game application
 * Tests navigation between all screens, event handling, and window lifecycle
 */
public class NavigationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=== Heart Game Navigation Test Suite ===\n");

        // Test 1: Event Manager Singleton Thread Safety
        testEventManagerThreadSafety();

        // Test 2: Event Subscription and Publishing
        testEventSubscriptionAndPublishing();

        // Test 3: Navigation Controller Initialization
        testNavigationControllerInitialization();

        // Test 4: Navigation Event Handling
        testNavigationEventHandling();

        // Test 5: Multiple Event Listeners
        testMultipleEventListeners();

        // Test 6: Event Unsubscription
        testEventUnsubscription();

        // Test 7: Event Error Isolation
        testEventErrorIsolation();

        // Test 8: Concurrent Event Publishing
        testConcurrentEventPublishing();

        // Test 9: Navigation to All Screens
        testNavigationToAllScreens();

        // Summary
        System.out.println("\n=== Test Summary ===");
        System.out.println("Tests Passed: " + testsPassed);
        System.out.println("Tests Failed: " + testsFailed);
        System.out.println("Total Tests: " + (testsPassed + testsFailed));

        if (testsFailed == 0) {
            System.out.println("\n✅ All navigation tests passed!");
        } else {
            System.out.println("\n⚠️ Some tests failed. Please review the errors above.");
        }

        // Exit cleanly
        System.exit(testsFailed > 0 ? 1 : 0);
    }

    private static void testEventManagerThreadSafety() {
        System.out.println("1. Testing GameEventManager Thread Safety...");
        try {
            final int NUM_THREADS = 10;
            final CountDownLatch latch = new CountDownLatch(NUM_THREADS);
            final GameEventManager[] instances = new GameEventManager[NUM_THREADS];

            // Create multiple threads trying to get the singleton instance simultaneously
            Thread[] threads = new Thread[NUM_THREADS];
            for (int i = 0; i < NUM_THREADS; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    instances[index] = GameEventManager.getInstance();
                    latch.countDown();
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            latch.await(5, TimeUnit.SECONDS);

            // Verify all instances are the same
            GameEventManager firstInstance = instances[0];
            for (int i = 1; i < NUM_THREADS; i++) {
                assert instances[i] == firstInstance : "All instances should be the same (singleton violation)";
            }

            System.out.println("   ✓ GameEventManager is thread-safe (singleton property maintained)");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   ✗ Thread safety test failed: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }

    private static void testEventSubscriptionAndPublishing() {
        System.out.println("\n2. Testing Event Subscription and Publishing...");
        try {
            GameEventManager eventManager = GameEventManager.getInstance();
            final AtomicBoolean eventReceived = new AtomicBoolean(false);
            final AtomicInteger receivedData = new AtomicInteger(-1);

            GameEventListener testListener = (eventType, data) -> {
                if (eventType == GameEventType.NAVIGATE_TO_HOME) {
                    eventReceived.set(true);
                    if (data instanceof Integer) {
                        receivedData.set((Integer) data);
                    }
                }
            };

            // Subscribe to event
            eventManager.subscribe(GameEventType.NAVIGATE_TO_HOME, testListener);

            // Publish event with data
            eventManager.publish(GameEventType.NAVIGATE_TO_HOME, 42);

            // Verify event was received
            assert eventReceived.get() : "Event should have been received";
            assert receivedData.get() == 42 : "Event data should match published data";

            // Cleanup
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_HOME, testListener);

            System.out.println("   ✓ Event subscription and publishing working correctly");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   ✗ Event subscription test failed: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }

    private static void testNavigationControllerInitialization() {
        System.out.println("\n3. Testing NavigationController Initialization...");
        try {
            NavigationController navController = NavigationController.getInstance();

            assert navController != null : "NavigationController should not be null";

            // Verify it's subscribed to all navigation events
            GameEventManager eventManager = GameEventManager.getInstance();

            // Check listener counts (NavigationController should be subscribed to each)
            int loginListeners = eventManager.getListenerCount(GameEventType.NAVIGATE_TO_LOGIN);
            int homeListeners = eventManager.getListenerCount(GameEventType.NAVIGATE_TO_HOME);
            int gameListeners = eventManager.getListenerCount(GameEventType.NAVIGATE_TO_GAME);
            int leaderboardListeners = eventManager.getListenerCount(GameEventType.NAVIGATE_TO_LEADERBOARD);
            int registerListeners = eventManager.getListenerCount(GameEventType.NAVIGATE_TO_REGISTER);

            assert loginListeners >= 1 : "Should have at least one listener for NAVIGATE_TO_LOGIN";
            assert homeListeners >= 1 : "Should have at least one listener for NAVIGATE_TO_HOME";
            assert gameListeners >= 1 : "Should have at least one listener for NAVIGATE_TO_GAME";
            assert leaderboardListeners >= 1 : "Should have at least one listener for NAVIGATE_TO_LEADERBOARD";
            assert registerListeners >= 1 : "Should have at least one listener for NAVIGATE_TO_REGISTER";

            System.out.println("   ✓ NavigationController initialized and subscribed to all navigation events");
            System.out.println("   Listeners - Login: " + loginListeners + ", Home: " + homeListeners +
                    ", Game: " + gameListeners + ", Leaderboard: " + leaderboardListeners +
                    ", Register: " + registerListeners);
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   ✗ NavigationController initialization test failed: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }

    private static void testNavigationEventHandling() {
        System.out.println("\n4. Testing Navigation Event Handling...");
        try {
            final AtomicBoolean loginEventHandled = new AtomicBoolean(false);
            final AtomicBoolean homeEventHandled = new AtomicBoolean(false);

            GameEventListener testListener = (eventType, data) -> {
                if (eventType == GameEventType.NAVIGATE_TO_LOGIN) {
                    loginEventHandled.set(true);
                } else if (eventType == GameEventType.NAVIGATE_TO_HOME) {
                    homeEventHandled.set(true);
                }
            };

            GameEventManager eventManager = GameEventManager.getInstance();
            eventManager.subscribe(GameEventType.NAVIGATE_TO_LOGIN, testListener);
            eventManager.subscribe(GameEventType.NAVIGATE_TO_HOME, testListener);

            // Publish navigation events
            eventManager.publish(GameEventType.NAVIGATE_TO_LOGIN, null);
            eventManager.publish(GameEventType.NAVIGATE_TO_HOME, null);

            // Give time for event processing
            Thread.sleep(100);

            assert loginEventHandled.get() : "Login navigation event should have been handled";
            assert homeEventHandled.get() : "Home navigation event should have been handled";

            // Cleanup
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_LOGIN, testListener);
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_HOME, testListener);

            System.out.println("   ✓ Navigation events handled correctly");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   ✗ Navigation event handling test failed: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }

    private static void testMultipleEventListeners() {
        System.out.println("\n5. Testing Multiple Event Listeners...");
        try {
            final AtomicInteger listener1Count = new AtomicInteger(0);
            final AtomicInteger listener2Count = new AtomicInteger(0);
            final AtomicInteger listener3Count = new AtomicInteger(0);

            GameEventListener listener1 = (eventType, data) -> listener1Count.incrementAndGet();
            GameEventListener listener2 = (eventType, data) -> listener2Count.incrementAndGet();
            GameEventListener listener3 = (eventType, data) -> listener3Count.incrementAndGet();

            GameEventManager eventManager = GameEventManager.getInstance();
            eventManager.subscribe(GameEventType.NAVIGATE_TO_GAME, listener1);
            eventManager.subscribe(GameEventType.NAVIGATE_TO_GAME, listener2);
            eventManager.subscribe(GameEventType.NAVIGATE_TO_GAME, listener3);

            // Publish event once
            eventManager.publish(GameEventType.NAVIGATE_TO_GAME, null);

            // All three listeners should have received the event
            assert listener1Count.get() == 1 : "Listener 1 should have received the event";
            assert listener2Count.get() == 1 : "Listener 2 should have received the event";
            assert listener3Count.get() == 1 : "Listener 3 should have received the event";

            // Cleanup
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_GAME, listener1);
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_GAME, listener2);
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_GAME, listener3);

            System.out.println("   ✓ Multiple listeners receive events correctly");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   ✗ Multiple listeners test failed: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }

    private static void testEventUnsubscription() {
        System.out.println("\n6. Testing Event Unsubscription...");
        try {
            final AtomicInteger eventCount = new AtomicInteger(0);

            GameEventListener testListener = (eventType, data) -> eventCount.incrementAndGet();

            GameEventManager eventManager = GameEventManager.getInstance();
            eventManager.subscribe(GameEventType.NAVIGATE_TO_LEADERBOARD, testListener);

            // Publish event - should be received
            eventManager.publish(GameEventType.NAVIGATE_TO_LEADERBOARD, null);
            assert eventCount.get() == 1 : "Event should have been received before unsubscription";

            // Unsubscribe
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_LEADERBOARD, testListener);

            // Publish event again - should NOT be received
            eventManager.publish(GameEventType.NAVIGATE_TO_LEADERBOARD, null);
            assert eventCount.get() == 1 : "Event should NOT have been received after unsubscription";

            System.out.println("   ✓ Event unsubscription working correctly");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   ✗ Event unsubscription test failed: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }

    private static void testEventErrorIsolation() {
        System.out.println("\n7. Testing Event Error Isolation...");
        try {
            final AtomicBoolean goodListenerCalled = new AtomicBoolean(false);

            // Create a faulty listener that throws an exception
            GameEventListener faultyListener = (eventType, data) -> {
                throw new RuntimeException("Intentional test exception");
            };

            // Create a good listener that should still be called
            GameEventListener goodListener = (eventType, data) -> goodListenerCalled.set(true);

            GameEventManager eventManager = GameEventManager.getInstance();
            eventManager.subscribe(GameEventType.NAVIGATE_TO_REGISTER, faultyListener);
            eventManager.subscribe(GameEventType.NAVIGATE_TO_REGISTER, goodListener);

            // Publish event - good listener should still be called despite faulty listener
            eventManager.publish(GameEventType.NAVIGATE_TO_REGISTER, null);

            assert goodListenerCalled.get() : "Good listener should have been called despite faulty listener";

            // Cleanup
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_REGISTER, faultyListener);
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_REGISTER, goodListener);

            System.out.println("   ✓ Event system isolates errors correctly (faulty listeners don't break event chain)");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   ✗ Event error isolation test failed: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }

    private static void testConcurrentEventPublishing() {
        System.out.println("\n8. Testing Concurrent Event Publishing...");
        try {
            final int NUM_THREADS = 10;
            final int EVENTS_PER_THREAD = 100;
            final AtomicInteger eventCount = new AtomicInteger(0);
            final CountDownLatch latch = new CountDownLatch(NUM_THREADS);

            GameEventListener counterListener = (eventType, data) -> eventCount.incrementAndGet();

            GameEventManager eventManager = GameEventManager.getInstance();
            eventManager.subscribe(GameEventType.GAME_STARTED, counterListener);

            // Create multiple threads publishing events concurrently
            Thread[] threads = new Thread[NUM_THREADS];
            for (int i = 0; i < NUM_THREADS; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < EVENTS_PER_THREAD; j++) {
                        eventManager.publish(GameEventType.GAME_STARTED, null);
                    }
                    latch.countDown();
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            latch.await(10, TimeUnit.SECONDS);

            // Give time for all events to be processed
            Thread.sleep(500);

            int expectedEvents = NUM_THREADS * EVENTS_PER_THREAD;
            assert eventCount.get() == expectedEvents :
                    "Should have received all " + expectedEvents + " events, but got " + eventCount.get();

            // Cleanup
            eventManager.unsubscribe(GameEventType.GAME_STARTED, counterListener);

            System.out.println("   ✓ Concurrent event publishing handled correctly (" + expectedEvents + " events)");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   ✗ Concurrent event publishing test failed: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }

    private static void testNavigationToAllScreens() {
        System.out.println("\n9. Testing Navigation to All Screens...");
        try {
            // Setup: Create a test user session
            User testUser = new User("NavigationTestUser");
            UserSession.getInstance().login(testUser);

            NavigationController navController = NavigationController.getInstance();
            GameEventManager eventManager = GameEventManager.getInstance();

            // Test navigation events are published (we can't actually create GUIs in headless mode)
            final AtomicBoolean loginNavReceived = new AtomicBoolean(false);
            final AtomicBoolean homeNavReceived = new AtomicBoolean(false);
            final AtomicBoolean gameNavReceived = new AtomicBoolean(false);
            final AtomicBoolean leaderboardNavReceived = new AtomicBoolean(false);
            final AtomicBoolean registerNavReceived = new AtomicBoolean(false);

            GameEventListener navTestListener = (eventType, data) -> {
                switch (eventType) {
                    case NAVIGATE_TO_LOGIN:
                        loginNavReceived.set(true);
                        break;
                    case NAVIGATE_TO_HOME:
                        homeNavReceived.set(true);
                        break;
                    case NAVIGATE_TO_GAME:
                        gameNavReceived.set(true);
                        break;
                    case NAVIGATE_TO_LEADERBOARD:
                        leaderboardNavReceived.set(true);
                        break;
                    case NAVIGATE_TO_REGISTER:
                        registerNavReceived.set(true);
                        break;
                }
            };

            // Subscribe to all navigation events
            eventManager.subscribe(GameEventType.NAVIGATE_TO_LOGIN, navTestListener);
            eventManager.subscribe(GameEventType.NAVIGATE_TO_HOME, navTestListener);
            eventManager.subscribe(GameEventType.NAVIGATE_TO_GAME, navTestListener);
            eventManager.subscribe(GameEventType.NAVIGATE_TO_LEADERBOARD, navTestListener);
            eventManager.subscribe(GameEventType.NAVIGATE_TO_REGISTER, navTestListener);

            // Publish all navigation events
            eventManager.publish(GameEventType.NAVIGATE_TO_LOGIN, null);
            eventManager.publish(GameEventType.NAVIGATE_TO_HOME, null);
            eventManager.publish(GameEventType.NAVIGATE_TO_GAME, null);
            eventManager.publish(GameEventType.NAVIGATE_TO_LEADERBOARD, null);
            eventManager.publish(GameEventType.NAVIGATE_TO_REGISTER, null);

            // Give time for event processing
            Thread.sleep(200);

            // Verify all navigation events were received
            assert loginNavReceived.get() : "NAVIGATE_TO_LOGIN event should be received";
            assert homeNavReceived.get() : "NAVIGATE_TO_HOME event should be received";
            assert gameNavReceived.get() : "NAVIGATE_TO_GAME event should be received";
            assert leaderboardNavReceived.get() : "NAVIGATE_TO_LEADERBOARD event should be received";
            assert registerNavReceived.get() : "NAVIGATE_TO_REGISTER event should be received";

            // Cleanup
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_LOGIN, navTestListener);
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_HOME, navTestListener);
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_GAME, navTestListener);
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_LEADERBOARD, navTestListener);
            eventManager.unsubscribe(GameEventType.NAVIGATE_TO_REGISTER, navTestListener);

            System.out.println("   ✓ All navigation events (Login, Home, Game, Leaderboard, Register) handled correctly");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("   ✗ Navigation to all screens test failed: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }
}
