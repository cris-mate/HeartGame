package com.heartgame.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages application configuration loaded from application.properties
 * Implemented as a Singleton
 */
public final class ConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    private static ConfigurationManager instance;
    private final Properties properties;

    private ConfigurationManager() {
        properties = new Properties();
        loadProperties();
    }

    /**
     * Loads properties from application.properties file
     */
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.warn("application.properties not found, using defaults");
                loadDefaults();
                return;
            }
            properties.load(input);
            logger.info("Application configuration loaded successfully");
        } catch (IOException e) {
            logger.error("Failed to load application.properties", e);
            loadDefaults();
        }
    }

    /**
     * Loads default configuration values
     */
    private void loadDefaults() {
        properties.setProperty("heartgame.api.url",
                "https://marcconrad.com/uob/heart/api.php?out=csv&base64=yes");
        properties.setProperty("heartgame.api.timeout", "30000");
        properties.setProperty("google.oauth.callback.port", "8888");
        properties.setProperty("google.oauth.tokens.directory", "tokens");
        properties.setProperty("app.name", "HeartGame");
        properties.setProperty("app.version", "2.0");
    }

    /**
     * @return The single instance of ConfigurationManager
     */
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    /**
     * Gets a configuration property value
     * @param key The property key
     * @return The property value, or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gets a configuration property value with a default
     * @param key The property key
     * @param defaultValue The default value if property not found
     * @return The property value, or defaultValue if not found
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets an integer property value
     * @param key The property key
     * @param defaultValue The default value if property not found or invalid
     * @return The property value as an integer
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for property '{}': {}", key, value);
            return defaultValue;
        }
    }

    /**
     * @return The Heart Game API URL
     */
    public String getHeartGameApiUrl() {
        return getProperty("heartgame.api.url");
    }

    /**
     * @return The Google OAuth callback port
     */
    public int getOAuthCallbackPort() {
        return getIntProperty("google.oauth.callback.port", 8888);
    }

    /**
     * @return The tokens directory path
     */
    public String getTokensDirectory() {
        return getProperty("google.oauth.tokens.directory", "tokens");
    }
}