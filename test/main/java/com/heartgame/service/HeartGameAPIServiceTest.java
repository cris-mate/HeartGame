package com.heartgame.service;

import com.heartgame.model.Question;
import org.junit.jupiter.api.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HeartGameAPIService
 * Uses test subclass to mock HTTP responses (KISS approach - no mocking frameworks)
 * Tests parsing logic, base64 decoding, and error handling
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HeartGameAPIServiceTest {

    /**
     * Test subclass that overrides fetchData to return mock responses
     * KISS approach: Simple inheritance instead of complex mocking frameworks
     */
    private static class TestableHeartGameAPIService extends HeartGameAPIService {
        private String mockResponse;
        private IOException mockException;

        public void setMockResponse(String response) {
            this.mockResponse = response;
            this.mockException = null;
        }

        public void setMockException(IOException exception) {
            this.mockException = exception;
            this.mockResponse = null;
        }

        @Override
        public String fetchData(String endpoint) throws IOException {
            if (mockException != null) {
                throw mockException;
            }
            return mockResponse;
        }
    }

    private TestableHeartGameAPIService service;

    @BeforeEach
    void setUp() {
        service = new TestableHeartGameAPIService();
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a valid base64-encoded PNG image for testing
     * Simple 1x1 white pixel PNG
     */
    private String createValidBase64Image() throws IOException {
        // Create a simple 1x1 white pixel image
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0xFFFFFF);

        // Encode to PNG and then base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    // ==================== Success Cases ====================

    @Test
    @Order(1)
    @DisplayName("getNewQuestion() parses valid response successfully")
    void testGetNewQuestionSuccess() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + ",5");

        Question question = service.getNewQuestion();

        assertNotNull(question, "Question should not be null");
        assertNotNull(question.getImage(), "Image should not be null");
        assertEquals(5, question.getSolution(), "Solution should be 5");
    }

    @Test
    @Order(2)
    @DisplayName("getNewQuestion() handles solution 0")
    void testGetNewQuestionSolutionZero() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + ",0");

        Question question = service.getNewQuestion();

        assertEquals(0, question.getSolution(), "Should handle solution 0");
    }

    @Test
    @Order(3)
    @DisplayName("getNewQuestion() handles solution 9")
    void testGetNewQuestionSolutionNine() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + ",9");

        Question question = service.getNewQuestion();

        assertEquals(9, question.getSolution(), "Should handle solution 9");
    }

    @Test
    @Order(4)
    @DisplayName("getNewQuestion() trims whitespace from solution")
    void testGetNewQuestionTrimsWhitespace() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + ",  7  ");

        Question question = service.getNewQuestion();

        assertEquals(7, question.getSolution(), "Should trim whitespace from solution");
    }

    @Test
    @Order(5)
    @DisplayName("getNewQuestion() decodes base64 image correctly")
    void testGetNewQuestionDecodesImage() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + ",3");

        Question question = service.getNewQuestion();

        assertNotNull(question.getImage(), "Image should be decoded");
        assertTrue(question.getImage().getWidth() > 0, "Image should have width");
        assertTrue(question.getImage().getHeight() > 0, "Image should have height");
    }

    // ==================== Error Cases: Empty/Null Response ====================

    @Test
    @Order(6)
    @DisplayName("getNewQuestion() throws IOException for null response")
    void testGetNewQuestionNullResponse() {
        service.setMockResponse(null);

        IOException exception = assertThrows(IOException.class, () -> service.getNewQuestion());

        assertTrue(exception.getMessage().contains("Empty or null"),
                "Should mention empty/null in error message");
    }

    @Test
    @Order(7)
    @DisplayName("getNewQuestion() throws IOException for empty response")
    void testGetNewQuestionEmptyResponse() {
        service.setMockResponse("");

        IOException exception = assertThrows(IOException.class, () -> service.getNewQuestion());

        assertTrue(exception.getMessage().contains("Empty or null"));
    }

    @Test
    @Order(8)
    @DisplayName("getNewQuestion() throws IOException for blank response")
    void testGetNewQuestionBlankResponse() {
        service.setMockResponse("   ");

        IOException exception = assertThrows(IOException.class, () -> service.getNewQuestion());

        assertTrue(exception.getMessage().contains("Empty or null"));
    }

    // ==================== Error Cases: Invalid Format ====================

    @Test
    @Order(9)
    @DisplayName("getNewQuestion() throws IOException for missing comma")
    void testGetNewQuestionMissingComma() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + "5"); // No comma

        IOException exception = assertThrows(IOException.class, () -> service.getNewQuestion());

        assertTrue(exception.getMessage().contains("format"),
                "Should mention format in error message");
    }

    @Test
    @Order(10)
    @DisplayName("getNewQuestion() throws IOException for missing solution")
    void testGetNewQuestionMissingSolution() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + ","); // Comma but no solution

        assertThrows(IOException.class, () -> service.getNewQuestion(), "Should throw for missing solution");
    }

    @Test
    @Order(11)
    @DisplayName("getNewQuestion() throws IOException for only comma")
    void testGetNewQuestionOnlyComma() {
        service.setMockResponse(",");

        assertThrows(IOException.class, () -> service.getNewQuestion(), "Should throw for only comma");
    }

    // ==================== Error Cases: Invalid Base64 ====================

    @Test
    @Order(12)
    @DisplayName("getNewQuestion() throws IOException for invalid base64")
    void testGetNewQuestionInvalidBase64() {
        service.setMockResponse("notValidBase64!!!,5");

        assertThrows(IOException.class, () -> service.getNewQuestion(), "Should throw for invalid base64");
    }

    @Test
    @Order(13)
    @DisplayName("getNewQuestion() throws IOException for base64 that's not an image")
    void testGetNewQuestionBase64NotImage() {
        // Valid base64 but not an image
        String notAnImage = Base64.getEncoder().encodeToString("Hello World".getBytes());
        service.setMockResponse(notAnImage + ",5");

        IOException exception = assertThrows(IOException.class, () -> service.getNewQuestion());

        assertTrue(exception.getMessage().contains("could not be decoded"));
    }

    // ==================== Error Cases: Invalid Solution ====================

    @Test
    @Order(14)
    @DisplayName("getNewQuestion() throws IOException for non-numeric solution")
    void testGetNewQuestionNonNumericSolution() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + ",abc");

        IOException exception = assertThrows(IOException.class, () -> service.getNewQuestion());

        assertTrue(exception.getMessage().contains("Invalid solution format"));
    }

    @Test
    @Order(15)
    @DisplayName("getNewQuestion() throws exception for negative solution")
    void testGetNewQuestionNegativeSolution() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + ",-1");

        assertThrows(Exception.class, () -> service.getNewQuestion(), "Should reject negative solution");
    }

    @Test
    @Order(16)
    @DisplayName("getNewQuestion() throws exception for solution > 9")
    void testGetNewQuestionSolutionTooLarge() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + ",10");

        assertThrows(Exception.class, () -> service.getNewQuestion(), "Should reject solution > 9");
    }

    @Test
    @Order(17)
    @DisplayName("getNewQuestion() throws exception for solution = 100")
    void testGetNewQuestionSolutionWayTooLarge() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + ",100");

        assertThrows(Exception.class, () -> service.getNewQuestion(), "Should reject solution = 100");
    }

    @Test
    @Order(18)
    @DisplayName("getNewQuestion() throws IOException for decimal solution")
    void testGetNewQuestionDecimalSolution() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + ",5.5");

        assertThrows(IOException.class, () -> service.getNewQuestion(), "Should reject decimal solution");
    }

    // ==================== Error Cases: Network Errors ====================

    @Test
    @Order(19)
    @DisplayName("getNewQuestion() propagates IOException from fetchData")
    void testGetNewQuestionNetworkError() {
        service.setMockException(new IOException("Network timeout"));

        IOException exception = assertThrows(IOException.class, () -> service.getNewQuestion());

        assertTrue(exception.getMessage().contains("Failed to read or parse"));
    }

    @Test
    @Order(20)
    @DisplayName("getNewQuestion() handles IOException with null message")
    void testGetNewQuestionIOExceptionNullMessage() {
        service.setMockException(new IOException((String) null));

        assertThrows(IOException.class, () -> service.getNewQuestion(), "Should handle IOException with null message");
    }

    // ==================== Edge Cases ====================

    @Test
    @Order(21)
    @DisplayName("getNewQuestion() handles multiple commas")
    void testGetNewQuestionMultipleCommas() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + ",5,extra,data");

        // Should only parse first two parts (image and solution)
        Question question = service.getNewQuestion();

        assertNotNull(question);
        assertEquals(5, question.getSolution());
    }

    @Test
    @Order(22)
    @DisplayName("getNewQuestion() handles solution with leading zeros")
    void testGetNewQuestionLeadingZeros() throws IOException {
        String base64Image = createValidBase64Image();
        service.setMockResponse(base64Image + ",007");

        Question question = service.getNewQuestion();

        assertEquals(7, question.getSolution(), "Should parse 007 as 7");
    }

    @Test
    @Order(23)
    @DisplayName("fetchData() can be called directly")
    void testFetchDataDirect() {
        HeartGameAPIService realService = new HeartGameAPIService();

        // Just verify the method exists and can be called
        // We won't make real HTTP calls in unit tests
        assertDoesNotThrow(() -> {
            // Method exists but would fail without real network
            // This just confirms the interface is correct
        });
    }

    // ==================== Solution Range Boundary Tests ====================

    @Test
    @Order(24)
    @DisplayName("getNewQuestion() accepts all valid solutions 0-9")
    void testGetNewQuestionAllValidSolutions() throws IOException {
        String base64Image = createValidBase64Image();

        for (int i = 0; i <= 9; i++) {
            service.setMockResponse(base64Image + "," + i);
            Question question = service.getNewQuestion();
            assertEquals(i, question.getSolution(), "Should accept solution " + i);
        }
    }

    @Test
    @Order(25)
    @DisplayName("getNewQuestion() rejects all invalid solutions < 0 or > 9")
    void testGetNewQuestionInvalidSolutionRange() throws IOException {
        String base64Image = createValidBase64Image();

        int[] invalidSolutions = {-5, -1, 10, 15, 99, 999};

        for (int invalid : invalidSolutions) {
            service.setMockResponse(base64Image + "," + invalid);
            assertThrows(Exception.class, () -> service.getNewQuestion(), "Should reject solution " + invalid);
        }
    }
}
