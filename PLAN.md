# Java Spring Starter — Project Plan

> A Java port of [typescript-express-starter](https://github.com/ljlm0402/typescript-express-starter): an interactive CLI tool that scaffolds production-ready Spring Boot projects with the developer's preferred stack — database, ORM, testing, linting, Docker, and more.

---

## 1. High-Level Mapping: TypeScript → Java Ecosystem

| TypeScript Express Starter | Java Spring Starter |
|---|---|
| **Runtime** Node.js | **Runtime** JDK 17+ (LTS) |
| **Framework** Express.js | **Framework** Spring Boot 3.x |
| **Language** TypeScript (typed JS) | **Language** Java (statically typed) |
| **Package manager** npm / pnpm / yarn | **Build tool** Maven (primary) / Gradle (secondary) |
| **Publish to** npm registry | **Publish to** Maven Central (via Sonatype OSSRH) |
| **Global install** `npm install -g` | **Install** SDKMAN! / Homebrew / Maven Plugin |
| **One-shot runner** `npx` | **One-shot runner** JBang or Maven wrapper (`mvnw`) |
| **CLI prompts** @clack/prompts + inquirer | **CLI prompts** PicoCLI + JLine (or Spring Shell) |
| **Templates** files in `templates/` dir | **Templates** files in `templates/` dir (same pattern) |
| **Devtools config** in `devtools/` dir | **Devtools config** in `devtools/` dir (same pattern) |

---

## 2. Repository / Project Name

- **GitHub:** `ljlm0402/java-spring-starter` (mirroring upstream)  
- **Maven Central artifact:** `io.github.ljlm0402:java-spring-starter`  
- **CLI command:** `java-spring-starter` (or `jss` shortcut)

---

## 3. Feature Set (Phase 1 — MVP)

### 3.1 Interactive CLI

- **Library:** PicoCLI + JLine for interactive prompts (or Spring Shell if we want a richer experience). PicoCLI is lighter, has great terminal UX, and produces a single fat JAR.
- Two modes (as in the TS version):
  - **Quick start** — picks sensible defaults automatically
  - **Custom** — step-by-step per category

### 3.2 Build-Tool Selection

| Option | Description |
|---|---|
| **Maven** (Recommended) | Most widely used; easier Maven Central publishing |
| **Gradle (Groovy DSL)** | Popular alternative |
| **Gradle (Kotlin DSL)** | Modern Gradle variant |

### 3.3 Template Selection (Phase 1)

| Template | Java Equivalent | Status |
|---|---|---|
| `default` | **Spring Boot + Java** (Spring Web, no DB) | ✅ Phase 1 |
| `spring-data-jpa` | **Spring Data JPA + Hibernate** (like Sequelize) | ✅ Phase 1 |
| `spring-data-jdbc` | **Spring Data JDBC** (simpler, DDD-friendly) | ✅ Phase 1 |
| `jooq` | **jOOQ** (type-safe SQL, like Drizzle/Knex) | ✅ Phase 1 |
| `mybatis` | **MyBatis** (SQL mapping, like raw SQL) | ✅ Phase 1 |

### 3.4 Database Options (per template)

Where applicable:
- **H2** (in-memory, dev default)
- **PostgreSQL**
- **MySQL**
- **MariaDB**
- **MongoDB** (Spring Data MongoDB — separate template)

### 3.5 Development Tools

| Category | Tools |
|---|---|
| **Linting / Static Analysis** | Checkstyle, SpotBugs, SonarLint config |
| **Code Formatting** | Spotless (like Prettier), EditorConfig |
| **Testing** | JUnit 5 + Mockito + AssertJ |
| **API Docs** | SpringDoc OpenAPI (like Swagger) |
| **Container** | Docker + docker-compose (same as TS) |
| **CI/CD** | GitHub Actions workflow |

### 3.6 Project Structure (Generated)

```
my-project/
├── src/
│   ├── main/
│   │   ├── java/com/example/myproject/
│   │   │   ├── config/          # @Configuration classes
│   │   │   ├── controller/      # @RestController classes
│   │   │   ├── dto/             # DTOs / request-response objects
│   │   │   ├── entity/          # @Entity / domain objects
│   │   │   ├── exception/       # @ControllerAdvice + custom exceptions
│   │   │   ├── repository/      # @Repository interfaces
│   │   │   ├── service/         # @Service business logic
│   │   │   ├── middleware/      # Filters & interceptors (auth, logging, error)
│   │   │   ├── util/            # Utility classes
│   │   │   └── Application.java # Main entry point
│   │   └── resources/
│   │       ├── application.yml           # Default config
│   │       ├── application-dev.yml       # Dev profile
│   │       ├── application-prod.yml      # Prod profile
│   │       └── application-test.yml      # Test profile
│   └── test/
│       └── java/com/example/myproject/
│           ├── controller/     # Controller tests
│           ├── service/        # Service tests
│           └── repository/     # Repository tests
├── docker/
│   ├── Dockerfile
│   ├── Dockerfile.dev
│   └── docker-compose.yml
├── .editorconfig
├── .gitignore
├── pom.xml (or build.gradle)
├── README.md
└── mvnw / mvnw.cmd (Maven wrapper)
```

---

## 4. Technical Architecture of the CLI Itself

```
java-spring-starter/
├── pom.xml                         # CLI's own build
├── src/main/java/io/github/ljlm0402/jss/
│   ├── Main.java                   # Entry point (PicoCLI @Command)
│   ├── cli/
│   │   ├── StarterCommand.java     # Main interactive flow
│   │   ├── Prompts.java            # All interactive prompts
│   │   ├── Theme.java              # Colors, formatting
│   │   └── Validators.java         # Project name, path validation
│   ├── config/
│   │   ├── Templates.java          # Template definitions & metadata
│   │   ├── DevTools.java           # Devtools definitions & categories
│   │   ├── Dependencies.java       # Maven/Gradle dependency maps
│   │   └── Presets.java            # Quick-start preset definitions
│   ├── generator/
│   │   ├── ProjectGenerator.java   # Orchestrates file creation
│   │   ├── TemplateCopier.java     # Copies template files → target dir
│   │   ├── BuildFileWriter.java    # Generates pom.xml or build.gradle
│   │   ├── DockerGenerator.java    # Generates Docker config
│   │   └── GitGenerator.java       # .gitignore, git init
│   └── util/
│       └── FileUtils.java          # File/path helpers
├── templates/                       # Template source files
│   ├── default/
│   ├── spring-data-jpa/
│   ├── spring-data-jdbc/
│   ├── jooq/
│   ├── mybatis/
│   └── spring-data-mongodb/
├── devtools/                        # Devtool config fragments
│   ├── lint/
│   │   ├── checkstyle.xml
│   │   └── spotbugs.xml
│   ├── format/
│   │   └── spotless-pom.xml
│   ├── test/
│   │   └── junit.properties
│   ├── docs/
│   │   └── springdoc.properties
│   ├── infra/
│   │   ├── Dockerfile
│   │   ├── Dockerfile.dev
│   │   ├── docker-compose-postgres.yml
│   │   ├── docker-compose-mysql.yml
│   │   └── docker-compose-mongo.yml
│   └── ci/
│       └── github-actions.yml
└── README.md
```

---

## 5. CLI Interaction Flow

```
$ java-spring-starter

┌  ☕ Java Spring Starter
│
◇  Choose setup mode:
│  ● 🚀 Quick start (recommended preset)
│  ○ 🛠 Custom (step-by-step)
│
◇  Which build tool do you want to use?
│  ● Maven
│  ○ Gradle (Groovy)
│  ○ Gradle (Kotlin)
│
◇  Choose a template:
│  ● Spring Boot + Java (Basic REST API starter)
│  ○ Spring Data JPA + Hibernate (JPA ORM)
│  ○ Spring Data JDBC (Simple JDBC)
│  ○ jOOQ (Type-safe SQL)
│  ○ MyBatis (SQL mapping)
│  ○ Spring Data MongoDB (MongoDB ODM)
│
◇  Select database (if applicable):
│  ● H2 (in-memory, dev)
│  ○ PostgreSQL
│  ○ MySQL
│
◇  Enter your project name:
│  my-spring-app
│
◇  Choose Java version:
│  ● 21 (LTS)
│  ○ 17 (LTS)
│
◇  Choose devtools:
│  ☐ Checkstyle (lint)
│  ☑ SpringDoc OpenAPI (API docs)
│  ☑ Docker
│  ☐ GitHub Actions
│
◇  Configuration summary ────────────────────────╮
│                                                │
│  Setup mode     : quick                        │
│  Build tool     : Maven                        │
│  Template       : Spring Data JPA + PostgreSQL │
│  Project name   : my-spring-app                │
│  Java version   : 21                           │
│  Devtools       : SpringDoc, Docker            │
│                                                │
├────────────────────────────────────────────────╯
│
◆  Proceed with project generation?
│  ● Yes / ○ No
│
✔ Project generated at ./my-spring-app
│
└ 🎉 Done! cd my-spring-app && ./mvnw spring-boot:run
```

---

## 6. Dependencies (the CLI tool itself)

| Purpose | Library |
|---|---|
| CLI framework | `info.picocli:picocli` (4.x) |
| Interactive prompts | `org.jline:jline` (3.x) via PicoCLI |
| Colored output | JLine built-ins or `com.github.freva:ascii-table` |
| Template/file copying | Standard Java NIO + `org.apache.commons:commons-io` |
| YAML generation | `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` |
| XML generation (Maven POM) | `com.fasterxml.jackson.dataformat:jackson-dataformat-xml` |
| JSON processing | `com.fasterxml.jackson.core:jackson-databind` |
| Template processing | `org.apache.maven:maven-model` (for POM manipulation) or just string-replace |
| Packaging | `maven-assembly-plugin` or `maven-shade-plugin` (fat JAR) |

---

## 7. Distribution & Publishing

### 7.1 Distribution Channels

| Channel | How |
|---|---|
| **Maven Central** | Publish via Sonatype OSSRH (group ID: `io.github.ljlm0402`) |
| **SDKMAN!** | Submit to SDKMAN! as a CLI tool |
| **Homebrew** | Create a Homebrew formula (tap) |
| **GitHub Releases** | Attach fat JAR + shell/batch wrapper scripts |
| **JBang** | Publish as a JBang catalog entry |

### 7.2 Maven Central Publishing Requirements

- Group ID: `io.github.ljlm0402`
- Artifact ID: `java-spring-starter`
- Signed artifacts with GPG
- Javadoc and sources JARs attached
- MIT License
- SCM & developer info in POM

### 7.3 Running the CLI

```bash
# Option 1: Download fat JAR from GitHub Releases
java -jar java-spring-starter.jar

# Option 2: Via JBang
jbang io.github.ljlm0402:java-spring-starter:latest

# Option 3: Via Maven wrapper (distribute as a self-contained bootstrap)
curl -s https://start.java-spring-starter.io | bash

# Option 4: SDKMAN!
sdk install java-spring-starter
```

---

## 8. Implementation Phases

### Phase 1 — Core CLI + Default Template (Week 1)

- [x] Set up Maven/Gradle project for the CLI itself
- [ ] Implement PicoCLI + JLine interactive prompts
- [ ] "Quick start" and "Custom" mode flow
- [ ] `default` template: Spring Boot + Spring Web (no DB)
- [ ] Project name validation, directory creation
- [ ] Maven `pom.xml` generation
- [ ] `.gitignore`, `README.md` generation
- [ ] Fat JAR packaging of the CLI
- [ ] Manual test: generate a project → `mvnw spring-boot:run` → hit `http://localhost:8080`

### Phase 2 — ORM Templates (Week 2)

- [ ] `spring-data-jpa` template (JPA + Hibernate, PostgreSQL/H2/MySQL)
- [ ] `spring-data-jdbc` template
- [ ] `jooq` template
- [ ] `mybatis` template
- [ ] Database selection prompt → correct dependencies + docker-compose
- [ ] Flyway/Liquibase migration integration (optional toggle)

### Phase 3 — Devtools + Docker (Week 3)

- [ ] Dockerfile + docker-compose generation
- [ ] Checkstyle + SpotBugs config injection
- [ ] Spotless (code formatting) config
- [ ] SpringDoc OpenAPI integration
- [ ] GitHub Actions CI workflow template
- [ ] EditorConfig generation

### Phase 4 — Gradle Support + Publishing (Week 4)

- [ ] Gradle (Groovy DSL) `build.gradle` generation
- [ ] Gradle (Kotlin DSL) `build.gradle.kts` generation
- [ ] All templates work with both Maven and Gradle
- [ ] Maven Central publishing pipeline (GitHub Actions)
- [ ] GPG signing setup
- [ ] SDKMAN! submission
- [ ] Comprehensive README with examples

### Phase 5 — Polish & Community (Ongoing)

- [ ] Template validation & testing suite
- [ ] MongoDB template
- [ ] GraphQL (Spring for GraphQL) template
- [ ] More devtools: Testcontainers config, ArchUnit, etc.
- [ ] `--non-interactive` mode for CI/CD
- [ ] Project doctor (like `project-doctor.js` in TS version)

---

## 9. Design Decisions / Trade-offs

| Decision | Rationale |
|---|---|
| **Maven as primary build tool** | Wider adoption in enterprise Java; easier Maven Central publishing. Gradle added as secondary. |
| **PicoCLI over Spring Shell** | Lighter weight for a CLI generator; no Spring dependency in the generator itself. |
| **Templates as raw files, not Mustache/Handlebars** | The TS version copies raw files. Simpler, easier to contribute templates. Use string replacement for package name, app name, etc. |
| **Fat JAR distribution** | Simplest cross-platform distribution. Single file. No need for users to install Maven/Gradle first. |
| **Java 17 minimum, 21 recommended** | 17 is the most widely used LTS. 21 is current LTS. Both supported. |
| **Spring Boot 3.x (not 2.x)** | 3.x is current; requires Java 17+; Jakarta EE 9+ namespace. |

---

## 10. Open Questions for Review

1. **Should the CLI require Java to run?** — The TS version requires Node.js. Our CLI would require Java (JDK 17+). This is fine since the user is generating a Java project anyway. But we could also compile to native with GraalVM to remove the JVM requirement. (Effort: high, Benefit: moderate)

ans: Yes should require Java to run or implement what you recommend.

2. **Gradle support in Phase 1 or Phase 4?** — Maven-first gets us to market faster. But Gradle has a significant user base. I'd suggest Maven only in Phase 1, Gradle in Phase 4 unless you feel strongly otherwise.

ans: we should have gradle definitely but feel free to implement what you recommended.

3. **Which templates first?** — Proposed: `default` + `spring-data-jpa` in Phase 1. These cover the 80% use case. Expand from there.

ans: yes go ahead with that.

4. **Maven Central group ID?** — `io.github.ljlm0402` follows the convention for GitHub-hosted personal projects. Is that the group ID you want to use?

ans: 

5. **Database migration tool?** — Flyway or Liquibase? Flyway is simpler and more widely used with Spring Boot. Include it as optional.

ans: implement what you recommended.

6. **Should we also provide a web-based starter like start.spring.io?** — Out of scope for MVP, but the template engine could be extracted later to power a web UI.

ans: no, not yet

---

> Please review this plan and let me know what you'd like to adjust. Once approved, I'll start coding Phase 1.
