package com.heartgame.model;

/**
 * Represents a player in the game
 * Supports both traditional password-based and OAuth-based authentication
 */
public class User {

    private int id;
    private final String username;
    private String email;
    private String displayName;
    private String oauthProvider;  // "password", "google", etc.
    private String oauthId;        // OAuth provider's user ID

    /**
     * Constructs a new User with username only (legacy constructor)
     * @param username The user's name
     */
    public User(String username) {
        this.username = username;
        this.oauthProvider = "password"; // Default to password-based auth
    }

    /**
     * Constructs a new User with full OAuth details
     * @param username The user's name
     * @param email The user's email
     * @param displayName The user's display name
     * @param oauthProvider The OAuth provider (e.g., "google")
     * @param oauthId The OAuth provider's user ID
     */
    public User(String username, String email, String displayName, String oauthProvider, String oauthId) {
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
    }

    /**
     * Constructs a User with database ID (for DAO retrieval)
     * @param id Database primary key
     * @param username The user's name
     * @param email The user's email
     * @param displayName The user's display name
     * @param oauthProvider The OAuth provider
     * @param oauthId The OAuth provider's user ID
     */
    public User(int id, String username, String email, String displayName, String oauthProvider, String oauthId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName != null ? displayName : username; }
    public String getOauthProvider() { return oauthProvider; }
    public String getOauthId() { return oauthId; }

    // Setters (for DAO updates)
    public void setId(int id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    /**
     * @return True if this user authenticates via OAuth, false for password-based
     */
    public boolean isOAuthUser() {
        return oauthProvider != null && !oauthProvider.equals("password");
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", oauthProvider='" + oauthProvider + '\'' +
                '}';
    }
}
