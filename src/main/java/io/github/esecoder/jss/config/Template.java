package io.github.esecoder.jss.config;

import java.util.List;

/**
 * Template definition with metadata.
 */
public record Template(
    String value,
    String label,
    String description,
    String category,
    String level,       // beginner, intermediate, advanced
    String status       // stable, beta, planned
) {
    /**
     * All available templates. Matches the TypeScript version's template catalog.
     */
    public static final List<Template> ALL = List.of(
        new Template("default",  "Spring Boot + Java",
            "Basic Spring Boot REST API starter · no database",
            "Core", "beginner", "stable"),

        new Template("spring-data-jpa", "Spring Data JPA + Hibernate",
            "JPA ORM with Hibernate · PostgreSQL, MySQL, H2",
            "ORM", "beginner", "stable"),

        new Template("spring-data-jdbc", "Spring Data JDBC",
            "Simple DDD-friendly JDBC persistence · no magic",
            "ORM", "intermediate", "stable"),

        new Template("jooq", "jOOQ",
            "Type-safe SQL query builder · code generation from schema",
            "SQL", "intermediate", "stable"),

        new Template("mybatis", "MyBatis",
            "Flexible SQL mapping framework · full SQL control",
            "SQL", "intermediate", "stable"),

        new Template("spring-data-mongodb", "Spring Data MongoDB",
            "MongoDB document database · ODM mapping",
            "NoSQL", "beginner", "stable")
    );

    public static Template byValue(String value) {
        return ALL.stream()
            .filter(t -> t.value.equals(value))
            .findFirst()
            .orElse(ALL.get(0));
    }

    /**
     * Returns true if this template requires a database selection.
     */
    public boolean requiresDatabase() {
        return !"default".equals(value);
    }
}
