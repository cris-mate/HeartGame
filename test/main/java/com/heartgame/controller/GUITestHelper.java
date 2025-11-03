package com.heartgame.controller;

import javax.swing.*;
import java.lang.reflect.Field;

/**
 * Helper class for GUI testing using reflection
 * Provides methods to set private fields in GUI classes for testing
 * Follows KISS principle - simple reflection utilities
 */
public class GUITestHelper {

    /**
     * Sets text in a private JTextField using reflection
     * @param gui The GUI object containing the field
     * @param fieldName The name of the private field
     * @param value The text value to set
     */
    public static void setTextField(Object gui, String fieldName, String value) {
        try {
            Field field = gui.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            JTextField textField = (JTextField) field.get(gui);
            textField.setText(value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }

    /**
     * Sets text in a private JPasswordField using reflection
     * @param gui The GUI object containing the field
     * @param fieldName The name of the private field
     * @param value The password value to set
     */
    public static void setPasswordField(Object gui, String fieldName, String value) {
        try {
            Field field = gui.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            JPasswordField passwordField = (JPasswordField) field.get(gui);
            passwordField.setText(value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }

    /**
     * Gets a private button using reflection
     * @param gui The GUI object containing the button
     * @param fieldName The name of the private button field
     * @return The JButton
     */
    public static JButton getButton(Object gui, String fieldName) {
        try {
            Field field = gui.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (JButton) field.get(gui);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get button " + fieldName, e);
        }
    }
}
