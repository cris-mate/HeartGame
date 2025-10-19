package com.heartgame.service;

import com.heartgame.model.Question;
import com.heartgame.util.ConfigurationManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Handles communication with the external Heart Game API to fetch questions
 * Uses ConfigurationManager for externalized API URL
 */
public class HeartGameAPIService implements APIService {

    private static final Logger logger = Logger.getLogger(HeartGameAPIService.class.getName());
    private final String apiUrl;

    /**
     * Constructs a new HeartGameAPIService
     * Loads API URL from configuration
     */
    public HeartGameAPIService() {
        this.apiUrl = ConfigurationManager.getInstance().getHeartGameApiUrl();
    }

    @Override
    public String fetchData(String endpoint) throws IOException {
        return readUrl(endpoint);
    }

    /**
     * Reads the content from a given URL and returns it as a String
     * @param urlString The URL to read from
     * @return The content of the URL as a String, or null if an error occurs
     */
    private static String readUrl(String urlString) throws IOException {
        URL url = new URL(urlString);

        try (InputStream inputStream = url.openStream();
             ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.severe("Error reading from URL: " + urlString + " - " + e.getMessage());
            throw new IOException("An error occurred while reading URL: " + urlString, e);
        }
    }

    /**
     * Fetches a random game question from the Heart game API
     * @return A new Question object or null if an error occurs
     * @throws IOException if fetching or parsing the game data fails
     */
    public Question getNewQuestion() throws IOException {

        BufferedImage image;
        int solution;

        try {
            String dataRaw = fetchData(apiUrl);
            if (dataRaw == null || dataRaw.isBlank()) {
                throw new IOException("Empty or null API response");
            }

            String[] data = dataRaw.split(",");
            if (data.length < 2) {
                throw new IOException("Unexpected API response format");
            }

            byte[] decodedImg = Base64.getDecoder().decode(data[0]);
            try (ByteArrayInputStream quest = new ByteArrayInputStream(decodedImg)) {
                image = ImageIO.read(quest);
                if (image == null) {
                    throw new IOException("Image could not be decoded");
                }
            }

            try {
                solution = Integer.parseInt(data[1].trim());
                if (solution < 0 || solution > 9) {
                    throw new IllegalArgumentException("Solution out of expected range (0-9)");
                }
            } catch (NumberFormatException e) {
                logger.severe("Unexpected solution format received from API: " + e.getMessage());
                throw new IOException("Invalid solution format in API response", e);
            }
            return new Question(image, solution);
        } catch (IOException e) {
            logger.severe("Failed to parse game data from API: " + e.getMessage());
            throw new IOException("Failed to read or parse game data from API", e);
        }
    }
}