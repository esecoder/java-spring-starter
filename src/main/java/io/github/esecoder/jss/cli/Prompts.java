package io.github.esecoder.jss.cli;

import java.io.Console;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.esecoder.jss.config.DatabaseOption;
import io.github.esecoder.jss.config.DevTool;
import io.github.esecoder.jss.config.Presets;
import io.github.esecoder.jss.config.Template;

import static io.github.esecoder.jss.cli.Theme.*;

/**
 * All interactive prompts, matching the @clack/prompts flow in the TS version.
 * Uses System.console() for portable interactive terminal I/O.
 */
public class Prompts {

    private final Console console;
    private final boolean useColor;

    public Prompts() {
        this.console = System.console();
        this.useColor = this.console != null;
    }

    // ─────────────────────────────────────────────
    // Low-level input helpers
    // ─────────────────────────────────────────────

    private void println(String s) {
        if (console != null) {
            console.printf("%s%n", useColor ? s : stripColor(s));
        }
    }

    private String readLine() {
        if (console != null) {
            return console.readLine().trim();
        }
        // fallback: should rarely happen
        try {
            return new java.util.Scanner(System.in).nextLine().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String stripColor(String s) {
        return s.replaceAll("\033\\[[;\\d]*m", "");
    }

    // ─────────────────────────────────────────────
    // Select: choose one option from a list
    // ─────────────────────────────────────────────

    /**
     * Present a numbered list. User types a number to select.
     * Returns the index (0-based).
     */
    private int select(String prompt, List<String> labels, List<String> hints) {
        println(prompt(prompt));
        for (int i = 0; i < labels.size(); i++) {
            String suffix = (i < hints.size() && hints.get(i) != null && !hints.get(i).isBlank())
                ? GRAY + " · " + hints.get(i) + RESET
                : "";
            println(String.format("  %s%-2d%s %s%s",
                DIM, i + 1, RESET, labels.get(i), suffix));
        }
        while (true) {
            print(ARROW + " Enter number (1-" + labels.size() + "): ");
            String input = readLine();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= labels.size()) {
                    return choice - 1;
                }
            } catch (NumberFormatException ignored) {}
            println(error("Please enter a number between 1 and " + labels.size()));
        }
    }

    private int select(String prompt, List<String> labels) {
        return select(prompt, labels, List.of());
    }

    // ─────────────────────────────────────────────
    // Multi-select: choose multiple options
    // ─────────────────────────────────────────────

    private List<Integer> multiSelect(String prompt, List<String> labels, List<String> hints) {
        println(prompt(prompt));
        for (int i = 0; i < labels.size(); i++) {
            String suffix = (i < hints.size() && hints.get(i) != null && !hints.get(i).isBlank())
                ? GRAY + " · " + hints.get(i) + RESET
                : "";
            println(String.format("  %s%-2d%s %s%s",
                DIM, i + 1, RESET, labels.get(i), suffix));
        }
        println(DIM + "  Enter numbers separated by commas, or 'all', or press enter for none" + RESET);
        while (true) {
            print(ARROW + " Selection: ");
            String input = readLine();
            if (input.isBlank()) return List.of();
            if ("all".equalsIgnoreCase(input.trim())) {
                List<Integer> all = new ArrayList<>();
                for (int i = 0; i < labels.size(); i++) all.add(i);
                return all;
            }
            try {
                return java.util.Arrays.stream(input.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .map(n -> n - 1)
                    .filter(n -> n >= 0 && n < labels.size())
                    .distinct()
                    .collect(Collectors.toList());
            } catch (NumberFormatException ignored) {}
            println(error("Please enter numbers like 1,3,5 or 'all'"));
        }
    }

    // ─────────────────────────────────────────────
    // Confirm: yes/no
    // ─────────────────────────────────────────────

    private boolean confirm(String prompt) {
        println(DIAMOND + " " + prompt);
        println("  " + selected("Yes") + " / " + unselected("No"));
        print(ARROW + " (Y/n): ");
        String input = readLine().toLowerCase();
        return input.isEmpty() || input.startsWith("y");
    }

    // ─────────────────────────────────────────────
    // Text input
    // ─────────────────────────────────────────────

    private String textInput(String prompt, String defaultValue) {
        String display = defaultValue != null && !defaultValue.isEmpty()
            ? prompt(prompt) + " " + DIM + "[" + defaultValue + "]" + RESET + ": "
            : prompt(prompt) + ": ";
        print(display);
        String input = readLine();
        return input.isEmpty() ? defaultValue : input;
    }

    private void print(String s) {
        if (console != null) {
            console.printf("%s", useColor ? s : stripColor(s));
        }
    }

    // ─────────────────────────────────────────────
    // High-level flow prompts
    // ─────────────────────────────────────────────

    /**
     * Prompt: choose setup mode (quick vs custom).
     */
    public boolean chooseQuickMode() {
        println(header("Java Spring Starter"));
        List<String> labels = List.of(
            "🚀 Quick start (recommended preset)",
            "🛠  Custom (step-by-step)");
        int choice = select("Choose setup mode:", labels,
            List.of("Automatically selects a sensible toolset",
                "Pick tools category by category"));
        return choice == 0;
    }

    /**
     * Prompt: choose build tool.
     */
    public String chooseBuildTool() {
        List<String> labels = List.of(
            "Maven (Recommended)",
            "Gradle (Groovy DSL)",
            "Gradle (Kotlin DSL)");
        int choice = select("Which build tool do you want to use?", labels,
            List.of("Most widely used · easier Maven Central publishing",
                "Flexible Groovy-based build scripts",
                "Type-safe Kotlin-based build scripts"));
        return switch (choice) {
            case 1 -> "gradle-groovy";
            case 2 -> "gradle-kotlin";
            default -> "maven";
        };
    }

    /**
     * Prompt: choose a template.
     */
    public Template chooseTemplate() {
        List<String> labels = Template.ALL.stream()
            .map(t -> t.label())
            .collect(Collectors.toList());
        List<String> hints = Template.ALL.stream()
            .map(t -> t.description() + " · " + t.level() + " · " + t.status())
            .collect(Collectors.toList());
        int choice = select("Choose a template:", labels, hints);
        return Template.ALL.get(choice);
    }

    /**
     * Prompt: enter project name.
     */
    public String chooseProjectName() {
        while (true) {
            String name = textInput("Enter your project name", "my-spring-app");
            String error = Validators.validateProjectName(name);
            if (error == null) {
                // Check path
                String pathError = Validators.validateProjectPath(Path.of(name));
                if (pathError == null) {
                    return name;
                }
                println(error(pathError));
            } else {
                println(error(error));
            }
        }
    }

    /**
     * Prompt: choose Java version.
     */
    public String chooseJavaVersion() {
        List<String> labels = List.of("Java 21 (LTS · Recommended)", "Java 17 (LTS)");
        int choice = select("Choose Java version:", labels);
        return choice == 1 ? "17" : "21";
    }

    /**
     * Prompt: choose database (for templates that need one).
     */
    public DatabaseOption chooseDatabase() {
        List<String> labels = DatabaseOption.ALL.stream()
            .map(d -> d.label())
            .collect(Collectors.toList());
        List<String> hints = DatabaseOption.ALL.stream()
            .map(DatabaseOption::description)
            .collect(Collectors.toList());
        int choice = select("Select database:", labels, hints);
        return DatabaseOption.ALL.get(choice);
    }

    /**
     * Prompt: choose quick profile (for quick mode).
     */
    public Presets.Profile chooseQuickProfile() {
        List<String> labels = List.of(
            Presets.Profile.MINIMAL.label + "  " + Presets.Profile.MINIMAL.description,
            Presets.Profile.RECOMMENDED.label + "  " + Presets.Profile.RECOMMENDED.description,
            Presets.Profile.FULL.label + "  " + Presets.Profile.FULL.description);
        int choice = select("Choose quick profile:", labels);
        return Presets.Profile.values()[choice];
    }

    /**
     * Prompt: choose devtools (for custom mode, by category).
     */
    public List<DevTool> chooseDevTools() {
        // Group devtools by category
        Map<String, List<DevTool>> grouped = new LinkedHashMap<>();
        for (DevTool tool : DevTool.ALL) {
            grouped.computeIfAbsent(tool.category(), k -> new ArrayList<>()).add(tool);
        }

        List<DevTool> selected = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            println(hr());
            println(BOLD + "  " + entry.getKey() + RESET);

            List<DevTool> categoryTools = entry.getValue();
            List<String> labels = categoryTools.stream()
                .map(DevTool::label)
                .collect(Collectors.toList());
            List<String> hints = categoryTools.stream()
                .map(DevTool::description)
                .collect(Collectors.toList());

            // Add "None" option
            labels = new ArrayList<>(labels);
            hints = new ArrayList<>(hints);
            labels.add(0, "None (skip)");
            hints.add(0, "Don't include any " + entry.getKey().toLowerCase() + " tool");

            List<Integer> chosen = multiSelect(
                "Select " + entry.getKey().toLowerCase() + " tools:",
                labels, hints);

            for (int idx : chosen) {
                // idx 0 is "None" — skip
                if (idx > 0 && idx <= categoryTools.size()) {
                    selected.add(categoryTools.get(idx - 1));
                }
            }
        }
        return selected;
    }

    /**
     * Prompt: confirm project generation.
     */
    public boolean confirmGeneration() {
        return confirm("Proceed with project generation?");
    }

    /**
     * Display the configuration summary.
     */
    public void showSummary(String mode, String buildTool, Template template,
                            String projectName, String javaVersion,
                            DatabaseOption database, List<DevTool> devtools) {
        println(prompt("Configuration summary ────────────────────────╮"));
        println(boxLine(""));
        println(boxLine("Setup mode     : " + BOLD + mode + RESET));
        println(boxLine("Build tool     : " + BOLD + buildTool + RESET));
        println(boxLine("Template       : " + BOLD + template.label() + RESET));
        println(boxLine("Project name   : " + BOLD + projectName + RESET));
        println(boxLine("Java version   : " + BOLD + javaVersion + RESET));
        if (database != null) {
            println(boxLine("Database       : " + BOLD + database.label() + RESET));
        }
        if (devtools.isEmpty()) {
            println(boxLine("Devtools       : " + GRAY + "none" + RESET));
        } else {
            String tools = devtools.stream().map(DevTool::label).collect(Collectors.joining(", "));
            println(boxLine("Devtools       : " + BOLD + tools + RESET));
        }
        println(boxLine(""));
        println(boxFooter());
    }
}
