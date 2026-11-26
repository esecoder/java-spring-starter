package io.github.esecoder.jss.config;

import java.util.List;

/**
 * Development tool definition with category grouping.
 * Matches the TypeScript version's devtools catalog.
 */
public record DevTool(
    String value,
    String label,
    String description,
    String category    // Linter, Testing, Documentation, Infrastructure, CI/CD
) {
    public static final List<DevTool> ALL = List.of(
        // Linters / Static Analysis
        new DevTool("checkstyle", "Checkstyle", "Coding style checker for Java", "Linter"),
        new DevTool("spotbugs", "SpotBugs", "Static analysis to find bugs", "Linter"),

        // Code Formatting
        new DevTool("spotless", "Spotless", "Code formatter — keep code clean & consistent", "Formatting"),

        // Testing
        new DevTool("testcontainers", "Testcontainers", "Integration testing with real Docker containers", "Testing"),

        // Documentation
        new DevTool("springdoc", "SpringDoc OpenAPI", "Auto-generated Swagger/OpenAPI documentation", "Documentation"),

        // Infrastructure
        new DevTool("docker", "Docker", "Dockerfile + docker-compose configuration", "Infrastructure"),

        // CI/CD
        new DevTool("github-actions", "GitHub Actions", "CI workflow for build, test, and deploy", "CI/CD")
    );

    public static DevTool byValue(String value) {
        return ALL.stream()
            .filter(d -> d.value.equals(value))
            .findFirst()
            .orElse(null);
    }
}
