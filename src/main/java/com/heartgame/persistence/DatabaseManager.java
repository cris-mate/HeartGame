package com.heartgame.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the database connection as a Singleton
 * Reads configuration from a properties file
 * Uses SLF4J for consistent logging with logback
 */
public final class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Properties props = new Properties();
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
                if (input == null) {
                    logger.error("Database properties file not found.");
                    return;
                }
                props.load(input);
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.username"),
                    props.getProperty("db.password")
            );
            logger.info("Database connection established successfully");
        } catch  (SQLException | ClassNotFoundException | IOException e) {
            logger.error("Failed to connect to the database.", e);
        }
    }

    /**
     * @return The single instance of the DatabaseManager
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * @return The active database connection.
     */
    public Connection getConnection() {
        if (connection != null) {
            try {
                if (!connection.isValid(2)) {
                    logger.warn("Connection validation failed, attempting to reconnect...");
                    instance = null;
                    return getInstance().getConnection();
                }
            } catch (SQLException e) {
                logger.error("Error validating connection", e);
            }
        }
        return this.connection;
    }

    /**
     * Closes the database connection
     * Should be called on application shutdown
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }
}
