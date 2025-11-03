package com.heartgame.controller;

import com.heartgame.event.GameEventManager;
import com.heartgame.event.GameEventType;
import com.heartgame.model.User;
import com.heartgame.model.UserSession;
import com.heartgame.persistence.UserDAO;
import com.heartgame.view.LoginGUI;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LoginController
 * Tests password-based login, validation, and event publishing
 * Uses real LoginGUI in headless mode with reflection (KISS approach)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginControllerTest {

    private LoginGUI loginView;
    private UserDAO userDAO;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        ControllerTestHelper.initializeTestEnvironment();
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() throws SQLException {
        ControllerTestHelper.cleanupBeforeEachTest();
        userDAO = new UserDAO();
        loginView = new LoginGUI();
    }

    @AfterEach
    void tearDown() {
        if (loginView != null) {
            loginView.dispose();
        }
    }

    private void setLoginData(String username, String password) {
        GUITestHelper.setTextField(loginView, "usernameField", username);
        GUITestHelper.setPasswordField(loginView, "passwordField", password);
    }

    @Test
    @Order(1)
    @DisplayName("Login succeeds with correct credentials")
    void testSuccessfulPasswordLogin() {
        User user = new User("testUser");
        userDAO.createUser(user, "password123");

        setLoginData("testUser", "password123");
        loginView.getLoginButton().doClick();

        assertTrue(UserSession.getInstance().isLoggedIn());
        assertEquals("testUser", UserSession.getInstance().getCurrentUser().getUsername());
    }

    @Test
    @Order(2)
    @DisplayName("Login fails with wrong password")
    void testWrongPassword() {
        User user = new User("testUser2");
        userDAO.createUser(user, "correctPassword");

        setLoginData("testUser2", "wrongPassword");
        loginView.getLoginButton().doClick();

        assertFalse(UserSession.getInstance().isLoggedIn());
    }

    @Test
    @Order(3)
    @DisplayName("Login fails with non-existent username")
    void testNonExistentUser() {
        setLoginData("ghostUser", "anyPassword");
        loginView.getLoginButton().doClick();

        assertFalse(UserSession.getInstance().isLoggedIn());
    }

    @Test
    @Order(4)
    @DisplayName("Login fails with empty username")
    void testEmptyUsername() {
        setLoginData("", "password123");
        loginView.getLoginButton().doClick();

        assertFalse(UserSession.getInstance().isLoggedIn());
    }

    @Test
    @Order(5)
    @DisplayName("Login fails with empty password")
    void testEmptyPassword() {
        setLoginData("testUser", "");
        loginView.getLoginButton().doClick();

        assertFalse(UserSession.getInstance().isLoggedIn());
    }

    @Test
    @Order(6)
    @DisplayName("Successful login publishes PLAYER_LOGGED_IN event")
    void testPlayerLoggedInEvent() {
        AtomicBoolean eventReceived = new AtomicBoolean(false);
        AtomicReference<User> eventUser = new AtomicReference<>();

        GameEventManager.getInstance().subscribe(GameEventType.PLAYER_LOGGED_IN, (eventType, data) -> {
            eventReceived.set(true);
            eventUser.set((User) data);
        });

        User user = new User("eventTest");
        userDAO.createUser(user, "password123");

        setLoginData("eventTest", "password123");
        loginView.getLoginButton().doClick();

        assertTrue(eventReceived.get());
        assertNotNull(eventUser.get());
        assertEquals("eventTest", eventUser.get().getUsername());
    }

    @Test
    @Order(7)
    @DisplayName("Successful login publishes NAVIGATE_TO_HOME event")
    void testNavigateToHomeEvent() {
        AtomicBoolean eventReceived = new AtomicBoolean(false);

        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_HOME, (eventType, data) -> eventReceived.set(true));

        User user = new User("navTest");
        userDAO.createUser(user, "password123");

        setLoginData("navTest", "password123");
        loginView.getLoginButton().doClick();

        assertTrue(eventReceived.get());
    }

    @Test
    @Order(8)
    @DisplayName("Successful login updates last_login timestamp")
    void testLastLoginUpdated() throws SQLException {
        User user = new User("loginTime");
        userDAO.createUser(user, "password123");

        var stmt = ControllerTestHelper.getConnection().createStatement();
        var rs = stmt.executeQuery("SELECT last_login FROM users WHERE username = 'loginTime'");
        assertTrue(rs.next());
        assertNull(rs.getTimestamp("last_login"));
        rs.close();
        stmt.close();

        setLoginData("loginTime", "password123");
        loginView.getLoginButton().doClick();

        stmt = ControllerTestHelper.getConnection().createStatement();
        rs = stmt.executeQuery("SELECT last_login FROM users WHERE username = 'loginTime'");
        assertTrue(rs.next());
        assertNotNull(rs.getTimestamp("last_login"));
        rs.close();
        stmt.close();
    }

    @Test
    @Order(9)
    @DisplayName("Login handles whitespace in username")
    void testUsernameWithWhitespace() {
        User user = new User("spaceUser");
        userDAO.createUser(user, "password123");

        setLoginData("  spaceUser  ", "password123");
        loginView.getLoginButton().doClick();

        assertTrue(UserSession.getInstance().isLoggedIn());
    }

    @Test
    @Order(10)
    @DisplayName("Login works with special characters in password")
    void testSpecialCharactersInPassword() {
        User user = new User("special");
        String specialPass = "P@ssw0rd!#$%";
        userDAO.createUser(user, specialPass);

        setLoginData("special", specialPass);
        loginView.getLoginButton().doClick();

        assertTrue(UserSession.getInstance().isLoggedIn());
    }
}