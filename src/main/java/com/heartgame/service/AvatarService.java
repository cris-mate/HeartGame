package com.heartgame.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Service for fetching user avatars from the DiceBear Avatars API
 * Demonstrates interoperability with a JSON-based REST API
 * Uses DiceBear API v7.x
 */
public class AvatarService {

    private static final Logger logger = LoggerFactory.getLogger(AvatarService.class);
    private static final String DICEBEAR_API_URL = "https://api.dicebear.com/7.x/avataaars/svg";
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;

    /**
     * Fetches an avatar image for a given username
     * The avatar is deterministic - same username always produces same avatar
     *
     * @param username The username to generate avatar for
     * @return BufferedImage of the avatar, or null if fetch fails
     */
    public BufferedImage fetchAvatar(String username) {
        if (username == null || username.isEmpty()) {
            logger.warn("Cannot fetch avatar for null or empty username");
            return null;
        }

        try {
            // Build API URL with username as seed
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
            String apiUrl = DICEBEAR_API_URL + "?seed=" + encodedUsername;

            logger.debug("Fetching avatar for user '{}' from DiceBear API", username);

            // Fetch SVG data
            String svgData = fetchDataFromUrl(apiUrl);
            if (svgData == null || svgData.isEmpty()) {
                logger.error("Received empty response from DiceBear API");
                return null;
            }

            // Convert SVG to PNG BufferedImage
            // Note: For simplicity, we're requesting PNG format from DiceBear
            // Alternative API that returns PNG directly
            String pngApiUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + encodedUsername + "&size=128";
            return fetchImageFromUrl(pngApiUrl);

        } catch (Exception e) {
            logger.error("Failed to fetch avatar for user '{}'", username, e);
            return null;
        }
    }

    /**
     * Fetches avatar metadata in JSON format
     * Demonstrates JSON parsing capabilities
     *
     * @param username The username to get metadata for
     * @return JSON string with avatar information, or null if fetch fails
     */
    public String fetchAvatarMetadata(String username) {
        if (username == null || username.isEmpty()) {
            logger.warn("Cannot fetch avatar metadata for null or empty username");
            return null;
        }

        try {
            // DiceBear also provides JSON metadata endpoint
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
            String jsonApiUrl = "https://api.dicebear.com/7.x/avataaars/json?seed=" + encodedUsername;

            logger.debug("Fetching avatar metadata for user '{}'", username);

            String jsonData = fetchDataFromUrl(jsonApiUrl);
            if (jsonData != null) {
                // Parse JSON to validate format
                JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
                logger.info("Successfully fetched avatar metadata: {} keys", jsonObject.size());
                return jsonData;
            }

            return null;

        } catch (Exception e) {
            logger.error("Failed to fetch avatar metadata for user '{}'", username, e);
            return null;
        }
    }

    /**
     * Fetches data from a URL and returns as String
     *
     * @param urlString The URL to fetch from
     * @return Response data as string, or null on error
     * @throws IOException if network error occurs
     */
    private String fetchDataFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (InputStream inputStream = connection.getInputStream();
                     ByteArrayOutputStream result = new ByteArrayOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        result.write(buffer, 0, length);
                    }
                    return result.toString(StandardCharsets.UTF_8);
                }
            } else {
                logger.warn("DiceBear API returned status code: {}", responseCode);
                return null;
            }

        } finally {
            connection.disconnect();
        }
    }

    /**
     * Fetches an image from a URL and returns as BufferedImage
     *
     * @param urlString The URL to fetch image from
     * @return BufferedImage of the fetched image, or null on error
     * @throws IOException if network or image decoding error occurs
     */
    private BufferedImage fetchImageFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "image/png");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (InputStream inputStream = connection.getInputStream()) {
                    BufferedImage image = ImageIO.read(inputStream);
                    if (image == null) {
                        logger.error("Failed to decode image from DiceBear API");
                        return null;
                    }
                    logger.debug("Successfully fetched and decoded avatar image");
                    return image;
                }
            } else {
                logger.warn("DiceBear API returned status code: {}", responseCode);
                return null;
            }

        } finally {
            connection.disconnect();
        }
    }
}