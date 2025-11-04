package com.heartgame.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base DAO class providing common database operations and patterns
 * Implements DRY principle by centralizing shared functionality
 * Provides transaction management and error handling
 */
public abstract class BaseDAO {

    private static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);
    protected static final String NO_CONNECTION_ERROR = "Cannot query database. No connection available.";
    protected final Connection connection;

    /**
     * Constructs a BaseDAO with database connection
     */
    protected BaseDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Checks if database connection is available and healthy
     * @return true if connection is available, false otherwise
     */
    protected boolean hasConnection() {
        if (connection == null) {
            logger.error(NO_CONNECTION_ERROR);
            return false;
        }

        // Additional health check
        if (!DatabaseManager.getInstance().isConnectionHealthy()) {
            logger.warn("Connection exists but is unhealthy");
            return false;
        }

        return true;
    }

    /**
     * Begins a database transaction
     * Disables auto-commit mode
     * @return true if transaction started successfully, false otherwise
     */
    protected boolean beginTransaction() {
        if (!hasConnection()) {
            return false;
        }

        try {
            if (!connection.getAutoCommit()) {
                logger.warn("Transaction already in progress");
                return true; // Already in transaction
            }

            connection.setAutoCommit(false);
            logger.debug("Transaction started");
            return true;

        } catch (SQLException e) {
            logger.error("Failed to start transaction", e);
            return false;
        }
    }

    /**
     * Commits the current transaction
     * Re-enables auto-commit mode
     * @return true if commit succeeded, false otherwise
     */
    protected boolean commitTransaction() {
        if (!hasConnection()) {
            return false;
        }

        try {
            connection.commit();
            connection.setAutoCommit(true);
            logger.debug("Transaction committed");
            return true;

        } catch (SQLException e) {
            logger.error("Failed to commit transaction", e);
            rollbackTransaction(); // Attempt rollback on commit failure
            return false;
        }
    }

    /**
     * Rolls back the current transaction
     * Re-enables auto-commit mode
     * Safe to call even if no transaction is active
     */
    protected void rollbackTransaction() {
        if (connection == null) {
            return;
        }

        try {
            if (!connection.getAutoCommit()) {
                connection.rollback();
                connection.setAutoCommit(true);
                logger.debug("Transaction rolled back");
            }
        } catch (SQLException e) {
            logger.error("Failed to rollback transaction", e);
        }
    }

    /**
     * Executes an operation within a transaction
     * Automatically handles commit/rollback
     * @param operation The database operation to execute
     * @return true if operation succeeded and committed, false otherwise
     */
    protected boolean executeInTransaction(DatabaseOperation operation) {
        if (!beginTransaction()) {
            return false;
        }

        try {
            boolean success = operation.execute();

            if (success) {
                return commitTransaction();
            } else {
                rollbackTransaction();
                return false;
            }

        } catch (Exception e) {
            logger.error("Error during transaction execution", e);
            rollbackTransaction();
            return false;
        }
    }

    /**
     * Functional interface for database operations
     */
    @FunctionalInterface
    protected interface DatabaseOperation {
        boolean execute() throws SQLException;
    }

    /**
     * Executes an operation with retry logic and exponential backoff
     * Makes one initial attempt plus additional retry attempts on failure
     * @param operation The operation to execute
     * @param maxRetries Maximum number of RETRY attempts (total attempts = 1 + maxRetries)
     *                   Example: maxRetries=2 means 3 total attempts (1 initial + 2 retries)
     * @return true if operation succeeded, false if all attempts failed
     */
    protected boolean executeWithRetry(DatabaseOperation operation, int maxRetries) {
        int attempts = 0;

        while (attempts <= maxRetries) {
            try {
                if (operation.execute()) {
                    if (attempts > 0) {
                        logger.info("Operation succeeded after {} retries", attempts);
                    }
                    return true;
                }

                attempts++;
                if (attempts <= maxRetries) {
                    logger.debug("Operation failed, retry {}/{}", attempts, maxRetries);
                    Thread.sleep(100L * attempts); // Simple backoff
                }

            } catch (SQLException e) {
                attempts++;
                logger.warn("Database operation failed (attempt {}/{}): {}",
                        attempts, maxRetries + 1, e.getMessage());

                if (attempts <= maxRetries) {
                    // Try to reconnect before retry
                    DatabaseManager.getInstance().reconnect();
                    try {
                        Thread.sleep(100L * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            } catch (Exception e) {
                logger.error("Unexpected error during operation", e);
                return false;
            }
        }

        logger.error("Operation failed after {} attempts", maxRetries + 1);
        return false;
    }
}
