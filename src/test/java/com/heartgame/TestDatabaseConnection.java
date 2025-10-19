package com.heartgame;

import com.heartgame.persistence.DatabaseManager;
import java.sql.Connection;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");

        Connection conn = DatabaseManager.getInstance().getConnection();

        if (conn != null) {
            System.out.println("Database connected successfully!");
            try {
                System.out.println("Database: " + conn.getCatalog());
                System.out.println("Connection valid: " + conn.isValid(2));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to connect to database!");
            System.out.println("Please double-check:");
            System.out.println("1. MySQL is running");
            System.out.println("2. database.properties has correct credentials");
            System.out.println("3. Database 'heartgame' exists");
        }
    }
}
