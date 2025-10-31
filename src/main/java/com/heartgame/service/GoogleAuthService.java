package com.heartgame.service;

import com.heartgame.model.User;
import com.heartgame.util.ConfigurationManager;
import com.heartgame.util.HTTPClient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Simplified Google OAuth 2.0 authentication service
 * Uses HTTPClient utility for all network requests (DRY principle)
 * Implements OAuth 2.0 Authorization Code Flow for desktop applications
 */
public class GoogleAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);
    private static final int CALLBACK_TIMEOUT = 120000;

    // OAuth endpoints - configurable via application.properties
    private final String authUrl;
    private final String tokenUrl;
    private final String userinfoUrl;

    // OAuth scopes
    private static final String SCOPES = "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";

    private final String clientId;
    private final String clientSecret;
    private final int callbackPort;
    private final String redirectUri;

    /**
     * Constructs a new GoogleAuthService
     * Loads OAuth credentials from client_secret.json and URLs from configuration
     */
    public GoogleAuthService() {
        ConfigurationManager config = ConfigurationManager.getInstance();
        this.callbackPort = config.getOAuthCallbackPort();
        this.redirectUri = "http://localhost:" + callbackPort;

        // Load OAuth URLs from configuration with defaults
        this.authUrl = config.getProperty("google.oauth.auth.url",
                "https://accounts.google.com/o/oauth2/v2/auth");
        this.tokenUrl = config.getProperty("google.oauth.token.url",
                "https://oauth2.googleapis.com/token");
        this.userinfoUrl = config.getProperty("google.oauth.userinfo.url",
                "https://www.googleapis.com/oauth2/v2/userinfo");

        // Load client credentials from file
        Properties credentials = loadClientCredentials();
        this.clientId = credentials.getProperty("client_id");
        this.clientSecret = credentials.getProperty("client_secret");

        if (clientId == null || clientSecret == null) {
            logger.error("Failed to load OAuth credentials from client_secret.json");
        }
    }

    /**
     * Loads OAuth client credentials from client_secret.json
     * @return Properties containing client_id and client_secret
     */
    private Properties loadClientCredentials() {
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/client_secret.json")) {
            if (input == null) {
                logger.error("client_secret.json not found in resources");
                logger.error("Please obtain OAuth credentials from Google Cloud Console:");
                logger.error("1. Go to https://console.cloud.google.com/");
                logger.error("2. Create a new project or select existing one");
                logger.error("3. Enable Google+ API");
                logger.error("4. Create OAuth 2.0 credentials (Desktop application type)");
                logger.error("5. Download JSON and place in src/main/resources/client_secret.json");
                return props;
            }

            // Parse JSON file using Gson
            InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            // Support both "installed" and "web" OAuth client types
            JsonObject credentials = json.has("installed") ?
                    json.getAsJsonObject("installed") :
                    json.getAsJsonObject("web");

            if (credentials != null) {
                props.setProperty("client_id", credentials.get("client_id").getAsString());
                props.setProperty("client_secret", credentials.get("client_secret").getAsString());
                logger.info("OAuth credentials loaded successfully");
            }

        } catch (Exception e) {
            logger.error("Failed to parse client_secret.json: {}", e.getMessage(), e);
        }
        return props;
    }

    /**
     * Authenticates a user via Google OAuth 2.0
     * Opens browser for user consent, then fetches user profile
     * @return User object with Google profile data, or null if authentication fails
     */
    public User authenticateUser() {
        try {
            // Step 1: Build authorization URL
            String authUrl = buildAuthorizationUrl();
            logger.info("Opening browser for Google authentication...");

            // Step 2: Open browser
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                logger.warn("Desktop not supported. Please visit: {}", authUrl);
            }

            // Step 3: Start local server to receive callback
            String authorizationCode = waitForCallback();
            if (authorizationCode == null) {
                logger.error("Failed to receive authorization code");
                return null;
            }

            // Step 4: Exchange code for access token
            String accessToken = exchangeCodeForToken(authorizationCode);
            if (accessToken == null) {
                logger.error("Failed to exchange code for access token");
                return null;
            }

            // Step 5: Fetch user info
            return fetchUserInfo(accessToken);

        } catch (Exception e) {
            logger.error("OAuth authentication failed: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Builds the Google OAuth authorization URL
     * @return Authorization URL with all required parameters
     */
    private String buildAuthorizationUrl() {
        return authUrl +
                "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode(SCOPES, StandardCharsets.UTF_8) +
                "&access_type=offline";
    }

    /**
     * Starts a local HTTP server and waits for OAuth callback
     * Has configurable timeout
     * @return Authorization code from callback, or null if timeout/error
     */
    private String waitForCallback() {
        try (ServerSocket serverSocket = new ServerSocket(callbackPort)) {
            serverSocket.setSoTimeout(CALLBACK_TIMEOUT);
            logger.info("Waiting for OAuth callback on port {}...", callbackPort);

            try (Socket socket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream())) {

                // Read the HTTP request
                String requestLine = in.readLine();
                logger.debug("Received callback: {}", requestLine);

                // Parse authorization code from URL
                String authCode = null;
                if (requestLine != null && requestLine.startsWith("GET")) {
                    String[] parts = requestLine.split(" ");
                    if (parts.length > 1) {
                        String path = parts[1];
                        authCode = extractParameter(path, "code");
                    }
                }

                // Send response to browser
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println();
                out.println("<html><body>");
                out.println("<h1>Authentication Complete!</h1>");
                out.println("<p>You can close this window and return to <strong>HeartGame</strong>.</p>");
                out.println("</body></html>");
                out.flush();

                return authCode;
            }

        } catch (Exception e) {
            logger.error("Error waiting for OAuth callback: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extracts a parameter value from a URL query string
     * @param url The URL with query parameters
     * @param paramName The parameter name to extract
     * @return The parameter value, or null if not found
     */
    private String extractParameter(String url, String paramName) {
        try {
            int queryStart = url.indexOf('?');
            if (queryStart == -1) return null;

            String query = url.substring(queryStart + 1);
            String[] params = query.split("&");

            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                    return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting parameter: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Exchanges authorization code for access token using HTTPClient
     * @param authorizationCode The authorization code from callback
     * @return Access token, or null if exchange fails
     */
    private String exchangeCodeForToken(String authorizationCode) {
        try {
            // Build POST data
            Map<String, String> params = new HashMap<>();
            params.put("code", authorizationCode);
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("redirect_uri", redirectUri);
            params.put("grant_type", "authorization_code");

            // Send POST request using HTTPClient
            String response = HTTPClient.post(tokenUrl, params);

            // Parse JSON response
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            return json.get("access_token").getAsString();

        } catch (Exception e) {
            logger.error("Error exchanging code for token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Fetches user profile information from Google using HTTPClient
     * @param accessToken The OAuth access token
     * @return User object with profile data, or null if fetch fails
     */
    private User fetchUserInfo(String accessToken) {
        try {
            String url = userinfoUrl + "?access_token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
            String response = HTTPClient.get(url);

            // Parse JSON response
            JsonObject userInfo = JsonParser.parseString(response).getAsJsonObject();

            // Extract user data
            String googleId = userInfo.get("id").getAsString();
            String email = userInfo.has("email") ? userInfo.get("email").getAsString() : null;
            String name = userInfo.has("name") ? userInfo.get("name").getAsString() : null;

            // Generate username
            String username = generateUsername(email, name, googleId);

            logger.info("Successfully fetched user info for: {}", username);

            return new User(username, email, "google", googleId);

        } catch (Exception e) {
            logger.error("Error fetching user info: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generates a username from Google profile data
     * @param email User's email
     * @param name User's display name
     * @param googleId Google user ID
     * @return A username string
     */
    private String generateUsername(String email, String name, String googleId) {
        if (email != null && !email.isEmpty()) {
            // Use part before @ in email
            return email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        } else if (name != null && !name.isEmpty()) {
            // Use name with spaces removed
            return name.replaceAll("\\s+", "").replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        } else {
            // Fallback to google ID
            return "google_user_" + googleId.substring(0, Math.min(8, googleId.length()));
        }
    }
}