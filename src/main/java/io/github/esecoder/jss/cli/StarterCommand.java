package io.github.esecoder.jss.cli;

import java.nio.file.Path;
import java.util.List;

import io.github.esecoder.jss.config.DatabaseOption;
import io.github.esecoder.jss.config.DevTool;
import io.github.esecoder.jss.config.Presets;
import io.github.esecoder.jss.config.Template;
import io.github.esecoder.jss.generator.ProjectGenerator;

import static io.github.esecoder.jss.cli.Theme.*;

/**
 * Orchestrates the interactive and non-interactive project generation flows.
 */
public final class StarterCommand {

    private StarterCommand() {}

    /**
     * Interactive mode: prompt the user through every choice, then generate.
     */
    public static void runInteractive() {
        Prompts prompts = new Prompts();

        // Step 1: Quick or Custom?
        boolean quick = prompts.chooseQuickMode();

        // Step 2: Build tool
        String buildTool = prompts.chooseBuildTool();

        // Step 3: Template
        Template template = prompts.chooseTemplate();

        // Step 4: Project name
        String projectName = prompts.chooseProjectName();

        // Step 5: Java version
        String javaVersion = prompts.chooseJavaVersion();

        // Step 6: Database (if template requires it)
        DatabaseOption database = null;
        if (template.requiresDatabase()) {
            database = prompts.chooseDatabase();
        }

        // Step 7: Devtools
        List<DevTool> devtools;
        if (quick) {
            Presets.Profile profile = prompts.chooseQuickProfile();
            devtools = profile.getTools().stream()
                .map(DevTool::byValue)
                .filter(t -> t != null)
                .collect(java.util.stream.Collectors.toList());
        } else {
            devtools = prompts.chooseDevTools();
        }

        // Step 8: Show summary & confirm
        String modeLabel = quick ? "quick" : "custom";
        prompts.showSummary(modeLabel, buildTool, template, projectName, javaVersion,
            database, devtools);

        if (!prompts.confirmGeneration()) {
            System.out.println(success("Generation cancelled. Goodbye!"));
            return;
        }

        // Step 9: Generate!
        generate(buildTool, template, projectName, javaVersion, database, devtools);
    }

    /**
     * Non-interactive mode: use CLI arguments to generate directly.
     */
    public static void runNonInteractive(String templateValue, String projectName,
                                          String buildTool, String javaVersion,
                                          String groupId, String dbValue,
                                          List<String> devtoolValues) {
        if (templateValue == null || projectName == null) {
            System.err.println(error("Non-interactive mode requires --template and --name."));
            System.err.println("Example: java -jar java-spring-starter.jar -n -t default --name my-app");
            System.exit(1);
        }

        Template template = Template.byValue(templateValue);
        String bt = buildTool != null ? buildTool : "maven";
        String jv = javaVersion != null ? javaVersion : "21";
        DatabaseOption db = dbValue != null ? DatabaseOption.byValue(dbValue) : null;
        List<DevTool> devtools = devtoolValues != null
            ? devtoolValues.stream().map(DevTool::byValue).filter(d -> d != null).toList()
            : List.of();

        System.out.println(info("Generating project in non-interactive mode..."));
        generate(bt, template, projectName, jv, db, devtools);
    }

    private static void generate(String buildTool, Template template, String projectName,
                                  String javaVersion, DatabaseOption database,
                                  List<DevTool> devtools) {
        Path targetDir = Path.of(projectName).toAbsolutePath();

        System.out.println(info("Creating project: " + projectName));
        System.out.println(info("Template: " + template.label()));
        System.out.println(info("Build tool: " + buildTool));
        System.out.println(info("Java version: " + javaVersion));
        if (database != null) {
            System.out.println(info("Database: " + database.label()));
        }
        if (!devtools.isEmpty()) {
            String tools = devtools.stream().map(DevTool::label).reduce((a, b) -> a + ", " + b).orElse("");
            System.out.println(info("Devtools: " + tools));
        }

        try {
            ProjectGenerator generator = new ProjectGenerator(
                buildTool, template, projectName, javaVersion, database, devtools, targetDir);
            generator.generate();

            System.out.println(success("Project generated at " + targetDir));
            System.out.println();
            System.out.println("  " + BOLD + "Next steps:" + RESET);
            System.out.println("  cd " + projectName);
            if ("maven".equals(buildTool)) {
                System.out.println("  ./mvnw spring-boot:run");
            } else {
                System.out.println("  ./gradlew bootRun");
            }
            System.out.println();
            System.out.println(GREEN + "☕ Happy coding! 🚀" + RESET);
        } catch (Exception e) {
            System.err.println(error("Failed to generate project: " + e.getMessage()));
            e.printStackTrace();
            System.exit(1);
        }
    }
}
