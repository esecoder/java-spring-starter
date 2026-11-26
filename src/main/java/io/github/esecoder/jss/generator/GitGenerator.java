package io.github.esecoder.jss.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates .gitignore and optionally initializes git for the generated project.
 */
public class GitGenerator {

    private final Path targetDir;

    public GitGenerator(Path targetDir) {
        this.targetDir = targetDir;
    }

    public void generate() throws IOException {
        String gitignore = """
            # ── Java ──
            *.class
            *.jar
            *.war
            target/
            build/
            !.mvn/wrapper/maven-wrapper.jar
            !**/src/main/**/target/
            !**/src/test/**/target/

            # ── IDE ──
            .idea/
            *.iml
            *.iws
            *.ipr
            .vscode/
            .settings/
            .project
            .classpath
            *.swp
            *.swo
            *~

            # ── OS ──
            .DS_Store
            Thumbs.db

            # ── Environment ──
            .env
            .env.local
            .env.*.local

            # ── Logs ──
            logs/
            *.log

            # ── Docker ──
            .dockerignore
            """;

        Files.writeString(targetDir.resolve(".gitignore"), gitignore);
    }
}
