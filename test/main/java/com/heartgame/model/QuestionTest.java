package com.heartgame.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for Question model
 * Tests constructors, getters, and immutability
 */
@DisplayName("Question Model Tests")
class QuestionTest {

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Should create question with image and solution")
    void testConstructor() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        int solution = 42;

        Question question = new Question(image, solution);

        assertNotNull(question);
        assertEquals(image, question.getImage());
        assertEquals(solution, question.getSolution());
    }

    @Test
    @DisplayName("Should create question with null image")
    void testConstructorWithNullImage() {
        Question question = new Question(null, 10);

        assertNull(question.getImage());
        assertEquals(10, question.getSolution());
    }

    // ========== Getter Tests ==========

    @Test
    @DisplayName("Should get image")
    void testGetImage() {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Question question = new Question(image, 5);

        assertEquals(image, question.getImage());
    }

    @Test
    @DisplayName("Should get solution")
    void testGetSolution() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Question question = new Question(image, 99);

        assertEquals(99, question.getSolution());
    }

    @Test
    @DisplayName("Should return same image reference")
    void testImageReference() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Question question = new Question(image, 10);

        assertSame(image, question.getImage(),
                "Should return same image reference");
    }

    // ========== Immutability Tests ==========

    @Test
    @DisplayName("Image and solution should be final (immutable)")
    void testImmutability() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Question question = new Question(image, 10);

        // Fields are final, so there are no setters
        // Multiple calls to getters should return consistent values
        assertEquals(image, question.getImage());
        assertEquals(image, question.getImage());
        assertEquals(10, question.getSolution());
        assertEquals(10, question.getSolution());
    }

    // ========== Different Image Types Tests ==========

    @Test
    @DisplayName("Should handle TYPE_INT_RGB image")
    void testTypeIntRgbImage() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Question question = new Question(image, 5);

        assertEquals(BufferedImage.TYPE_INT_RGB, question.getImage().getType());
    }

    @Test
    @DisplayName("Should handle TYPE_INT_ARGB image")
    void testTypeIntArgbImage() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Question question = new Question(image, 5);

        assertEquals(BufferedImage.TYPE_INT_ARGB, question.getImage().getType());
    }

    @Test
    @DisplayName("Should handle TYPE_BYTE_GRAY image")
    void testTypeByteGrayImage() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_GRAY);
        Question question = new Question(image, 5);

        assertEquals(BufferedImage.TYPE_BYTE_GRAY, question.getImage().getType());
    }

    // ========== Different Solution Values Tests ==========

    @Test
    @DisplayName("Should handle zero solution")
    void testZeroSolution() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Question question = new Question(image, 0);

        assertEquals(0, question.getSolution());
    }

    @Test
    @DisplayName("Should handle negative solution")
    void testNegativeSolution() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Question question = new Question(image, -5);

        assertEquals(-5, question.getSolution());
    }

    @Test
    @DisplayName("Should handle large positive solution")
    void testLargePositiveSolution() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Question question = new Question(image, 999999);

        assertEquals(999999, question.getSolution());
    }

    @Test
    @DisplayName("Should handle single digit solution")
    void testSingleDigitSolution() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Question question = new Question(image, 7);

        assertEquals(7, question.getSolution());
    }

    // ========== Different Image Sizes Tests ==========

    @Test
    @DisplayName("Should handle small image")
    void testSmallImage() {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Question question = new Question(image, 5);

        assertEquals(10, question.getImage().getWidth());
        assertEquals(10, question.getImage().getHeight());
    }

    @Test
    @DisplayName("Should handle large image")
    void testLargeImage() {
        BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Question question = new Question(image, 5);

        assertEquals(1920, question.getImage().getWidth());
        assertEquals(1080, question.getImage().getHeight());
    }

    @Test
    @DisplayName("Should handle rectangular image")
    void testRectangularImage() {
        BufferedImage image = new BufferedImage(300, 150, BufferedImage.TYPE_INT_RGB);
        Question question = new Question(image, 5);

        assertEquals(300, question.getImage().getWidth());
        assertEquals(150, question.getImage().getHeight());
    }

    @Test
    @DisplayName("Should handle 1x1 pixel image")
    void testOnePixelImage() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Question question = new Question(image, 5);

        assertEquals(1, question.getImage().getWidth());
        assertEquals(1, question.getImage().getHeight());
    }

    // ========== Multiple Question Creation Tests ==========

    @Test
    @DisplayName("Should create multiple independent questions")
    void testMultipleQuestions() {
        BufferedImage image1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage image2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        BufferedImage image3 = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);

        Question question1 = new Question(image1, 10);
        Question question2 = new Question(image2, 20);
        Question question3 = new Question(image3, 30);

        assertEquals(10, question1.getSolution());
        assertEquals(20, question2.getSolution());
        assertEquals(30, question3.getSolution());

        assertSame(image1, question1.getImage());
        assertSame(image2, question2.getImage());
        assertSame(image3, question3.getImage());
    }

    @Test
    @DisplayName("Should handle different questions with same solution")
    void testQuestionsWithSameSolution() {
        BufferedImage image1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage image2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

        Question question1 = new Question(image1, 42);
        Question question2 = new Question(image2, 42);

        assertEquals(42, question1.getSolution());
        assertEquals(42, question2.getSolution());
        assertNotSame(question1.getImage(), question2.getImage());
    }

    // ========== Image Data Integrity Tests ==========

    @Test
    @DisplayName("Should maintain image data after creation")
    void testImageDataIntegrity() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        // Set a specific pixel value
        image.setRGB(50, 50, 0xFF0000); // Red pixel

        Question question = new Question(image, 5);

        // Verify the pixel is still the same
        assertEquals(0xFF0000, question.getImage().getRGB(50, 50),
                "Image data should be maintained");
    }
}
