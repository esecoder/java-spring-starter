package io.github.esecoder.jss.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Preset configurations for the "Quick Start" mode.
 * Matches the TypeScript version's preset system.
 */
public final class Presets {

    private Presets() {}

    /**
     * Recommended tools per category (used by "Recommended" preset).
     */
    public static final Map<String, String> RECOMMENDED = new LinkedHashMap<>();
    static {
        RECOMMENDED.put("Linter",      null);       // no linter by default for Java
        RECOMMENDED.put("Formatting",  "spotless");
        RECOMMENDED.put("Testing",     "testcontainers");
        RECOMMENDED.put("Documentation", "springdoc");
        RECOMMENDED.put("Infrastructure", "docker");
        RECOMMENDED.put("CI/CD",       "github-actions");
    }

    /**
     * Minimal preset: no devtools, just the template.
     */
    public static final List<String> MINIMAL = List.of();

    /**
     * Full preset: one tool from every category.
     */
    public static final List<String> FULL = List.of(
        "checkstyle", "spotless", "testcontainers", "springdoc", "docker", "github-actions"
    );

    /**
     * Recommended preset: sensible defaults.
     */
    public static final List<String> RECOMMENDED_LIST = List.of(
        "spotless", "springdoc", "docker", "github-actions"
    );

    /**
     * Available quick profiles.
     */
    public enum Profile {
        MINIMAL("Minimal", "Template only (no additional devtools)"),
        RECOMMENDED("Recommended", "Balanced default toolchain"),
        FULL("Full", "One tool from each category");

        public final String label;
        public final String description;

        Profile(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public List<String> getTools() {
            return switch (this) {
                case MINIMAL -> Presets.MINIMAL;
                case RECOMMENDED -> Presets.RECOMMENDED_LIST;
                case FULL -> Presets.FULL;
            };
        }
    }
}
