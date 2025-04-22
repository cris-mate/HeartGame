package com.perisic.banana.engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class loads database configuration from a properties file,
 * loads the appropriate JDBC driver, and provides a connection to the
 * database for use in the Banana Game.
 */
public class DBConfig {

    private static final Logger LOGGER = Logger.getLogger(DBConfig.class.getName());

    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String dbDriver;

    private final Connection connection;
    private static boolean shutdownHook = false;

    /**
     * Constructs a new DBConfig instance.
     * Initializes the database configuration, loads the JDBC driver, establishes
     * a connection, and registers a shutdown hook to close the connection cleanly.
     *
     * @throws RuntimeException if configuration loading, driver loading, or connection fails.
     */
    public DBConfig() {
        try {
            loadConfig();
            loadDriver();
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            LOGGER.info("Connected to database.");

            if (!shutdownHook) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        if (connection != null && !connection.isClosed()) {
                            connection.close();
                            LOGGER.info("Disconnected from database.");
                        }
                    } catch (SQLException e) {
                        LOGGER.severe("Error disconnecting from database: " + e.getMessage());
                    }
                }));
                shutdownHook = true;
            }
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Database connection failed", e);
            throw new RuntimeException("Database connection failed", e);
        }
    }

    /**
     * Loads database configuration properties from a file named config.properties.
     *
     * @throws RuntimeException if the configuration file cannot be read.
     */
    private void loadConfig() {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            Properties props = new Properties();
            props.load(fis);
            dbUrl = props.getProperty("db.url");
            dbUsername = props.getProperty("db.username");
            dbPassword = props.getProperty("db.password");
            dbDriver = props.getProperty("db.driver");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load DB configuration", e);
            throw new RuntimeException("Failed to load DB configuration", e);
        }
    }

    /**
     * Loads the JDBC driver specified in the configuration file.
     *
     * @throws ClassNotFoundException if the driver class cannot be found.
     */
    private void loadDriver() throws ClassNotFoundException {
        Class.forName(dbDriver);
    }

    /**
     * Returns the active database connection.
     *
     * @return the established {@link java.sql.Connection}
     */
    public Connection getConnection() {
        return connection;
    }
}