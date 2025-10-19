package com.heartgame.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    public static void main(String[] args) {
        String plaintextPassword = "password123";
        String hashedPassword = BCrypt.hashpw(plaintextPassword, BCrypt.gensalt());

        System.out.println("Plaintext: " + plaintextPassword);
        System.out.println("Hashed: " + hashedPassword);
    }
}
