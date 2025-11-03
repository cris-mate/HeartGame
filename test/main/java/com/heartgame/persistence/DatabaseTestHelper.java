package com.heartgame.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helper class for database testing
 * Sets up H2 in-memory database schema and provides cleanup utilities
 * Follows KISS principle - simple schema initialization and data cleanup
 */
public class DatabaseTestHelper {

    /**
     * Initializes the H2 test database schema
     * Creates all required tables matching the production schema
     * H2 MODE=MySQL ensures MySQL compatibility
     */
    public static void initializeSchema(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password_hash VARCHAR(60) NULL,
                    email VARCHAR(100),
                    oauth_provider VARCHAR(20),
                    oauth_id VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_login TIMESTAMP NULL
                )
            """);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_username ON users(username)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_oauth ON users(oauth_provider, oauth_id)");

            // Game sessions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS game_sessions (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    end_time TIMESTAMP NULL,
                    final_score INT DEFAULT 0,
                    questions_answered INT DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);

            // Logging tables (required by schema but not directly tested)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS logging_event (
                    timestmp BIGINT NOT NULL,
                    formatted_message TEXT NOT NULL,
                    logger_name VARCHAR(254) NOT NULL,
                    level_string VARCHAR(254) NOT NULL,
                    thread_name VARCHAR(254),
                    reference_flag SMALLINT,
                    arg0 VARCHAR(254),
                    arg1 VARCHAR(254),
                    arg2 VARCHAR(254),
                    arg3 VARCHAR(254),
                    caller_filename VARCHAR(254) NOT NULL,
                    caller_class VARCHAR(254) NOT NULL,
                    caller_method VARCHAR(254) NOT NULL,
                    caller_line CHAR(4) NOT NULL,
                    event_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY
                )
            """);
        }
    }

    /**
     * Clears all data from test tables
     * Used in @BeforeEach to ensure clean state for each test
     * Maintains referential integrity by deleting in correct order
     */
    public static void clearAllData(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("TRUNCATE TABLE game_sessions");
            stmt.execute("TRUNCATE TABLE users");
            stmt.execute("TRUNCATE TABLE logging_event");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    /**
     * Counts rows in a table
     * Useful for test assertions
     */
    public static int countRows(Connection connection, String tableName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
}
