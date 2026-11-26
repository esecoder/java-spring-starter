package io.github.esecoder.jss.cli;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Validation utilities for project name, path, etc.
 */
public final class Validators {

    private Validators() {}

    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 64;

    public static String validateProjectName(String name) {
        if (name == null || name.isBlank()) {
            return "Project name cannot be empty.";
        }
        if (name.length() < MIN_NAME_LENGTH) {
            return "Project name must be at least " + MIN_NAME_LENGTH + " characters.";
        }
        if (name.length() > MAX_NAME_LENGTH) {
            return "Project name must be at most " + MAX_NAME_LENGTH + " characters.";
        }
        if (!name.matches("^[a-zA-Z][a-zA-Z0-9._-]*$")) {
            return "Project name must start with a letter and contain only letters, digits, dots, underscores, and hyphens.";
        }
        return null; // valid
    }

    public static boolean isValidProjectName(String name) {
        return validateProjectName(name) == null;
    }

    public static String validateProjectPath(Path path) {
        if (Files.exists(path)) {
            return "Directory already exists: " + path.toAbsolutePath()
                + "\nPlease choose a different name or remove the existing directory.";
        }
        return null; // valid
    }

    public static boolean isValidProjectPath(Path path) {
        return validateProjectPath(path) == null;
    }

    /**
     * Converts a project name to a valid Java package name segment.
     * Example: "my-cool-app" -> "mycoolapp"
     */
    public static String toPackageName(String projectName) {
        return projectName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    /**
     * Converts a project name to a valid Java class name prefix.
     * Example: "my-cool-app" -> "MyCoolApp"
     */
    public static String toClassName(String projectName) {
        String[] parts = projectName.split("[^a-zA-Z0-9]");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
}
