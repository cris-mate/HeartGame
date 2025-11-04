package com.heartgame.service;

import com.heartgame.model.Question;
import com.heartgame.util.ConfigurationManager;
import com.heartgame.util.HTTPClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles communication with the external Heart Game API to fetch questions
 * Uses ConfigurationManager for externalized API URL
 * Uses HTTPClient utility for network requests
 */
public class HeartGameAPIService implements APIService {

    private static final Logger logger = LoggerFactory.getLogger(HeartGameAPIService.class);
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
        return HTTPClient.get(endpoint);
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
                logger.error("Unexpected solution format received from API: {}", e.getMessage());
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