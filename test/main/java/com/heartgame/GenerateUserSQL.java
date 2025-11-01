package com.heartgame;

import com.heartgame.service.AuthenticationService;

/**
 * Generates SQL INSERT statements with BCrypt hashes
 * Uses AuthenticationService for password hashing operations
 */
public class GenerateUserSQL {
    public static void main(String[] args) {
        AuthenticationService authService = new AuthenticationService();

        // Define users to create (username, password, email)
        String[][] users = {
                {"admin", "admin123", "admin@heartgame.com"},
                {"demo", "demo123", "demo@heartgame.com"},
                {"testUser", "test123", "test@heartgame.com"}
        };

        System.out.println("-- Generated User INSERT Statements");
        System.out.println("-- Password is shown in comment for reference\n");

        for (String[] user : users) {
            String username = user[0];
            String password = user[1];
            String email = user[2];
            String hash = authService.hashPassword(password);

            System.out.println("-- Password: " + password);
            System.out.println("INSERT INTO users (username, password_hash, email, oauth_provider) VALUES");
            System.out.println("('" + username + "', '" + hash + "', '" + email + "', 'password')");
            System.out.println("ON DUPLICATE KEY UPDATE username=username;\n");
        }
    }
}
