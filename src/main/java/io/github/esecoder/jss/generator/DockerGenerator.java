package io.github.esecoder.jss.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.github.esecoder.jss.config.DatabaseOption;
import io.github.esecoder.jss.config.Template;

/**
 * Generates Docker and docker-compose configuration for the generated project.
 */
public class DockerGenerator {

    private final Template template;
    private final DatabaseOption database;
    private final String projectName;
    private final String javaVersion;
    private final Path targetDir;

    public DockerGenerator(Template template, DatabaseOption database,
                           String projectName, String javaVersion, Path targetDir) {
        this.template = template;
        this.database = database;
        this.projectName = projectName;
        this.javaVersion = javaVersion;
        this.targetDir = targetDir;
    }

    public void generate() throws IOException {
        Path dockerDir = targetDir.resolve("docker");
        Files.createDirectories(dockerDir);

        generateDockerfile(dockerDir);
        generateDockerfileDev(dockerDir);
        generateDockerCompose(dockerDir);
        generateDockerignore(targetDir);
    }

    private void generateDockerfile(Path dockerDir) throws IOException {
        String dockerfile = """
            # Multi-stage build for Java Spring Boot
            FROM eclipse-temurin:%s-jdk AS builder
            WORKDIR /app
            COPY pom.xml ./
            COPY src ./src
            RUN ./mvnw package -DskipTests

            FROM eclipse-temurin:%s-jre
            WORKDIR /app
            COPY --from=builder /app/target/*.jar app.jar
            EXPOSE 8080
            ENTRYPOINT ["java", "-jar", "app.jar"]
            """.formatted(javaVersion(), javaVersion());
        Files.writeString(dockerDir.resolve("Dockerfile"), dockerfile);
    }

    private void generateDockerfileDev(Path dockerDir) throws IOException {
        String dockerfileDev = """
            # Development Dockerfile with hot reload via spring-boot-devtools
            FROM eclipse-temurin:%s-jdk
            WORKDIR /app
            COPY pom.xml ./
            RUN ./mvnw dependency:resolve
            COPY src ./src
            EXPOSE 8080
            ENTRYPOINT ["./mvnw", "spring-boot:run", "-Dspring-boot.run.profiles=dev"]
            """.formatted(javaVersion());
        Files.writeString(dockerDir.resolve("Dockerfile.dev"), dockerfileDev);
    }

    private void generateDockerCompose(Path dockerDir) throws IOException {
        StringBuilder compose = new StringBuilder();
        compose.append("""
            services:
              app:
                build:
                  context: ..
                  dockerfile: docker/Dockerfile.dev
                ports:
                  - "8080:8080"
                environment:
                  - SPRING_PROFILES_ACTIVE=dev
                volumes:
                  - ../src:/app/src
            """);

        if (database != null) {
            switch (database.value()) {
                case "postgresql":
                    compose.append("""
                          - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/%s
                          - SPRING_DATASOURCE_USERNAME=postgres
                          - SPRING_DATASOURCE_PASSWORD=postgres
                        depends_on:
                          - db

                          db:
                            image: postgres:16-alpine
                            environment:
                              - POSTGRES_DB=%s
                              - POSTGRES_USER=postgres
                              - POSTGRES_PASSWORD=postgres
                            ports:
                              - "5432:5432"
                            volumes:
                              - postgres_data:/var/lib/postgresql/data

                        volumes:
                          postgres_data:
                        """.formatted(projectName, projectName));
                    break;
                case "mysql":
                    compose.append("""
                          - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/%s
                          - SPRING_DATASOURCE_USERNAME=root
                          - SPRING_DATASOURCE_PASSWORD=root
                        depends_on:
                          - db

                          db:
                            image: mysql:8.0
                            environment:
                              - MYSQL_DATABASE=%s
                              - MYSQL_ROOT_PASSWORD=root
                            ports:
                              - "3306:3306"
                            volumes:
                              - mysql_data:/var/lib/mysql

                        volumes:
                          mysql_data:
                        """.formatted(projectName, projectName));
                    break;
                case "mongodb":
                    compose.append("""
                          - SPRING_DATA_MONGODB_URI=mongodb://db:27017/%s
                        depends_on:
                          - db

                          db:
                            image: mongo:7
                            ports:
                              - "27017:27017"
                            volumes:
                              - mongo_data:/data/db

                        volumes:
                          mongo_data:
                        """.formatted(projectName));
                    break;
                case "h2":
                default:
                    // H2 is in-memory, no external DB service needed
                    break;
            }
        }

        Files.writeString(dockerDir.resolve("docker-compose.yml"), compose.toString());
    }

    private void generateDockerignore(Path targetDir) throws IOException {
        String dockerignore = """
            # Git
            .git
            .gitignore

            # IDE
            .idea
            *.iml
            .vscode

            # Build
            target/
            build/

            # Docker
            Dockerfile
            docker/
            .dockerignore
            """;
        Files.writeString(targetDir.resolve(".dockerignore"), dockerignore);
    }

    private String javaVersion() {
        return "21".equals(javaVersion) ? "21" : "17";
    }
}
