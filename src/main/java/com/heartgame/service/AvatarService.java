package com.heartgame.service;

import com.heartgame.util.HTTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service for fetching user avatars from the DiceBear Avatars API
 * Implements APIService to demonstrate JSON-based REST API interoperability
 * Uses HTTPClient utility for network requests (DRY principle)
 * Uses DiceBear API v7.x
 */
public class AvatarService implements APIService {

    private static final Logger logger = LoggerFactory.getLogger(AvatarService.class);
    private static final String DICEBEAR_BASE_URL = "https://api.dicebear.com/7.x/avataaars";

    /**
     * Fetches raw data from the DiceBear API
     * Implements APIService contract
     * @param endpoint The API endpoint (e.g., "json?seed=username")
     * @return Response data as string
     * @throws IOException If network or parsing error occurs
     */
    @Override
    public String fetchData(String endpoint) throws IOException {
        String fullUrl = DICEBEAR_BASE_URL + "/" + endpoint;
        return HTTPClient.get(fullUrl);
    }

    /**
     * Fetches an avatar image for a given username
     * The avatar is deterministic - same username always produces same avatar
     * @param username The username to generate avatar for
     * @return BufferedImage of the avatar, or null if fetch fails
     */
    public BufferedImage fetchAvatar(String username) {
        if (username == null || username.isEmpty()) {
            logger.warn("Cannot fetch avatar for null or empty username");
            return null;
        }

        try {
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
            String fullUrl = DICEBEAR_BASE_URL + "/png?seed=" + encodedUsername + "&size=128";

            logger.debug("Fetching avatar for user '{}'", username);

            try (InputStream inputStream = HTTPClient.getStream(fullUrl)) {
                BufferedImage image = ImageIO.read(inputStream);
                if (image == null) {
                    throw new IOException("Failed to decode image");
                }
                logger.debug("Successfully fetched avatar for '{}'", username);
                return image;
            }

        } catch (Exception e) {
            logger.error("Failed to fetch avatar for user '{}'", username, e);
            return null;
        }
    }

    /**
     * Fetches avatar metadata in JSON format
     * Demonstrates JSON API interaction
     * @param username The username to get metadata for
     * @return JSON string with avatar information, or null if fetch fails
     */
    public String fetchAvatarMetadata(String username) {
        if (username == null || username.isEmpty()) {
            logger.warn("Cannot fetch avatar metadata for null or empty username");
            return null;
        }

        try {
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
            String endpoint = "json?seed=" + encodedUsername;

            logger.debug("Fetching avatar metadata for user '{}'", username);

            String jsonData = fetchData(endpoint);
            if (jsonData != null) {
                logger.info("Successfully fetched avatar metadata for '{}'", username);
            }
            return jsonData;

        } catch (Exception e) {
            logger.error("Failed to fetch avatar metadata for user '{}'", username, e);
            return null;
        }
    }
}