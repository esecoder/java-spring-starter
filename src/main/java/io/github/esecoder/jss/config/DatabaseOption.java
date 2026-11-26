package io.github.esecoder.jss.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Database options available per template.
 */
public record DatabaseOption(
    String value,
    String label,
    String description
) {
    public static final List<DatabaseOption> ALL = List.of(
        new DatabaseOption("h2", "H2", "In-memory database · great for development & testing"),
        new DatabaseOption("postgresql", "PostgreSQL", "Advanced open-source relational database"),
        new DatabaseOption("mysql", "MySQL", "Popular open-source relational database"),
        new DatabaseOption("mariadb", "MariaDB", "Community-developed MySQL fork"),
        new DatabaseOption("mongodb", "MongoDB", "Document-oriented NoSQL database")
    );

    public static DatabaseOption byValue(String value) {
        return ALL.stream()
            .filter(d -> d.value.equals(value))
            .findFirst()
            .orElse(ALL.get(0));
    }
}
