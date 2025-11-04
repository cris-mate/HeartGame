package com.heartgame.controller;

import com.heartgame.event.GameEventManager;
import com.heartgame.event.GameEventType;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.view.HomeGUI;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HomeController
 * Tests navigation actions and event publishing
 * Uses real HomeGUI in headless mode (KISS approach)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HomeControllerTest {

    private HomeGUI homeView;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        ControllerTestHelper.initializeTestEnvironment();
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() throws SQLException {
        ControllerTestHelper.cleanupBeforeEachTest();

        // Login a user for testing
        User testUser = new User("homeTest");
        UserSession.getInstance().login(testUser);

        homeView = new HomeGUI();
    }

    @AfterEach
    void tearDown() {
        if (homeView != null) {
            homeView.dispose();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Start Game button publishes NAVIGATE_TO_GAME event")
    void testStartGamePublishesEvent() {
        AtomicBoolean eventReceived = new AtomicBoolean(false);
        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_GAME, (eventType, data) -> eventReceived.set(true));
        homeView.getStartGameButton().doClick();
        assertTrue(eventReceived.get());
    }

    @Test
    @Order(2)
    @DisplayName("Leaderboard button publishes NAVIGATE_TO_LEADERBOARD event")
    void testLeaderboardPublishesEvent() {
        AtomicBoolean eventReceived = new AtomicBoolean(false);
        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_LEADERBOARD, (eventType, data) -> eventReceived.set(true));
        homeView.getLeaderboardButton().doClick();
        assertTrue(eventReceived.get());
    }

    @Test
    @Order(3)
    @DisplayName("Start Game does not publish event when no user logged in")
    void testStartGameWithoutUser() {
        UserSession.getInstance().logout();
        HomeGUI newView = new HomeGUI();
        AtomicBoolean eventReceived = new AtomicBoolean(false);
        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_GAME, (eventType, data) -> eventReceived.set(true));

        newView.getStartGameButton().doClick();
        newView.dispose();

        assertFalse(eventReceived.get());
    }

    @Test
    @Order(4)
    @DisplayName("All buttons are accessible")
    void testButtonsAccessible() {
        assertNotNull(homeView.getStartGameButton());
        assertNotNull(homeView.getLeaderboardButton());
        assertNotNull(homeView.getLogoutButton());
        assertNotNull(homeView.getExitButton());
    }
}