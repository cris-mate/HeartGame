package com.heartgame.integration;

import com.heartgame.controller.ControllerTestHelper;
import com.heartgame.controller.GUITestHelper;
import com.heartgame.event.GameEventManager;
import com.heartgame.event.GameEventType;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.persistence.UserDAO;
import com.heartgame.view.RegisterGUI;
import com.heartgame.view.LoginGUI;
import com.heartgame.view.HomeGUI;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for authentication and navigation flow
 * Tests the complete user journey: Register → Login → Home → Navigation
 * Uses real GUIs in headless mode with H2 database (KISS approach)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthenticationFlowIntegrationTest {

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        ControllerTestHelper.initializeTestEnvironment();
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() throws SQLException {
        ControllerTestHelper.cleanupBeforeEachTest();
    }

    @Test
    @Order(1)
    @DisplayName("Integration: Register → Navigate to Login")
    void testCompleteRegistrationFlow() {
        RegisterGUI registerView = new RegisterGUI();

        GUITestHelper.setTextField(registerView, "usernameField", "integrationUser");
        GUITestHelper.setTextField(registerView, "emailField", "integration@example.com");
        GUITestHelper.setPasswordField(registerView, "passwordField", "password123");
        GUITestHelper.setPasswordField(registerView, "confirmPasswordField", "password123");

        AtomicBoolean navigationEventReceived = new AtomicBoolean(false);
        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_LOGIN, (eventType, data) -> navigationEventReceived.set(true));

        registerView.getRegisterButton().doClick();

        UserDAO userDAO = new UserDAO();
        assertTrue(userDAO.usernameExists("integrationUser"));
        assertTrue(navigationEventReceived.get());

        registerView.dispose();
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Login → Session Created → Navigate to Home")
    void testCompleteLoginFlow() {
        UserDAO userDAO = new UserDAO();
        User user = new User("loginFlowUser");
        userDAO.createUser(user, "password123");

        AtomicBoolean loginEventReceived = new AtomicBoolean(false);
        AtomicBoolean homeEventReceived = new AtomicBoolean(false);
        AtomicReference<User> loggedInUser = new AtomicReference<>();

        GameEventManager.getInstance().subscribe(GameEventType.PLAYER_LOGGED_IN, (eventType, data) -> {
            loginEventReceived.set(true);
            loggedInUser.set((User) data);
        });

        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_HOME, (eventType, data) -> homeEventReceived.set(true));

        LoginGUI loginView = new LoginGUI();
        GUITestHelper.setTextField(loginView, "usernameField", "loginFlowUser");
        GUITestHelper.setPasswordField(loginView, "passwordField", "password123");

        loginView.getLoginButton().doClick();

        assertTrue(loginEventReceived.get());
        assertTrue(homeEventReceived.get());
        assertNotNull(loggedInUser.get());
        assertEquals("loginFlowUser", loggedInUser.get().getUsername());

        assertTrue(UserSession.getInstance().isLoggedIn());
        assertEquals("loginFlowUser", UserSession.getInstance().getCurrentUser().getUsername());

        loginView.dispose();
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Complete Registration → Login → Home Flow")
    void testCompleteUserJourney() {
        // STEP 1: Registration
        RegisterGUI registerView = new RegisterGUI();
        GUITestHelper.setTextField(registerView, "usernameField", "journeyUser");
        GUITestHelper.setTextField(registerView, "emailField", "journey@example.com");
        GUITestHelper.setPasswordField(registerView, "passwordField", "password123");
        GUITestHelper.setPasswordField(registerView, "confirmPasswordField", "password123");

        AtomicBoolean registerNavigationReceived = new AtomicBoolean(false);
        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_LOGIN, (eventType, data) -> registerNavigationReceived.set(true));

        registerView.getRegisterButton().doClick();
        assertTrue(registerNavigationReceived.get());
        registerView.dispose();

        // STEP 2: Login
        LoginGUI loginView = new LoginGUI();
        GUITestHelper.setTextField(loginView, "usernameField", "journeyUser");
        GUITestHelper.setPasswordField(loginView, "passwordField", "password123");

        AtomicBoolean loginEventReceived = new AtomicBoolean(false);
        AtomicBoolean homeNavigationReceived = new AtomicBoolean(false);

        GameEventManager.getInstance().subscribe(GameEventType.PLAYER_LOGGED_IN, (eventType, data) -> loginEventReceived.set(true));

        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_HOME, (eventType, data) -> homeNavigationReceived.set(true));

        loginView.getLoginButton().doClick();
        assertTrue(loginEventReceived.get());
        assertTrue(homeNavigationReceived.get());
        loginView.dispose();

        // STEP 3: Home Actions
        HomeGUI homeView = new HomeGUI();
        AtomicBoolean gameNavigationReceived = new AtomicBoolean(false);

        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_GAME, (eventType, data) -> gameNavigationReceived.set(true));

        homeView.getStartGameButton().doClick();
        assertTrue(gameNavigationReceived.get());
        homeView.dispose();
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Multiple event subscribers receive same event")
    void testMultipleEventSubscribers() {
        AtomicInteger subscriber1Count = new AtomicInteger(0);
        AtomicInteger subscriber2Count = new AtomicInteger(0);
        AtomicInteger subscriber3Count = new AtomicInteger(0);

        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_LOGIN, (eventType, data) -> subscriber1Count.incrementAndGet());

        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_LOGIN, (eventType, data) -> subscriber2Count.incrementAndGet());

        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_LOGIN, (eventType, data) -> subscriber3Count.incrementAndGet());

        RegisterGUI registerView = new RegisterGUI();
        GUITestHelper.setTextField(registerView, "usernameField", "eventTest");
        GUITestHelper.setTextField(registerView, "emailField", "event@test.com");
        GUITestHelper.setPasswordField(registerView, "passwordField", "password123");
        GUITestHelper.setPasswordField(registerView, "confirmPasswordField", "password123");

        registerView.getRegisterButton().doClick();

        assertEquals(1, subscriber1Count.get());
        assertEquals(1, subscriber2Count.get());
        assertEquals(1, subscriber3Count.get());

        registerView.dispose();
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Failed registration doesn't corrupt database")
    void testFailedRegistrationRollback() throws SQLException {
        int initialUserCount = countUsers();

        RegisterGUI registerView = new RegisterGUI();
        GUITestHelper.setTextField(registerView, "usernameField", "ab");
        GUITestHelper.setTextField(registerView, "emailField", "invalid");
        GUITestHelper.setPasswordField(registerView, "passwordField", "pass");
        GUITestHelper.setPasswordField(registerView, "confirmPasswordField", "different");

        registerView.getRegisterButton().doClick();

        int finalUserCount = countUsers();
        assertEquals(initialUserCount, finalUserCount);

        registerView.dispose();
    }

    @Test
    @Order(6)
    @DisplayName("Integration: Failed login doesn't create session")
    void testFailedLoginNoSession() {
        UserDAO userDAO = new UserDAO();
        User user = new User("failLogin");
        userDAO.createUser(user, "correctPass");

        LoginGUI loginView = new LoginGUI();
        GUITestHelper.setTextField(loginView, "usernameField", "failLogin");
        GUITestHelper.setPasswordField(loginView, "passwordField", "wrongPass");

        loginView.getLoginButton().doClick();

        assertFalse(UserSession.getInstance().isLoggedIn());
        assertNull(UserSession.getInstance().getCurrentUser());

        loginView.dispose();
    }

    @Test
    @Order(7)
    @DisplayName("Integration: Multiple registrations work sequentially")
    void testMultipleRegistrations() {
        String[] usernames = {"user1", "user2", "user3"};
        UserDAO userDAO = new UserDAO();

        for (String username : usernames) {
            RegisterGUI registerView = new RegisterGUI();
            GUITestHelper.setTextField(registerView, "usernameField", username);
            GUITestHelper.setTextField(registerView, "emailField", username + "@example.com");
            GUITestHelper.setPasswordField(registerView, "passwordField", "password123");
            GUITestHelper.setPasswordField(registerView, "confirmPasswordField", "password123");

            registerView.getRegisterButton().doClick();
            registerView.dispose();
        }

        for (String username : usernames) {
            assertTrue(userDAO.usernameExists(username));
        }
    }

    private int countUsers() throws SQLException {
        var stmt = ControllerTestHelper.getConnection().createStatement();
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        stmt.close();
        return count;
    }
}