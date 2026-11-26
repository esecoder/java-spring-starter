package io.github.esecoder.jss.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.esecoder.jss.cli.Theme;
import io.github.esecoder.jss.cli.Validators;
import io.github.esecoder.jss.config.DatabaseOption;
import io.github.esecoder.jss.config.DevTool;
import io.github.esecoder.jss.config.Template;

/**
 * Orchestrates the full project generation: copies template files, writes
 * build configuration, applies devtools, and initializes git.
 */
public class ProjectGenerator {

    private final String buildTool;
    private final Template template;
    private final String projectName;
    private final String javaVersion;
    private final DatabaseOption database;
    private final List<DevTool> devtools;
    private final Path targetDir;

    // Derived values
    private final String packageName;
    private final String className;
    private final String groupId;

    public ProjectGenerator(String buildTool, Template template, String projectName,
                            String javaVersion, DatabaseOption database,
                            List<DevTool> devtools, Path targetDir) {
        this.buildTool = buildTool;
        this.template = template;
        this.projectName = projectName;
        this.javaVersion = javaVersion;
        this.database = database;
        this.devtools = devtools;
        this.targetDir = targetDir;

        // Derive from project name
        this.packageName = "com.example." + Validators.toPackageName(projectName);
        this.className = Validators.toClassName(projectName) + "Application";
        this.groupId = "com.example";
    }

    /**
     * Run the full generation pipeline.
     */
    public void generate() throws IOException {
        // 1. Create base directory
        Files.createDirectories(targetDir);

        // 2. Generate build file (pom.xml or build.gradle)
        new BuildFileWriter(buildTool, projectName, groupId, javaVersion,
            template, database, devtools, targetDir).write();

        // 3. Copy template source files
        copyTemplateSource();

        // 4. Copy devtool configs
        copyDevToolConfigs();

        // 5. Generate Docker files (if docker devtool is selected)
        if (hasDevTool("docker")) {
            new DockerGenerator(template, database, projectName, javaVersion, targetDir).generate();
        }

        // 6. Generate .gitignore
        new GitGenerator(targetDir).generate();

        // 7. Generate wrapper scripts (Maven wrapper or Gradle wrapper)
        generateWrapper();

        // 8. Generate README
        generateReadme();

        // 9. Create dev/test resource configs
        generateApplicationConfigs();
    }

    private boolean hasDevTool(String value) {
        return devtools.stream().anyMatch(d -> d.value().equals(value));
    }

    /**
     * Copy the template source tree from resources/templates/<template>/ to targetDir.
     * Applies variable substitutions for package name, class name, etc.
     */
    private void copyTemplateSource() throws IOException {
        String templateResourcePath = "templates/default/";

        Map<String, String> vars = new LinkedHashMap<>();
        vars.put("PACKAGE_NAME", packageName);
        vars.put("CLASS_NAME", className);
        vars.put("PROJECT_NAME", projectName);
        vars.put("GROUP_ID", groupId);
        vars.put("JAVA_VERSION", javaVersion);
        vars.put("SPRING_BOOT_VERSION", "3.3.0");
        if (database != null) {
            vars.put("DB_TYPE", database.value());
            vars.put("DB_NAME", database.label());
        }

        // We'll copy from the filesystem (dev mode). For fat JAR mode, we'd need
        // to enumerate resources differently.
        try {
            io.github.esecoder.jss.util.FileUtils.copyResourceDir(templateResourcePath, targetDir);
        } catch (IOException e) {
            // If running from JAR, fall back to generating files programmatically
            System.err.println(Theme.warning(
                "Could not copy template from resources (may be running from JAR). " +
                "Generating files programmatically..."));
            generateTemplateProgrammatically(vars);
        }
    }

    /**
     * Programmatic fallback: generate the template files directly.
     */
    private void generateTemplateProgrammatically(Map<String, String> vars) throws IOException {
        String pkgPath = packageName.replace('.', '/');
        Path srcMain = targetDir.resolve("src/main/java").resolve(pkgPath);
        Path srcTest = targetDir.resolve("src/test/java").resolve(pkgPath);
        Path resources = targetDir.resolve("src/main/resources");
        Files.createDirectories(srcMain);
        Files.createDirectories(srcTest);
        Files.createDirectories(resources);

        // Package subdirectories
        Path configDir = srcMain.resolve("config");
        Path controllerDir = srcMain.resolve("controller");
        Path dtoDir = srcMain.resolve("dto");
        Path entityDir = srcMain.resolve("entity");
        Path exceptionDir = srcMain.resolve("exception");
        Path repositoryDir = srcMain.resolve("repository");
        Path serviceDir = srcMain.resolve("service");
        Path utilDir = srcMain.resolve("util");
        Files.createDirectories(configDir);
        Files.createDirectories(controllerDir);
        Files.createDirectories(dtoDir);
        Files.createDirectories(entityDir);
        Files.createDirectories(exceptionDir);
        Files.createDirectories(repositoryDir);
        Files.createDirectories(serviceDir);
        Files.createDirectories(utilDir);

        // --- Write all source files ---

        // Application.java (main entry point)
        writeFile(srcMain.resolve(className + ".java"),
            "package " + packageName + ";\n\n" +
            "import org.springframework.boot.SpringApplication;\n" +
            "import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n" +
            "@SpringBootApplication\n" +
            "public class " + className + " {\n" +
            "    public static void main(String[] args) {\n" +
            "        SpringApplication.run(" + className + ".class, args);\n" +
            "    }\n" +
            "}\n");

        // HealthController
        writeFile(controllerDir.resolve("HealthController.java"),
            "package " + packageName + ".controller;\n\n" +
            "import org.springframework.web.bind.annotation.GetMapping;\n" +
            "import org.springframework.web.bind.annotation.RestController;\n\n" +
            "import java.time.Instant;\n" +
            "import java.util.Map;\n\n" +
            "@RestController\n" +
            "public class HealthController {\n\n" +
            "    @GetMapping(\"/\")\n" +
            "    public Map<String, Object> health() {\n" +
            "        return Map.of(\n" +
            "            \"status\", \"UP\",\n" +
            "            \"service\", \"" + projectName + "\",\n" +
            "            \"timestamp\", Instant.now().toString()\n" +
            "        );\n" +
            "    }\n\n" +
            "    @GetMapping(\"/health\")\n" +
            "    public Map<String, String> healthCheck() {\n" +
            "        return Map.of(\"status\", \"UP\");\n" +
            "    }\n" +
            "}\n");

        // GlobalExceptionHandler
        writeFile(exceptionDir.resolve("GlobalExceptionHandler.java"),
            "package " + packageName + ".exception;\n\n" +
            "import org.springframework.http.HttpStatus;\n" +
            "import org.springframework.http.ProblemDetail;\n" +
            "import org.springframework.web.bind.annotation.ExceptionHandler;\n" +
            "import org.springframework.web.bind.annotation.RestControllerAdvice;\n\n" +
            "import java.net.URI;\n" +
            "import java.time.Instant;\n\n" +
            "@RestControllerAdvice\n" +
            "public class GlobalExceptionHandler {\n\n" +
            "    @ExceptionHandler(Exception.class)\n" +
            "    public ProblemDetail handleException(Exception ex) {\n" +
            "        ProblemDetail problem = ProblemDetail.forStatusAndDetail(\n" +
            "            HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());\n" +
            "        problem.setTitle(\"Internal Server Error\");\n" +
            "        problem.setProperty(\"timestamp\", Instant.now().toString());\n" +
            "        return problem;\n" +
            "    }\n\n" +
            "    @ExceptionHandler(IllegalArgumentException.class)\n" +
            "    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {\n" +
            "        ProblemDetail problem = ProblemDetail.forStatusAndDetail(\n" +
            "            HttpStatus.BAD_REQUEST, ex.getMessage());\n" +
            "        problem.setTitle(\"Bad Request\");\n" +
            "        problem.setProperty(\"timestamp\", Instant.now().toString());\n" +
            "        return problem;\n" +
            "    }\n" +
            "}\n");

        // Application tests
        writeFile(srcTest.resolve(className + "Tests.java"),
            "package " + packageName + ";\n\n" +
            "import org.junit.jupiter.api.Test;\n" +
            "import org.springframework.boot.test.context.SpringBootTest;\n\n" +
            "@SpringBootTest\n" +
            "class " + className + "Tests {\n\n" +
            "    @Test\n" +
            "    void contextLoads() {\n" +
            "    }\n\n" +
            "}\n");

        // application.yml
        writeFile(resources.resolve("application.yml"),
            "server:\n" +
            "  port: 8080\n\n" +
            "spring:\n" +
            "  application:\n" +
            "    name: " + projectName + "\n\n" +
            "# Logging\n" +
            "logging:\n" +
            "  level:\n" +
            "    root: INFO\n" +
            "    " + packageName + ": DEBUG\n");

        // application-dev.yml
        writeFile(resources.resolve("application-dev.yml"),
            "spring:\n" +
            "  devtools:\n" +
            "    restart:\n" +
            "      enabled: true\n" +
            "logging:\n" +
            "  level:\n" +
            "    " + packageName + ": DEBUG\n");
    }

    private void copyDevToolConfigs() throws IOException {
        for (DevTool tool : devtools) {
            switch (tool.value()) {
                case "checkstyle":
                    copyDevToolResource("lint/checkstyle.xml",
                        targetDir.resolve("checkstyle.xml"));
                    break;
                case "springdoc":
                    // SpringDoc config is added via application properties in BuildFileWriter
                    break;
                case "github-actions":
                    copyDevToolResource("ci/github-actions.yml",
                        targetDir.resolve(".github/workflows/ci.yml"));
                    break;
                case "spotless":
                    // Spotless config is in the build file
                    break;
                case "spotbugs":
                    copyDevToolResource("lint/spotbugs-exclude.xml",
                        targetDir.resolve("spotbugs-exclude.xml"));
                    break;
                case "testcontainers":
                    // No extra files needed; dependencies added in build file
                    break;
            }
        }
    }

    private void copyDevToolResource(String resourcePath, Path targetPath) throws IOException {
        try {
            io.github.esecoder.jss.util.FileUtils.copyResourceFile(
                resourcePath, targetPath, Map.of());
        } catch (IOException e) {
            // DevTool resource not yet available — skip for MVP
            System.err.println(Theme.warning("Skipping devtool resource: " + resourcePath
                + " (" + e.getMessage() + ")"));
        }
    }

    private void generateWrapper() {
        // Maven wrapper is handled separately — users can run
        // "mvn wrapper:wrapper" or we provide pre-generated scripts.
        // For now, we note it in the README.
    }

    private void generateReadme() throws IOException {
        String readme = """
            # %s
            
            Generated with ☕ [Java Spring Starter](https://github.com/esecoder/java-spring-starter)
            
            ## Stack
            
            - **Java** %s
            - **Spring Boot** 3.3.x
            - **Build Tool** %s
            %s
            
            ## Quick Start
            
            ```bash
            %s
            ```
            
            Then open [http://localhost:8080](http://localhost:8080)
            
            ## API Endpoints
            
            | Method | Path       | Description    |
            |--------|------------|----------------|
            | GET    | /          | Service info   |
            | GET    | /health    | Health check   |
            
            ## Project Structure
            
            ```
            src/
            ├── main/java/%s/
            │   ├── config/          Configuration classes
            │   ├── controller/      REST controllers
            │   ├── dto/             Data Transfer Objects
            │   ├── entity/          Domain entities
            │   ├── exception/       Global exception handling
            │   ├── repository/      Data access layer
            │   ├── service/         Business logic
            │   └── util/            Utility classes
            └── main/resources/
                └── application.yml  Configuration
            ```
            """.formatted(
                projectName,
                javaVersion,
                buildTool,
                database != null ? "- **Database** " + database.label() + "\n" : "",
                "maven".equals(buildTool)
                    ? "./mvnw spring-boot:run"
                    : "./gradlew bootRun",
                packageName.replace('.', '/')
            );
        Files.writeString(targetDir.resolve("README.md"), readme);
    }

    private void generateApplicationConfigs() throws IOException {
        Path resources = targetDir.resolve("src/main/resources");
        Files.createDirectories(resources);

        // Only create if they don't exist (template may have already created them)
        if (!Files.exists(resources.resolve("application-prod.yml"))) {
            Files.writeString(resources.resolve("application-prod.yml"),
                "logging:\n" +
                "  level:\n" +
                "    root: WARN\n" +
                "    " + packageName + ": INFO\n");
        }

        if (!Files.exists(resources.resolve("application-test.yml"))) {
            Files.writeString(resources.resolve("application-test.yml"),
                "spring:\n" +
                "  datasource:\n" +
                "    url: jdbc:h2:mem:testdb\n" +
                "    driver-class-name: org.h2.Driver\n" +
                "  jpa:\n" +
                "    hibernate:\n" +
                "      ddl-auto: create-drop\n");
        }
    }

    // ── helpers ──

    private void writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
