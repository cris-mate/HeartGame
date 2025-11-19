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
 * Includes connection health checks and validation
 */
public final class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;
    private final Properties dbProperties;
    private Connection connection;

    // Connection validation settings
    private static final int VALIDATION_TIMEOUT_SECONDS = 2;

    private DatabaseManager() {
        this.dbProperties = loadDatabaseProperties();
        this.connection = createConnection();

        initializeDatabase();
    }

    /**
     * Loads database properties from the properties file
     * @return Properties object with database configuration
     */
    private Properties loadDatabaseProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                logger.error("Database properties file not found.");
                return props;
            }
            props.load(input);
            logger.debug("Database properties loaded successfully");
        } catch (IOException e) {
            logger.error("Failed to load database properties", e);
        }
        return props;
    }

    /**
     * Creates a new database connection using the loaded properties
     * @return Connection object, or null if creation fails
     */
    private Connection createConnection() {
        try {
            String driverClass =  dbProperties.getProperty("db.driver");
            if (driverClass == null || driverClass.trim().isEmpty()) {
                logger.error("Database driver class (db.driver) not configured in properties.");
                return null;
            }
            Class.forName(driverClass);

            Connection conn = DriverManager.getConnection(
                    dbProperties.getProperty("db.url"),
                    dbProperties.getProperty("db.username"),
                    dbProperties.getProperty("db.password")
            );
            logger.info("Database connection established successfully");
            return conn;
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Failed to connect to the database. Check properties and server status.", e);
            return null;
        }
    }

    /**
     * Initialises the database: sets up the schema and imports backup data if available
     * This is called once during startup to ensure tables exist and data is loaded
     */
    private void initializeDatabase() {
        String dbUrl = dbProperties.getProperty("db.url", "");

        // Checks if running in the H2 in-memory test environment
        // The test database URL contains "jdbc:h2:mem"
        if (dbUrl.contains("jdbc:h2:mem")) {
            logger.warn("Skipping production initialization in H2 test environment.");
            return;
        }
        logger.info("Starting production database initialization...");

        // Ensures the database structure is created using schema.sql
        executeSQLScript("schema.sql");

        // Loads backup data ONLY if this is first-time setup (tables are empty)
        // This prevents existing user data from being wiped on application restart
        if (isDatabaseEmpty()) {
            logger.info("Database is empty. Loading initial seed data from backup...");
            executeSQLScript("heartgame_backup.sql");
        } else {
            logger.info("Database contains existing data. Skipping backup restoration to preserve user data.");
        }
        logger.info("Database initialization complete.");
    }

    /**
     * Checks if the database has any user data
     * Used to determine if seed data should be loaded from backup
     * @return true if the users table is empty or doesn't exist, false otherwise
     */
    private boolean isDatabaseEmpty() {
        if (this.connection == null) {
            logger.debug("Connection is null, considering database as empty");
            return true;
        }

        try (java.sql.Statement stmt = connection.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) {
                int count = rs.getInt(1);
                logger.debug("Found {} users in database", count);
                return count == 0;
            }
            return true;
        } catch (SQLException e) {
            // If table doesn't exist yet, schema.sql will create it
            logger.debug("Could not check user count (table may not exist yet): {}", e.getMessage());
            return true;
        }
    }

    /**
     * Executes a SQL script file from the classpath
     * This is a simple implementation that splits statements by semicolon.
     * @param scriptPath The resource path
     * @return true if successful, false otherwise
     */
    private boolean executeSQLScript(String scriptPath) {
        if (this.connection == null) {
            logger.error("Cannot execute script: database connection is null.");
            return false;
        }

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(scriptPath)) {
            if (input == null) {
                // The schema.sql is critical, but the backup file is optional
                if (!scriptPath.equals("schema.sql")) {
                    logger.debug("Optional SQL script not found: {}", scriptPath);
                    return true;
                }
                logger.error("Critical SQL script not found: {}", scriptPath);
                return false;
            }

            logger.info("Executing SQL script: {}", scriptPath);

            // Read the entire file content
            String script = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

            // Split the script into individual SQL statements by semicolon
            String[] statements = script.split(";");

            try (java.sql.Statement stmt = connection.createStatement()) {
                for (String statement : statements) {
                    String trimmedStatement = statement.trim();
                    // Skip empty lines, whitespace, and single-line comments
                    if (trimmedStatement.isEmpty() || trimmedStatement.startsWith("--")) continue;

                    stmt.execute(trimmedStatement);
                }
                logger.info("SQL script executed successfully: {}", scriptPath);
                return true;
            } catch (java.sql.SQLException e) {
                // Log the failure but don't re-throw, allowing the application to attempt to start
                logger.error("Failed to execute SQL script: {}", scriptPath, e);
                return false;
            }

        } catch (IOException e) {
            logger.error("Failed to read SQL script file: {}", scriptPath, e);
            return false;
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
     * Gets the active database connection with automatic validation and reconnection
     * Validates the connection before returning it
     * @return The active database connection, or null if unavailable
     */
    public Connection getConnection() {
        // Check if connection needs validation/reconnection
        if (!isConnectionHealthy()) {
            logger.info("Connection unhealthy, attempting to reconnect...");
            reconnect();
        }

        return this.connection;
    }

    /**
     * Checks if the database connection is healthy
     * Uses a quick validation check without blocking
     * @return true if connection is healthy, false otherwise
     */
    public boolean isConnectionHealthy() {
        if (connection == null) {
            logger.debug("Connection is null");
            return false;
        }

        try {
            // Quick validation check with timeout
            if (connection.isClosed()) {
                logger.warn("Connection is closed");
                return false;
            }

            if (!connection.isValid(VALIDATION_TIMEOUT_SECONDS)) {
                logger.warn("Connection validation failed");
                return false;
            }

            logger.trace("Connection is healthy");
            return true;

        } catch (SQLException e) {
            logger.error("Error validating connection health", e);
            return false;
        }
    }


    /**
     * Attempts to reconnect to the database
     * Closes the old connection (if any) and creates a new one
     */
    public boolean reconnect() {
        logger.info("Attempting to reconnect to database...");

        // Close old connection if it exists
        closeConnection();

        // Create new connection
        this.connection = createConnection();

        if (connection != null) {
            logger.info("Database reconnection successful");
            return true;
        } else {
            logger.error("Database reconnection failed");
            return false;
        }
    }

    /**
     * Tests the database connection by executing a simple query
     * This is a more thorough check than isConnectionHealthy()
     * @return true if test query succeeds, false otherwise
     */
    public boolean testConnection() {
        if (connection == null) {
            logger.warn("Cannot test connection: connection is null");
            return false;
        }

        try {
            // Execute a simple test query
            var stmt = connection.createStatement();
            var rs = stmt.executeQuery("SELECT 1");
            boolean hasResult = rs.next();
            rs.close();
            stmt.close();

            if (hasResult) {
                logger.info("Connection test passed");
                return true;
            } else {
                logger.warn("Connection test failed: no result");
                return false;
            }

        } catch (SQLException e) {
            logger.error("Connection test failed with exception", e);
            return false;
        }
    }

    /**
     * Gets connection information for diagnostics
     * @return String with connection details, or error message if unavailable
     */
    public String getConnectionInfo() {
        if (connection == null) {
            return "No connection available";
        }

        try {
            return String.format("Database: %s | Valid: %s | Closed: %s | ReadOnly: %s",
                    connection.getCatalog(),
                    connection.isValid(1),
                    connection.isClosed(),
                    connection.isReadOnly());
        } catch (SQLException e) {
            logger.error("Error getting connection info", e);
            return "Error retrieving connection info: " + e.getMessage();
        }
    }

    /**
     * Closes the database connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Gets the database URL from properties
     * Useful for diagnostics and logging
     * @return Database URL string
     */
    public String getDatabaseUrl() {
        return dbProperties.getProperty("db.url", "URL not configured");
    }

    /**
     * Gets the database username from properties
     * @return Database username string
     */
    public String getDatabaseUsername() {
        return dbProperties.getProperty("db.username", "Username not configured");
    }
}
