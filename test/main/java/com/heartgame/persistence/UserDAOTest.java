package com.heartgame.persistence;

import com.heartgame.model.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserDAO
 * Tests all CRUD operations and authentication logic using H2 in-memory database
 * Also implicitly tests BaseDAO transaction and retry logic
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {

    private static DatabaseManager dbManager;
    private static Connection connection;
    private UserDAO userDAO;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        dbManager = DatabaseManager.getInstance();
        connection = dbManager.getConnection();
        DatabaseTestHelper.initializeSchema(connection);
    }

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseTestHelper.clearAllData(connection);
        userDAO = new UserDAO();
    }

    // ==================== findByUsername Tests ====================

    @Test
    @Order(1)
    @DisplayName("findByUsername() returns empty for non-existent user")
    void testFindByUsernameNotFound() {
        Optional<User> result = userDAO.findByUsername("nonexistent");

        assertTrue(result.isEmpty(), "Should return empty Optional for non-existent user");
    }

    @Test
    @Order(2)
    @DisplayName("findByUsername() returns user when exists")
    void testFindByUsernameFound() {
        // Arrange: Create a user
        User user = new User("testUser");
        userDAO.createUser(user, "password123");

        // Act
        Optional<User> result = userDAO.findByUsername("testUser");

        // Assert
        assertTrue(result.isPresent(), "Should find existing user");
        assertEquals("testUser", result.get().getUsername());
    }

    @Test
    @Order(3)
    @DisplayName("findByUsername() is case-sensitive")
    void testFindByUsernameCaseSensitive() {
        User user = new User("TestUser");
        userDAO.createUser(user, "password123");

        Optional<User> result = userDAO.findByUsername("testUser");

        assertTrue(result.isEmpty(), "Should not find user with different case");
    }

    @Test
    @Order(4)
    @DisplayName("findByUsername() returns user with all fields populated")
    void testFindByUsernameReturnsCompleteUser() {
        User user = new User("john", "john@example.com", "google", "google123");
        userDAO.createUser(user, null);

        Optional<User> result = userDAO.findByUsername("john");

        assertTrue(result.isPresent());
        User found = result.get();
        assertEquals("john", found.getUsername());
        assertEquals("john@example.com", found.getEmail());
        assertEquals("google", found.getOauthProvider());
        assertEquals("google123", found.getOauthId());
        assertTrue(found.getId() > 0, "Should have generated ID");
    }

    // ==================== findByOAuthId Tests ====================

    @Test
    @Order(5)
    @DisplayName("findByOAuthId() returns empty for non-existent OAuth user")
    void testFindByOAuthIdNotFound() {
        Optional<User> result = userDAO.findByOAuthId("google", "nonexistent");

        assertTrue(result.isEmpty(), "Should return empty for non-existent OAuth user");
    }

    @Test
    @Order(6)
    @DisplayName("findByOAuthId() finds OAuth user successfully")
    void testFindByOAuthIdFound() {
        User user = new User("alice", "alice@gmail.com", "google", "google789");
        userDAO.createUser(user, null);

        Optional<User> result = userDAO.findByOAuthId("google", "google789");

        assertTrue(result.isPresent(), "Should find OAuth user");
        assertEquals("alice", result.get().getUsername());
        assertEquals("google789", result.get().getOauthId());
    }

    @Test
    @Order(7)
    @DisplayName("findByOAuthId() distinguishes between providers")
    void testFindByOAuthIdDifferentProviders() {
        User user = new User("bob", "bob@example.com", "github", "gh123");
        userDAO.createUser(user, null);

        Optional<User> wrongProvider = userDAO.findByOAuthId("google", "gh123");
        Optional<User> correctProvider = userDAO.findByOAuthId("github", "gh123");

        assertTrue(wrongProvider.isEmpty(), "Should not find with wrong provider");
        assertTrue(correctProvider.isPresent(), "Should find with correct provider");
    }

    // ==================== createUser Tests ====================

    @Test
    @Order(8)
    @DisplayName("createUser() creates password-based user successfully")
    void testCreatePasswordUser() {
        User user = new User("newUser");
        boolean result = userDAO.createUser(user, "securePass123");

        assertTrue(result, "Should create user successfully");
        assertTrue(user.getId() > 0, "Should set generated ID");

        // Verify user was created
        Optional<User> found = userDAO.findByUsername("newUser");
        assertTrue(found.isPresent(), "Created user should be findable");
    }

    @Test
    @Order(9)
    @DisplayName("createUser() creates OAuth user successfully")
    void testCreateOAuthUser() {
        User user = new User("oauthUser", "oauth@example.com", "google", "goog456");
        boolean result = userDAO.createUser(user, null);

        assertTrue(result, "Should create OAuth user");
        assertTrue(user.getId() > 0, "Should set generated ID");
    }

    @Test
    @Order(10)
    @DisplayName("createUser() rejects duplicate username")
    void testCreateUserDuplicateUsername() {
        User user1 = new User("duplicate");
        userDAO.createUser(user1, "pass1");

        User user2 = new User("duplicate");
        boolean result = userDAO.createUser(user2, "pass2");

        assertFalse(result, "Should reject duplicate username");
    }

    @Test
    @Order(11)
    @DisplayName("createUser() hashes password correctly")
    void testCreateUserPasswordHashed() {
        User user = new User("hashTest");
        userDAO.createUser(user, "myPassword");

        // Password should be hashed, not stored in plain text
        // Verify by checking password verification works
        boolean verified = userDAO.verifyPassword("hashTest", "myPassword");
        assertTrue(verified, "Should verify correct password");
    }

    @Test
    @Order(12)
    @DisplayName("createUser() allows null password for OAuth users")
    void testCreateUserNullPasswordForOAuth() {
        User user = new User("oauthOnly", "oauth@test.com", "github", "gh999");
        boolean result = userDAO.createUser(user, null);

        assertTrue(result, "Should create OAuth user with null password");
    }

    @Test
    @Order(13)
    @DisplayName("createUser() allows empty password for OAuth users")
    void testCreateUserEmptyPasswordForOAuth() {
        User user = new User("oauthEmpty", "oauth2@test.com", "google", "g111");
        boolean result = userDAO.createUser(user, "");

        assertTrue(result, "Should create OAuth user with empty password");
    }

    // ==================== verifyPassword Tests ====================

    @Test
    @Order(14)
    @DisplayName("verifyPassword() returns true for correct password")
    void testVerifyPasswordCorrect() {
        User user = new User("passUser");
        userDAO.createUser(user, "correctPass");

        boolean result = userDAO.verifyPassword("passUser", "correctPass");

        assertTrue(result, "Should verify correct password");
    }

    @Test
    @Order(15)
    @DisplayName("verifyPassword() returns false for wrong password")
    void testVerifyPasswordWrong() {
        User user = new User("passUser2");
        userDAO.createUser(user, "correctPass");

        boolean result = userDAO.verifyPassword("passUser2", "wrongPass");

        assertFalse(result, "Should reject wrong password");
    }

    @Test
    @Order(16)
    @DisplayName("verifyPassword() returns false for non-existent user")
    void testVerifyPasswordNonExistentUser() {
        boolean result = userDAO.verifyPassword("ghost", "anyPassword");

        assertFalse(result, "Should return false for non-existent user");
    }

    @Test
    @Order(17)
    @DisplayName("verifyPassword() returns false for OAuth user (no password)")
    void testVerifyPasswordOAuthUser() {
        User user = new User("oauthOnly2", "oauth@test.com", "google", "g222");
        userDAO.createUser(user, null);

        boolean result = userDAO.verifyPassword("oauthOnly2", "anyPassword");

        assertFalse(result, "OAuth users should not verify with password");
    }

    @Test
    @Order(18)
    @DisplayName("verifyPassword() is case-sensitive for password")
    void testVerifyPasswordCaseSensitive() {
        User user = new User("caseUser");
        userDAO.createUser(user, "Password123");

        boolean result = userDAO.verifyPassword("caseUser", "password123");

        assertFalse(result, "Password verification should be case-sensitive");
    }

    // ==================== usernameExists Tests ====================

    @Test
    @Order(19)
    @DisplayName("usernameExists() returns false for non-existent user")
    void testUsernameExistsNonExistent() {
        boolean exists = userDAO.usernameExists("ghost");

        assertFalse(exists, "Should return false for non-existent username");
    }

    @Test
    @Order(20)
    @DisplayName("usernameExists() returns true for existing user")
    void testUsernameExistsExisting() {
        User user = new User("existing");
        userDAO.createUser(user, "pass");

        boolean exists = userDAO.usernameExists("existing");

        assertTrue(exists, "Should return true for existing username");
    }

    // ==================== updateLastLogin Tests ====================

    @Test
    @Order(21)
    @DisplayName("updateLastLogin() updates timestamp successfully")
    void testUpdateLastLogin() throws SQLException {
        // Create user
        User user = new User("loginUser");
        userDAO.createUser(user, "pass");

        // Update last login
        userDAO.updateLastLogin("loginUser");

        // Verify last_login was set (check database directly)
        var stmt = connection.createStatement();
        var rs = stmt.executeQuery("SELECT last_login FROM users WHERE username = 'loginUser'");
        assertTrue(rs.next(), "User should exist");
        assertNotNull(rs.getTimestamp("last_login"), "last_login should be set");
        rs.close();
        stmt.close();
    }

    @Test
    @Order(22)
    @DisplayName("updateLastLogin() can be called multiple times")
    void testUpdateLastLoginMultipleTimes() {
        User user = new User("multiLogin");
        userDAO.createUser(user, "pass");

        assertDoesNotThrow(() -> {
            userDAO.updateLastLogin("multiLogin");
            userDAO.updateLastLogin("multiLogin");
            userDAO.updateLastLogin("multiLogin");
        }, "Should handle multiple login updates");
    }

    @Test
    @Order(23)
    @DisplayName("updateLastLogin() handles non-existent user gracefully")
    void testUpdateLastLoginNonExistentUser() {
        assertDoesNotThrow(() -> userDAO.updateLastLogin("ghost"), "Should not throw for non-existent user");
    }

    // ==================== Integration Tests (Testing BaseDAO) ====================

    @Test
    @Order(24)
    @DisplayName("Transaction rollback on createUser failure")
    void testTransactionRollback() throws SQLException {
        // Create first user
        User user1 = new User("txUser");
        userDAO.createUser(user1, "pass");

        int countBefore = DatabaseTestHelper.countRows(connection, "users");

        // Try to create duplicate (should fail and rollback)
        User user2 = new User("txUser");
        boolean result = userDAO.createUser(user2, "pass2");

        int countAfter = DatabaseTestHelper.countRows(connection, "users");

        assertFalse(result, "Duplicate creation should fail");
        assertEquals(countBefore, countAfter, "Row count should not change (rollback)");
    }

    @Test
    @Order(25)
    @DisplayName("Multiple operations in sequence work correctly")
    void testMultipleOperations() {
        // Create
        User user = new User("multi");
        assertTrue(userDAO.createUser(user, "pass123"));

        // Find
        Optional<User> found = userDAO.findByUsername("multi");
        assertTrue(found.isPresent());

        // Verify password
        assertTrue(userDAO.verifyPassword("multi", "pass123"));

        // Update login
        assertDoesNotThrow(() -> userDAO.updateLastLogin("multi"));

        // Check existence
        assertTrue(userDAO.usernameExists("multi"));
    }

    @Test
    @Order(26)
    @DisplayName("Can create multiple users successfully")
    void testCreateMultipleUsers() {
        User user1 = new User("user1");
        User user2 = new User("user2");
        User user3 = new User("user3");

        assertTrue(userDAO.createUser(user1, "pass1"));
        assertTrue(userDAO.createUser(user2, "pass2"));
        assertTrue(userDAO.createUser(user3, "pass3"));

        assertTrue(userDAO.usernameExists("user1"));
        assertTrue(userDAO.usernameExists("user2"));
        assertTrue(userDAO.usernameExists("user3"));
    }

    @Test
    @Order(27)
    @DisplayName("Mixed OAuth and password users coexist")
    void testMixedUserTypes() {
        User passwordUser = new User("passUser");
        User oauthUser = new User("oauthUser", "oauth@test.com", "google", "g999");

        assertTrue(userDAO.createUser(passwordUser, "password"));
        assertTrue(userDAO.createUser(oauthUser, null));

        assertTrue(userDAO.verifyPassword("passUser", "password"));
        assertFalse(userDAO.verifyPassword("oauthUser", "password"));

        Optional<User> oauthFound = userDAO.findByOAuthId("google", "g999");
        assertTrue(oauthFound.isPresent());
    }

    @Test
    @Order(28)
    @DisplayName("User with special characters in username")
    void testSpecialCharactersInUsername() {
        User user = new User("user_123-test");
        boolean result = userDAO.createUser(user, "pass");

        assertTrue(result, "Should handle special characters in username");
        assertTrue(userDAO.usernameExists("user_123-test"));
    }

    @Test
    @Order(29)
    @DisplayName("User with special characters in password")
    void testSpecialCharactersInPassword() {
        User user = new User("specialPass");
        String password = "p@ssw0rd!#$%";
        userDAO.createUser(user, password);

        assertTrue(userDAO.verifyPassword("specialPass", password));
        assertFalse(userDAO.verifyPassword("specialPass", "wrongPass"));
    }

    @Test
    @Order(30)
    @DisplayName("Null and empty email handling")
    void testNullAndEmptyEmail() {
        User user1 = new User("nullEmail", null, "google", "g123");
        User user2 = new User("emptyEmail", "", "google", "g456");

        assertTrue(userDAO.createUser(user1, null));
        assertTrue(userDAO.createUser(user2, null));
    }
}
