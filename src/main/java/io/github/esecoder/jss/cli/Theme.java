package io.github.esecoder.jss.cli;

/**
 * ANSI color codes and terminal formatting for the CLI.
 */
public final class Theme {

    private Theme() {}

    // Colors
    public static final String RESET  = "\033[0m";
    public static final String BOLD   = "\033[1m";
    public static final String DIM    = "\033[2m";
    public static final String RED    = "\033[31m";
    public static final String GREEN  = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String BLUE   = "\033[34m";
    public static final String MAGENTA= "\033[35m";
    public static final String CYAN   = "\033[36m";
    public static final String WHITE  = "\033[37m";
    public static final String GRAY   = "\033[90m";

    // Background
    public static final String BG_BLUE  = "\033[44m";
    public static final String BG_GREEN = "\033[42m";

    // Symbols
    public static final String CHECK   = "✔";
    public static final String CROSS   = "✘";
    public static final String ARROW   = "→";
    public static final String DIAMOND = "◆";
    public static final String BULLET  = "●";
    public static final String CIRCLE  = "○";
    public static final String DOT     = "·";

    public static String header(String text) {
        return BOLD + CYAN + "\n┌  ☕ " + text + RESET;
    }

    public static String prompt(String text) {
        return "\n◇  " + text;
    }

    public static String selected(String text) {
        return GREEN + BULLET + " " + text + RESET;
    }

    public static String unselected(String text) {
        return GRAY + CIRCLE + " " + text + RESET;
    }

    public static String info(String text) {
        return DIM + "│  " + text + RESET;
    }

    public static String success(String text) {
        return GREEN + CHECK + " " + text + RESET;
    }

    public static String error(String text) {
        return RED + CROSS + " " + text + RESET;
    }

    public static String warning(String text) {
        return YELLOW + "! " + text + RESET;
    }

    public static String boxHeader(String text) {
        return DIM + "◇  " + text + " ─".repeat(Math.max(0, 48 - text.length())) + "╮" + RESET;
    }

    public static String boxLine(String text) {
        return DIM + "│  " + text + RESET;
    }

    public static String boxFooter() {
        return DIM + "├" + "─".repeat(50) + "╯" + RESET;
    }

    public static String hr() {
        return DIM + "─".repeat(54) + RESET;
    }
}
