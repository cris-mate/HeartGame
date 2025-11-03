package com.heartgame.persistence;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DatabaseManager
 * Tests connection management, health checks, and reconnection logic
 * Uses H2 in-memory database
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseManagerTest {

    private DatabaseManager dbManager;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dbManager = DatabaseManager.getInstance();
        connection = dbManager.getConnection();
        assertNotNull(connection, "Connection should be initialized");

        // Initialize schema for tests
        DatabaseTestHelper.initializeSchema(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            DatabaseTestHelper.clearAllData(connection);
        }
    }

    // ==================== Singleton Pattern Tests ====================

    @Test
    @Order(1)
    @DisplayName("getInstance() returns same instance (singleton)")
    void testSingletonPattern() {
        DatabaseManager instance1 = DatabaseManager.getInstance();
        DatabaseManager instance2 = DatabaseManager.getInstance();

        assertSame(instance1, instance2, "Should return same singleton instance");
    }

    // ==================== Connection Tests ====================

    @Test
    @Order(2)
    @DisplayName("getConnection() returns valid connection")
    void testGetConnection() {
        Connection conn = dbManager.getConnection();

        assertNotNull(conn, "Connection should not be null");
    }

    @Test
    @Order(3)
    @DisplayName("getConnection() returns open connection")
    void testConnectionIsOpen() throws SQLException {
        Connection conn = dbManager.getConnection();

        assertFalse(conn.isClosed(), "Connection should be open");
    }

    // ==================== Connection Health Tests ====================

    @Test
    @Order(4)
    @DisplayName("isConnectionHealthy() returns true for valid connection")
    void testConnectionHealthy() {
        assertTrue(dbManager.isConnectionHealthy(), "Connection should be healthy");
    }

    @Test
    @Order(5)
    @DisplayName("isConnectionHealthy() detects closed connection")
    void testConnectionHealthyAfterClose() throws SQLException {
        connection.close();

        assertFalse(dbManager.isConnectionHealthy(), "Should detect closed connection");
    }

    // ==================== Connection Testing ====================

    @Test
    @Order(6)
    @DisplayName("testConnection() executes SELECT 1 successfully")
    void testConnectionTest() {
        boolean result = dbManager.testConnection();

        assertTrue(result, "Test query should succeed");
    }

    @Test
    @Order(7)
    @DisplayName("testConnection() returns false for closed connection")
    void testConnectionTestAfterClose() throws SQLException {
        connection.close();

        boolean result = dbManager.testConnection();

        assertFalse(result, "Test should fail on closed connection");
    }

    // ==================== Reconnection Tests ====================

    @Test
    @Order(8)
    @DisplayName("reconnect() establishes new connection after close")
    void testReconnect() throws SQLException {
        connection.close();
        assertFalse(dbManager.isConnectionHealthy(), "Connection should be unhealthy");

        boolean reconnected = dbManager.reconnect();

        assertTrue(reconnected, "Reconnection should succeed");
        assertTrue(dbManager.isConnectionHealthy(), "Connection should be healthy after reconnect");
    }

    @Test
    @Order(9)
    @DisplayName("reconnect() returns new working connection")
    void testReconnectReturnsWorkingConnection() throws SQLException {
        connection.close();

        dbManager.reconnect();
        Connection newConnection = dbManager.getConnection();

        assertNotNull(newConnection, "New connection should not be null");
        assertFalse(newConnection.isClosed(), "New connection should be open");
    }

    // ==================== Connection Info Tests ====================

    @Test
    @Order(10)
    @DisplayName("getConnectionInfo() returns diagnostic information")
    void testGetConnectionInfo() {
        String info = dbManager.getConnectionInfo();

        assertNotNull(info, "Connection info should not be null");
        assertTrue(info.contains("Database:"), "Should contain database info");
        assertTrue(info.contains("Valid:"), "Should contain validation status");
    }

    @Test
    @Order(11)
    @DisplayName("getConnectionInfo() handles closed connection")
    void testGetConnectionInfoAfterClose() throws SQLException {
        connection.close();

        String info = dbManager.getConnectionInfo();

        assertNotNull(info, "Should return error message");
    }

    // ==================== Configuration Tests ====================

    @Test
    @Order(12)
    @DisplayName("getDatabaseUrl() returns configured URL")
    void testGetDatabaseUrl() {
        String url = dbManager.getDatabaseUrl();

        assertNotNull(url, "URL should not be null");
        assertTrue(url.startsWith("jdbc:"), "Should be valid JDBC URL");
        assertTrue(url.contains("h2:mem"), "Should be H2 in-memory for tests");
    }

    @Test
    @Order(13)
    @DisplayName("getDatabaseUsername() returns configured username")
    void testGetDatabaseUsername() {
        String username = dbManager.getDatabaseUsername();

        assertNotNull(username, "Username should not be null");
        assertEquals("sa", username, "Should be 'sa' for H2");
    }

    // ==================== Connection Lifecycle Tests ====================

    @Test
    @Order(14)
    @DisplayName("closeConnection() closes the connection")
    void testCloseConnection() throws SQLException {
        dbManager.closeConnection();

        Connection conn = dbManager.getConnection();
        assertTrue(conn == null || conn.isClosed(), "Connection should be closed or null");
    }

    @Test
    @Order(15)
    @DisplayName("closeConnection() can be called multiple times safely")
    void testCloseConnectionIdempotent() {
        assertDoesNotThrow(() -> {
            dbManager.closeConnection();
            dbManager.closeConnection();
            dbManager.closeConnection();
        }, "Multiple closes should not throw exception");
    }

    // ==================== Integration Tests ====================

    @Test
    @Order(16)
    @DisplayName("getConnection() auto-reconnects if connection is unhealthy")
    void testAutoReconnect() throws SQLException {
        connection.close();

        // getConnection() should detect unhealthy connection and reconnect
        Connection newConn = dbManager.getConnection();

        assertNotNull(newConn, "Should get new connection");
        assertFalse(newConn.isClosed(), "New connection should be open");
    }

    @Test
    @Order(17)
    @DisplayName("Connection can execute simple query")
    void testConnectionCanExecuteQuery() throws SQLException {
        var stmt = connection.createStatement();
        var rs = stmt.executeQuery("SELECT 1");

        assertTrue(rs.next(), "Query should return result");
        assertEquals(1, rs.getInt(1), "Should return 1");

        rs.close();
        stmt.close();
    }

    @Test
    @Order(18)
    @DisplayName("Connection supports transactions")
    void testConnectionSupportsTransactions() throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();

        connection.setAutoCommit(false);
        assertFalse(connection.getAutoCommit(), "Should disable auto-commit");

        connection.setAutoCommit(true);
        assertTrue(connection.getAutoCommit(), "Should enable auto-commit");

        // Restore original state
        connection.setAutoCommit(originalAutoCommit);
    }
}