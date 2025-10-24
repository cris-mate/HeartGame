package com.heartgame;

import com.heartgame.persistence.UserDAO;

/**
 * Generates SQL INSERT statements with BCrypt hashes
 */
public class GenerateUserSQL {
    public static void main(String[] args) {
        // Define users to create
        String[][] users = {
                {"admin", "admin123", "admin@heartgame.com", "Administrator"},
                {"demo", "demo123", "demo@heartgame.com", "Demo User"},
                {"testUser", "test123", "test@heartgame.com", "Test User"}
        };

        System.out.println("-- Generated User INSERT Statements");
        System.out.println("-- Password is shown in comment for reference\n");

        for (String[] user : users) {
            String username = user[0];
            String password = user[1];
            String email = user[2];
            String displayName = user[3];
            String hash = UserDAO.hashPassword(password);

            System.out.println("-- Password: " + password);
            System.out.println("INSERT INTO users (username, password_hash, email, display_name, oauth_provider) VALUES");
            System.out.println("('" + username + "', '" + hash + "', '" + email + "', '" + displayName + "', 'password')");
            System.out.println("ON DUPLICATE KEY UPDATE username=username;\n");
        }
    }
}
