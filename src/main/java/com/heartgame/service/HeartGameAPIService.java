package com.heartgame.service;

import com.heartgame.model.Question;
import com.heartgame.util.ConfigurationManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles communication with the external Heart Game API to fetch questions
 * Uses ConfigurationManager for externalized API URL
 * Uses SLF4J logging and implements HTTP timeouts
 */
public class HeartGameAPIService implements APIService {

    private static final Logger logger = LoggerFactory.getLogger(HeartGameAPIService.class);
    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 30000;
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
     * Implements connection and read timeouts
     * @param urlString The URL to read from
     * @return The content of the URL as a String
     * @throws IOException if an error occurs during network communication
     */
    private static String readUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            // Set timeouts to prevent indefinite hanging
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream result = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                return result.toString(StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.error("Error reading from URL: {} - {}", urlString, e.getMessage());
            throw new IOException("An error occurred while reading URL: " + urlString, e);
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Fetches a random game question from the HeartGame API
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
                logger.error("Unexpected solution format received from API: " + e.getMessage());
                throw new IOException("Invalid solution format in API response", e);
            }

            logger.debug("Successfully fetched new question with solution: {}", solution);
            return new Question(image, solution);
        } catch (IOException e) {
            logger.error("Failed to parse game data from API: {}", e.getMessage());
            throw new IOException("Failed to read or parse game data from API", e);
        }
    }
}