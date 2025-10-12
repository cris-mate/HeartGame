package com.heartgame.service;

import java.io.IOException;

/**
 * Defines a contract for services that fetch data from an external API
 */
public interface APIService {
    /**
     * Fetches raw data from a given API endpoint
     * @param endpoint The URL or identifier for the resource
     * @return A string representation of the fetched data (e.g., JSON, CSV)
     * @throws IOException If there is a network or parsing error
     */
    String fetchData(String endpoint) throws IOException;
}
