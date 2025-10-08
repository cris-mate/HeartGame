package service;

import model.Question;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Base64;

/**
 * Handles communication with the external game API to fetch questions
 */
public class GameAPIService {

    /**
     * Reads the content from a given URL and returns it as a String
     * @param urlString The URL to read from
     * @return The content of the URL as a String, or null if an error occurs
     */
    private static String readUrl(String urlString)  {
        try {
            URL url = new URI(urlString).toURL();
            InputStream inputStream = url.openStream();

            // Choose anyone of
            // https://stackoverflow.com/questions/309424/how-do-i-read-convert-an-inputstream-into-a-string-in-java
            // to convert InputStream to String.
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } catch (Exception e) {
            /* TODO: proper exception handling when URL cannot be read. */
            System.out.println("An error occurred: " + e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetches a random game question from the API
     * @return A new Question object or null if an error occurs
     */
    public Question getRandomGame() {
        // See http://marconrad.com/uob/tomato for details of usage of the api.

        String tomatoAPI = "https://marcconrad.com/uob/heart/api.php?out=csv&base64=yes";
        String dataRaw = readUrl(tomatoAPI);
        if (dataRaw != null) {
            String[] data = dataRaw.split(",");
            byte[] decodeImg = Base64.getDecoder().decode(data[0]);
            ByteArrayInputStream quest = new ByteArrayInputStream(decodeImg);
            int solution = Integer.parseInt(data[1]);
            try {
                BufferedImage img = ImageIO.read(quest);
                return new Question(img, solution);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }
}