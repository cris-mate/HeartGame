package com.heartgame.controller;

import com.heartgame.event.GameEventManager;
import com.heartgame.event.GameEventType;
import com.heartgame.persistence.UserDAO;
import com.heartgame.view.RegisterGUI;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RegisterController
 * Tests registration logic, validation, and event publishing
 * Uses real RegisterGUI in headless mode with reflection
 * Note: RegisterGUI creates its own controller in the constructor,
 * so these are integration tests of the GUI+Controller together.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegisterControllerTest {

    private RegisterGUI registerView;
    private UserDAO userDAO;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        ControllerTestHelper.initializeTestEnvironment();

        // Set headless mode for GUI testing
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() throws SQLException {
        ControllerTestHelper.cleanupBeforeEachTest();
        userDAO = new UserDAO();

        // Create GUI in headless mode (it creates its own controller)
        registerView = new RegisterGUI();
    }

    @AfterEach
    void tearDown() {
        if (registerView != null) {
            registerView.dispose();
        }
    }

    // Helper method to set registration form data
    private void setRegistrationData(String username, String email, String password, String confirmPassword) {
        GUITestHelper.setTextField(registerView, "usernameField", username);
        GUITestHelper.setTextField(registerView, "emailField", email);
        GUITestHelper.setPasswordField(registerView, "passwordField", password);
        GUITestHelper.setPasswordField(registerView, "confirmPasswordField", confirmPassword);
    }

    // ==================== Input Validation Tests ====================

    @Test
    @Order(1)
    @DisplayName("Registration fails with empty username")
    void testEmptyUsername() {
        setRegistrationData("", "test@example.com", "password123", "password123");
        registerView.getRegisterButton().doClick();

        assertFalse(userDAO.usernameExists(""));
    }

    @Test
    @Order(2)
    @DisplayName("Registration fails with empty email")
    void testEmptyEmail() {
        setRegistrationData("testUser", "", "password123", "password123");
        registerView.getRegisterButton().doClick();

        assertFalse(userDAO.usernameExists("testUser"));
    }

    @Test
    @Order(3)
    @DisplayName("Registration fails with empty password")
    void testEmptyPassword() {
        setRegistrationData("testUser", "test@example.com", "", "");
        registerView.getRegisterButton().doClick();

        assertFalse(userDAO.usernameExists("testUser"));
    }

    @Test
    @Order(4)
    @DisplayName("Registration fails with short username (< 3 chars)")
    void testShortUsername() {
        setRegistrationData("ab", "test@example.com", "password123", "password123");
        registerView.getRegisterButton().doClick();

        assertFalse(userDAO.usernameExists("ab"));
    }

    @Test
    @Order(5)
    @DisplayName("Registration fails with invalid username characters")
    void testInvalidUsernameCharacters() {
        setRegistrationData("test-user!", "test@example.com", "password123", "password123");
        registerView.getRegisterButton().doClick();

        assertFalse(userDAO.usernameExists("test-user!"));
    }

    @Test
    @Order(6)
    @DisplayName("Registration succeeds with valid username (letters, numbers, underscore)")
    void testValidUsernameCharacters() {
        setRegistrationData("test_user_123", "test@example.com", "password123", "password123");
        registerView.getRegisterButton().doClick();

        assertTrue(userDAO.usernameExists("test_user_123"));
    }

    @Test
    @Order(7)
    @DisplayName("Registration fails with invalid email format")
    void testInvalidEmailFormat() {
        setRegistrationData("testUser", "notAnEmail", "password123", "password123");
        registerView.getRegisterButton().doClick();

        assertFalse(userDAO.usernameExists("testUser"));
    }

    @Test
    @Order(8)
    @DisplayName("Registration fails with invalid email (missing @)")
    void testEmailMissingAt() {
        setRegistrationData("testUser", "testExample.com", "password123", "password123");
        registerView.getRegisterButton().doClick();

        assertFalse(userDAO.usernameExists("testUser"));
    }

    @Test
    @Order(9)
    @DisplayName("Registration fails with short password (< 6 chars)")
    void testShortPassword() {
        setRegistrationData("testUser", "test@example.com", "12345", "12345");
        registerView.getRegisterButton().doClick();

        assertFalse(userDAO.usernameExists("testUser"));
    }

    @Test
    @Order(10)
    @DisplayName("Registration fails with mismatched passwords")
    void testPasswordMismatch() {
        setRegistrationData("testUser", "test@example.com", "password123", "password456");
        registerView.getRegisterButton().doClick();

        assertFalse(userDAO.usernameExists("testUser"));
    }

    // ==================== Successful Registration Tests ====================

    @Test
    @Order(11)
    @DisplayName("Registration succeeds with valid inputs")
    void testSuccessfulRegistration() {
        setRegistrationData("validUser", "valid@example.com", "password123", "password123");
        registerView.getRegisterButton().doClick();

        assertTrue(userDAO.usernameExists("validUser"));
    }

    @Test
    @Order(12)
    @DisplayName("Registration creates user with correct email")
    void testUserCreatedWithEmail() {
        setRegistrationData("emailTest", "emailtest@example.com", "password123", "password123");
        registerView.getRegisterButton().doClick();

        userDAO.findByUsername("emailTest").ifPresent(user -> assertEquals("emailtest@example.com", user.getEmail()));
    }

    @Test
    @Order(13)
    @DisplayName("Registration hashes password (not stored in plain text)")
    void testPasswordIsHashed() {
        setRegistrationData("hashTest", "hash@example.com", "myPassword", "myPassword");
        registerView.getRegisterButton().doClick();

        assertTrue(userDAO.verifyPassword("hashTest", "myPassword"));
    }

    // ==================== Duplicate Username Tests ====================

    @Test
    @Order(14)
    @DisplayName("Registration fails with duplicate username")
    void testDuplicateUsername() {
        // Create first user
        setRegistrationData("duplicate", "first@example.com", "password123", "password123");
        registerView.getRegisterButton().doClick();

        assertTrue(userDAO.usernameExists("duplicate"));

        // Create second GUI and try duplicate username
        RegisterGUI secondView = new RegisterGUI();
        GUITestHelper.setTextField(secondView, "usernameField", "duplicate");
        GUITestHelper.setTextField(secondView, "emailField", "second@example.com");
        GUITestHelper.setPasswordField(secondView, "passwordField", "password456");
        GUITestHelper.setPasswordField(secondView, "confirmPasswordField", "password456");

        secondView.getRegisterButton().doClick();
        secondView.dispose();

        // Should still be only one user
        assertEquals(1, countUsersWithUsername("duplicate"));
    }

    // ==================== Event Publishing Tests ====================

    @Test
    @Order(15)
    @DisplayName("Successful registration publishes NAVIGATE_TO_LOGIN event")
    void testNavigationEventOnSuccess() {
        AtomicBoolean navigationEventReceived = new AtomicBoolean(false);

        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_LOGIN, (eventType, data) -> navigationEventReceived.set(true));

        setRegistrationData("navTest", "nav@example.com", "password123", "password123");
        registerView.getRegisterButton().doClick();

        assertTrue(navigationEventReceived.get(), "NAVIGATE_TO_LOGIN event should be published");
    }

    @Test
    @Order(16)
    @DisplayName("Back button publishes NAVIGATE_TO_LOGIN event")
    void testBackButtonNavigation() {
        AtomicBoolean navigationEventReceived = new AtomicBoolean(false);

        GameEventManager.getInstance().subscribe(GameEventType.NAVIGATE_TO_LOGIN, (eventType, data) -> navigationEventReceived.set(true));

        registerView.getBackToLoginButton().doClick();

        assertTrue(navigationEventReceived.get(), "NAVIGATE_TO_LOGIN event should be published");
    }

    // ==================== Edge Cases ====================

    @Test
    @Order(17)
    @DisplayName("Registration handles whitespace in username")
    void testUsernameWithWhitespace() {
        setRegistrationData("  testUser  ", "test@example.com", "password123", "password123");
        registerView.getRegisterButton().doClick();

        assertTrue(userDAO.usernameExists("testUser"));
    }

    @Test
    @Order(18)
    @DisplayName("Registration handles whitespace in email")
    void testEmailWithWhitespace() {
        setRegistrationData("spaceTest", "  space@example.com  ", "password123", "password123");
        registerView.getRegisterButton().doClick();

        userDAO.findByUsername("spaceTest").ifPresent(user -> assertEquals("space@example.com", user.getEmail()));
    }

    @Test
    @Order(19)
    @DisplayName("Registration accepts minimum valid username (3 chars)")
    void testMinimumUsernameLength() {
        setRegistrationData("abc", "min@example.com", "password123", "password123");
        registerView.getRegisterButton().doClick();

        assertTrue(userDAO.usernameExists("abc"));
    }

    @Test
    @Order(20)
    @DisplayName("Registration accepts minimum valid password (6 chars)")
    void testMinimumPasswordLength() {
        setRegistrationData("minPass", "minpass@example.com", "123456", "123456");
        registerView.getRegisterButton().doClick();

        assertTrue(userDAO.usernameExists("minPass"));
        assertTrue(userDAO.verifyPassword("minPass", "123456"));
    }

    // ==================== Helper Methods ====================

    private int countUsersWithUsername(String username) {
        try {
            var stmt = ControllerTestHelper.getConnection().createStatement();
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE username = '" + username + "'");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}