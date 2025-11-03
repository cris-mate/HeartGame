package com.heartgame.controller;

import com.heartgame.event.GameEventManager;
import com.heartgame.model.UserSession;
import com.heartgame.persistence.DatabaseManager;
import com.heartgame.persistence.DatabaseTestHelper;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Helper utilities for controller testing
 * Provides common setup and teardown operations following KISS principle
 */
public class ControllerTestHelper {

    private static DatabaseManager dbManager;
    private static Connection connection;

    /**
     * Initializes test infrastructure before all tests
     * Sets up H2 in-memory database
     */
    public static void initializeTestEnvironment() throws SQLException {
        dbManager = DatabaseManager.getInstance();
        connection = dbManager.getConnection();
        DatabaseTestHelper.initializeSchema(connection);
    }

    /**
     * Cleans up state before each test
     * - Clears database data
     * - Logs out current user session
     * - Clears all event listeners
     */
    public static void cleanupBeforeEachTest() throws SQLException {
        // Clear database
        if (connection != null) {
            DatabaseTestHelper.clearAllData(connection);
        }

        // Clear user session
        UserSession.getInstance().logout();

        // Clear all event listeners (prevents cross-test contamination)
        GameEventManager.getInstance().clearAllListeners();
    }

    /**
     * Gets the test database connection
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Gets the database manager instance
     */
    public static DatabaseManager getDbManager() {
        return dbManager;
    }
}
