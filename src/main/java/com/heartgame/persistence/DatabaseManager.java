package com.heartgame.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the database connection as a Singleton
 * Reads configuration from a properties file
 */
public final class DatabaseManager {

    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Properties props = new Properties();
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
                if (input == null) {
                    logger.severe("Database properties file not found.");
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
        } catch  (SQLException | ClassNotFoundException | IOException e) {
            logger.log(Level.SEVERE, "Failed to connect to the database.", e);
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
        return this.connection;
    }
}
