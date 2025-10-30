package com.heartgame;

import com.heartgame.persistence.DatabaseManager;
import java.sql.Connection;

/**
 * Enhanced test class for database connection validation
 * Demonstrates the new health check features
 */
public class TestDatabaseConnection {
    public static void main(String[] args) {
        System.out.println("=== HeartGame Database Connection Test ===\n");

        DatabaseManager dbManager = DatabaseManager.getInstance();

        // Test 1: Basic connection check
        System.out.println("1. Testing basic connection...");
        Connection conn = dbManager.getConnection();

        if (conn != null) {
            System.out.println("   Connection established");
        } else {
            System.out.println("   Failed to establish connection");
            printTroubleshootingSteps();
            return;
        }

        // Test 2: Connection health check
        System.out.println("\n2. Testing connection health...");
        boolean isHealthy = dbManager.isConnectionHealthy();
        System.out.println("   Health Status: " + (isHealthy ? "HEALTHY" : "UNHEALTHY"));

        // Test 3: Connection info
        System.out.println("\n3. Connection Details:");
        String info = dbManager.getConnectionInfo();
        System.out.println("   " + info);

        // Test 4: Test query execution
        System.out.println("\n4. Testing query execution...");
        boolean testPassed = dbManager.testConnection();
        System.out.println("   Test Query: " + (testPassed ? "PASSED" : "FAILED"));

        // Test 5: Database metadata
        System.out.println("\n5. Database Configuration:");
        System.out.println("   URL: " + dbManager.getDatabaseUrl());
        System.out.println("   Username: " + dbManager.getDatabaseUsername());

        // Test 6: Advanced connection details
        if (conn != null) {
            System.out.println("\n6. Advanced Connection Details:");
            try {
                System.out.println("   Database Product: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("   Database Version: " + conn.getMetaData().getDatabaseProductVersion());
                System.out.println("   Driver Name: " + conn.getMetaData().getDriverName());
                System.out.println("   Driver Version: " + conn.getMetaData().getDriverVersion());
                System.out.println("   Connection Catalog: " + conn.getCatalog());
                System.out.println("   Connection Valid: " + conn.isValid(2));
            } catch (Exception e) {
                System.out.println("   Error retrieving metadata: " + e.getMessage());
            }
        }

        // Test 7: Reconnection capability
        System.out.println("\n7. Testing reconnection capability...");
        boolean reconnected = dbManager.reconnect();
        System.out.println("   Reconnection: " + (reconnected ? "SUCCESS" : "FAILED"));

        // Final summary
        System.out.println("\n=== Test Summary ===");
        if (isHealthy && testPassed && reconnected) {
            System.out.println("All tests passed - Database is ready for use!");
        } else {
            System.out.println("Some tests failed - Review the output above");
            printTroubleshootingSteps();
        }
    }

    private static void printTroubleshootingSteps() {
        System.out.println("\n=== Troubleshooting Steps ===");
        System.out.println("1. Ensure MySQL server is running");
        System.out.println("2. Verify database.properties has correct credentials");
        System.out.println("3. Confirm database 'heartgame' exists");
        System.out.println("4. Check MySQL user has proper permissions");
        System.out.println("5. Verify MySQL is listening on localhost:3306");
        System.out.println("6. Check firewall settings");
    }
}