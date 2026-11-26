package io.github.esecoder.jss;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import io.github.esecoder.jss.cli.StarterCommand;

/**
 * Java Spring Starter — Interactive CLI for scaffolding Spring Boot projects.
 * <p>
 * Usage:
 *   java -jar java-spring-starter.jar
 *   java -jar java-spring-starter.jar --non-interactive --template default --name my-app
 */
@Command(
    name = "java-spring-starter",
    mixinStandardHelpOptions = true,
    version = "java-spring-starter 1.0.0",
    description = "Quick and Easy Java Spring Boot Starter",
    subcommands = {}
)
public class Main implements Runnable {

    @CommandLine.Option(names = {"-n", "--non-interactive"},
        description = "Run in non-interactive mode (requires --template and --name)")
    boolean nonInteractive;

    @CommandLine.Option(names = {"-t", "--template"},
        description = "Template to use: default, spring-data-jpa, spring-data-jdbc, jooq, mybatis")
    String template;

    @CommandLine.Option(names = {"--name"},
        description = "Project name (directory name and artifact ID)")
    String projectName;

    @CommandLine.Option(names = {"--build-tool"},
        description = "Build tool: maven, gradle-groovy, gradle-kotlin")
    String buildTool;

    @CommandLine.Option(names = {"--java-version"},
        description = "Java version: 17, 21")
    String javaVersion;

    @CommandLine.Option(names = {"--group-id"},
        description = "Maven group ID / Gradle group")
    String groupId;

    @CommandLine.Option(names = {"--db"},
        description = "Database: h2, postgresql, mysql, mariadb")
    String database;

    @CommandLine.Option(names = {"--devtools"},
        description = "Comma-separated devtools: checkstyle,spotbugs,springdoc,docker,github-actions",
        split = ",")
    java.util.List<String> devtools;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        if (nonInteractive) {
            StarterCommand.runNonInteractive(template, projectName, buildTool, javaVersion,
                groupId, database, devtools);
        } else {
            StarterCommand.runInteractive();
        }
    }
}
