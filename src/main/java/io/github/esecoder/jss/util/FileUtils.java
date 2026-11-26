package io.github.esecoder.jss.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * File and path utility helpers.
 */
public final class FileUtils {

    private FileUtils() {}

    /**
     * Copy a directory recursively from classpath resources to a target path.
     */
    public static void copyResourceDir(String resourcePath, Path targetDir) throws IOException {
        // We need to copy files from inside the JAR resources
        // For dev mode: files are on the filesystem; for prod: in the JAR
        var url = FileUtils.class.getClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        if ("jar".equals(url.getProtocol())) {
            // Running from JAR — walk the resource entries
            copyFromJar(resourcePath, targetDir);
        } else {
            // Running from filesystem (dev mode) — walk the directory
            Path sourceDir;
            try {
                sourceDir = Path.of(url.toURI());
            } catch (java.net.URISyntaxException e) {
                throw new IOException("Invalid resource URI: " + url, e);
            }
            try (Stream<Path> walk = Files.walk(sourceDir)) {
                for (Path source : walk.toList()) {
                    Path relative = sourceDir.relativize(source);
                    Path destination = targetDir.resolve(relative.toString());
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(destination);
                    } else {
                        Files.createDirectories(destination.getParent());
                        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private static void copyFromJar(String resourcePath, Path targetDir) throws IOException {
        // Read the resource listing from a special file
        // For simplicity in MVP, we just handle resources we know about
        // In production, use Spring's PathMatchingResourcePatternResolver or similar
        throw new IOException(
            "JAR-based resource copying is not yet supported. " +
            "Please run from the development environment (not from a fat JAR). " +
            "This will be fixed in a future release.");
    }

    /**
     * Copy a single resource file to a target path, applying string replacements.
     */
    public static void copyResourceFile(String resourcePath, Path targetPath,
                                         java.util.Map<String, String> replacements) throws IOException {
        String content = readResourceAsString(resourcePath);
        for (var entry : replacements.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        Files.createDirectories(targetPath.getParent());
        Files.writeString(targetPath, content);
    }

    /**
     * Read a classpath resource as a string.
     */
    public static String readResourceAsString(String resourcePath) throws IOException {
        try (InputStream is = FileUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    /**
     * Ensure a directory exists (create if needed).
     */
    public static Path ensureDir(Path dir) throws IOException {
        return Files.createDirectories(dir);
    }
}
