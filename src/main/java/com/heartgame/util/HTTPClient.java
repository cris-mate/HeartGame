package com.heartgame.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Utility class for making HTTP requests
 * Consolidates HTTP logic from all service classes
 * Provides both GET and POST methods with configurable timeouts
 */
public final class HTTPClient {

    private static final Logger logger = LoggerFactory.getLogger(HTTPClient.class);
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    private static final int DEFAULT_READ_TIMEOUT = 30000;

    // Private constructor to prevent instantiation
    private HTTPClient() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Performs an HTTP GET request and returns the response as a String
     * @param urlString The URL to request
     * @return Response body as String
     * @throws IOException if network error occurs
     */
    public static String get(String urlString) throws IOException {
        return get(urlString, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * Performs an HTTP GET request with custom timeouts
     * @param urlString The URL to request
     * @param connectTimeout Connection timeout in milliseconds
     * @param readTimeout Read timeout in milliseconds
     * @return Response body as String
     * @throws IOException if network error occurs
     */
    public static String get(String urlString, int connectTimeout, int readTimeout) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                String errorMessage = "HTTP GET failed for " + urlString + " with status: " + responseCode;
                logger.error(errorMessage);
                throw new IOException(errorMessage);
            }

            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream result = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }

                logger.debug("HTTP GET successful: {}", urlString);
                return result.toString(StandardCharsets.UTF_8);
            }

        } finally {
            connection.disconnect();
        }
    }

    /**
     * Performs an HTTP GET request and returns the response as an InputStream
     * Useful for downloading images or binary data
     * NOTE: Caller is responsible for closing the returned InputStream
     * @param urlString The URL to request
     * @return InputStream containing response body
     * @throws IOException if network error occurs
     */
    public static InputStream getStream(String urlString) throws IOException {
        return getStream(urlString, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * Performs an HTTP GET request and returns the response as an InputStream with custom timeouts
     * NOTE: Caller is responsible for closing the returned InputStream
     * @param urlString The URL to request
     * @param connectTimeout Connection timeout in milliseconds
     * @param readTimeout Read timeout in milliseconds
     * @return InputStream containing response body
     * @throws IOException if network error occurs
     */
    public static InputStream getStream(String urlString, int connectTimeout, int readTimeout) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            connection.disconnect();
            String errorMessage = "HTTP GET failed for " + urlString + " with status: " + responseCode;
            logger.error(errorMessage);
            throw new IOException(errorMessage);
        }

        logger.debug("HTTP GET stream successful: {}", urlString);
        return connection.getInputStream();
    }

    /**
     * Performs an HTTP POST request with form data
     * @param urlString The URL to request
     * @param formData Map of form parameters
     * @return Response body as String
     * @throws IOException if network error occurs
     */
    public static String post(String urlString, Map<String, String> formData) throws IOException {
        return post(urlString, formData, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    /**
     * Performs an HTTP POST request with form data and custom timeouts
     * @param urlString The URL to request
     * @param formData Map of form parameters
     * @param connectTimeout Connection timeout in milliseconds
     * @param readTimeout Read timeout in milliseconds
     * @return Response body as String
     * @throws IOException if network error occurs
     */
    public static String post(String urlString, Map<String, String> formData,
                              int connectTimeout, int readTimeout) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setDoOutput(true);

            // Build POST data
            String postData = buildFormData(formData);

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            // Read response
            int responseCode = connection.getResponseCode();
            InputStream inputStream = (responseCode == 200)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }

                String response = result.toString(StandardCharsets.UTF_8);

                if (responseCode != 200) {
                    logger.error("HTTP POST failed for {} with status: {}. Response: {}",
                            urlString, responseCode, response);
                    throw new IOException("HTTP POST failed with status: " + responseCode);
                }

                logger.debug("HTTP POST successful: {}", urlString);
                return response;
            }

        } finally {
            connection.disconnect();
        }
    }

    /**
     * Builds URL-encoded form data from a map of parameters
     * @param params Map of parameter names to values
     * @return URL-encoded form data string
     */
    private static String buildFormData(Map<String, String> params) {
        StringBuilder formData = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (formData.length() > 0) {
                formData.append('&');
            }
            formData.append(java.net.URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            formData.append('=');
            formData.append(java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return formData.toString();
    }
}