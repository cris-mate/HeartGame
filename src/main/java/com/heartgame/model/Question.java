package com.heartgame.model;

import java.awt.image.BufferedImage;

/**
 * Represents a single question in the game, containing the visual representation
 * (image) and the correct answer (solution)
 */
public class Question {

    private final BufferedImage image;
    private final int solution;

    /**
     * Constructs a new Question
     * @param image     The image for the question
     * @param solution  The correct numerical solution
     */
    public Question(BufferedImage image, int solution) {
        this.image = image;
        this.solution = solution;
    }

    /**
     * @return the image of the game
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * @return the solution of the game
     */
    public int getSolution() {
        return solution;
    }
}

