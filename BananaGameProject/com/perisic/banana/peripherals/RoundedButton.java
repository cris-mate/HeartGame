package com.perisic.banana.peripherals;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A custom Swing JButton implementation that displays a button
 * with rounded corners and supports dynamic corner radius adjustment.
 * Supports anti-aliased rendering and configurable radius.
 */
 public class RoundedButton extends JButton {

    private int cornerRadius = 30;

    /**
     * Constructs a RoundedButton with the given label text.
     *
     * @param name the text to display on the button
     */
    public RoundedButton(String name) {
        super(name);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
    }

    /**
     * Sets the radius of the button's rounded corners.
     *
     * @param radius the radius in pixels
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    /**
     * Gets the current corner radius of the button.
     *
     * @return the radius in pixels
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Custom paint method to render the button with rounded corners
     * and antialiasing.
     *
     * @param g the Graphics context to use for painting
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fillColor = getModel().isArmed() ? getBackground().darker() : getBackground();
        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(getText());
        int textHeight = fm.getAscent();
        int x = (getWidth() - textWidth) / 2;
        int y = (getHeight() + textHeight) / 2 - 4;
        g2.drawString(getText(), x, y);

        g2.dispose();
    }

    /**
     * Paints the border of the button using rounded corners.
     *
     * @param g the Graphics context to use for painting
     */
    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        g2.dispose();
    }
}
