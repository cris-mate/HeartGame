package com.heartgame;

import com.heartgame.persistence.UserDAO;
import java.util.Scanner;

/**
 * Interactive tool for generating BCrypt password hashes
 */
public class PasswordHasherTool {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== BCrypt Password Hasher ===\n");

        while (true) {
            System.out.print("Enter password (or 'quit' to exit): ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.isEmpty()) {
                System.out.println("⚠️  Password cannot be empty!\n");
                continue;
            }

            String hash = UserDAO.hashPassword(input);

            System.out.println("\n✅ Hash generated successfully!");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("Plaintext: " + input);
            System.out.println("BCrypt:    " + hash);
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

            System.out.println("SQL INSERT example:");
            System.out.println("INSERT INTO users (username, password_hash, oauth_provider) VALUES");
            System.out.println("('myuser', '" + hash + "', 'password');\n");
        }

        scanner.close();
    }
}