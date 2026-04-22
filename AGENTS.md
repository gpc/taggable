# AGENTS.md - taggable

## Project Overview

This is a **Grails Plugin** that adds a generic tagging mechanism to any Grails domain class. Domain classes implement
the `Taggable` trait to gain instance and static methods for attaching, querying, and counting tags, plus a taglib for
rendering a frequency-ranked tag cloud.

- **Language:** Groovy 4.0.30 on Java 17
- **Framework:** Grails 7.x
- **Build System:** Gradle 8.14.4 (with wrapper)
- **Current Version:** 7.0.x-SNAPSHOT
- **License:** Apache 2.0

## Skill Files (Best Practices)

Detailed best practices are documented in `.skills/`:

| Skill File                                                             | Purpose                                               |
|------------------------------------------------------------------------|-------------------------------------------------------|
| [`.skills/repository-structure.md`](.skills/repository-structure.md)   | Canonical directory layout and architectural rules    |
| [`.skills/gradle-best-practices.md`](.skills/gradle-best-practices.md) | Gradle best practices, convention plugins, and idioms |
| [`.skills/plugin-project.md`](.skills/plugin-project.md)               | Plugin project scope: source code + unit tests only   |
| [`.skills/example-apps.md`](.skills/example-apps.md)                   | Example app patterns: integration & functional tests  |

**Read these skill files before making structural changes to the repository.**

## Critical Rules

1. **NEVER add code to the root `build.gradle` to configure subprojects.** No `subprojects {}`, `allprojects {}`, or
   `configure()` blocks. All shared configuration goes through convention plugins in `build-logic/`.
2. **The plugin project contains ONLY plugin code and unit tests.** No integration tests, no functional tests, no
   example controllers or views.
3. **Example apps under `examples/` host all integration and functional tests.** They depend on the plugin via
   `implementation project(':taggable')` and test it as a real consumer would.
4. **Use Gradle convention plugins to deduplicate.** If two or more subprojects share build logic, extract it into a
   convention plugin in `build-logic/`.
5. **Always use lazy Gradle APIs** to avoid eager initialization (`tasks.register()`, `tasks.named()`, `configureEach`,
   `provider {}`).

## Repository Structure

```
taggable/
├── .skills/             # Best practice skill files
├── plugin/              # Core Grails plugin (artifact: taggable)
│   ├── grails-app/      #   Plugin domain classes, services, and taglibs
│   └── src/main/        #   Plugin source code (Taggable trait, plugin descriptor)
├── examples/app1/       # Example Grails app
│   └── grails-app/      #   Domain classes and conf for integration testing
├── docs/                # Asciidoctor documentation
├── build-logic/         # Gradle convention plugins (composite build)
├── .github/workflows/   # CI, release, and release-notes workflows
├── build.gradle         # Root build file (docs + root-publish ONLY)
├── settings.gradle      # Multi-project settings
└── gradle.properties    # Version properties
```

## Build and Test Commands

```bash
# Full build (compile + test)
./gradlew build

# Run only unit tests (plugin module)
./gradlew :taggable:test

# Run integration tests (example app)
./gradlew :app1:integrationTest

# Skip tests
./gradlew build -PskipTests

# Run the example app
./gradlew :app1:bootRun

# Generate documentation
./gradlew docs

# Clean build
./gradlew clean build

# Run code style checks only
./gradlew codeStyle

# Skip code style checks
./gradlew build -PskipCodeStyle
```

## SDK Requirements

Use SDKMAN to install the correct tool versions (see `.sdkmanrc`):

- Java: `17.0.18-librca`
- Gradle: `8.14.4`
- Groovy: `4.0.30`

Run `sdk env install` to set up the environment.

## Architecture

The plugin provides a trait-based tagging mechanism with a supporting service, domain model, and tag library:

1. **`TaggableGrailsPlugin`** is the plugin descriptor. It extends `grails.plugins.Plugin`, observes `hibernate`, and
   calls `taggableService.refreshDomainClasses()` on startup and `onChange` so the domain class family map stays in
   sync with reloaded artifacts.
2. **`Taggable`** is a Groovy trait that domain classes implement to become taggable. It provides instance methods
   like `addTag(name)`, `addTags(names)`, `removeTag(name)`, `setTags(list)`, `parseTags(str, delimiter)`, and
   `getTags()`, plus static methods `findAllByTag(name)`, `findAllByTagWithCriteria(name, closure)`,
   `countByTag(name)`, `getAllTags()`, `getTotalTags()`, and `findAllTagsWithCriteria(params, closure)`.
3. **`TaggableService`** maintains `domainClassFamilies` — a map from class name to the property names of that class
   plus all of its subclasses — so polymorphic queries against `TagLink` can include subclass references. It exposes
   `getTagCounts(type)` for aggregation and `refreshDomainClasses()` which is called by the plugin lifecycle.
4. **`Tag`** and **`TagLink`** are the domain entities. `Tag` stores the unique tag name; `TagLink` is the polymorphic
   join table with `tag`, `tagRef` (the id of the tagged instance), and `type` (the simple class name of the tagged
   domain class).
5. **`TagsTagLib`** (namespace `tags`) provides `<tags:tagCloud>` for rendering a frequency-ranked tag cloud where CSS
   class names scale with tag frequency.

### Core Classes

| Class / Interface       | Location                                              | Purpose                                                            |
|-------------------------|-------------------------------------------------------|--------------------------------------------------------------------|
| `TaggableGrailsPlugin`  | `plugin/src/main/groovy/grails/plugins/taggable/`     | Plugin descriptor; refreshes domain class families on load/change  |
| `Taggable`              | `plugin/src/main/groovy/grails/plugins/taggable/`     | Trait that adds tagging methods to a domain class                  |
| `TagException`          | `plugin/src/main/groovy/grails/plugins/taggable/`     | Thrown when a tag is in an invalid state                           |
| `Tag`                   | `plugin/grails-app/domain/grails/plugins/taggable/`   | Domain entity storing the unique tag name                          |
| `TagLink`               | `plugin/grails-app/domain/grails/plugins/taggable/`   | Polymorphic join entity (`tag`, `tagRef`, `type`)                  |
| `TaggableService`       | `plugin/grails-app/services/grails/plugins/taggable/` | Maintains `domainClassFamilies`; exposes `getTagCounts(type)`      |
| `TagsTagLib`            | `plugin/grails-app/taglib/grails/plugins/taggable/`   | Tag library: `<tags:tagCloud>`                                     |

## Configuration

The plugin reads the following keys from the application config (for example, `application.yml` or `application.groovy`):

| Key                                | Type         | Default                                                | Purpose                                                         |
|------------------------------------|--------------|--------------------------------------------------------|-----------------------------------------------------------------|
| `grails.taggable.preserve.case`    | boolean      | `false`                                                | When `false`, tag names are normalized to lowercase              |
| `grails.taggable.tag.table`        | string       | (Hibernate default)                                    | Overrides the physical table name for `Tag`                      |
| `grails.taggable.tagLink.table`    | string       | (Hibernate default)                                    | Overrides the physical table name for `TagLink`                  |
| `grails.taggable.tag.autoImport`   | boolean      | (Hibernate default)                                    | Toggle Hibernate `autoImport` for `Tag`                          |
| `grails.taggable.tagLink.autoImport` | boolean    | (Hibernate default)                                    | Toggle Hibernate `autoImport` for `TagLink`                      |
| `grails.taggable.css.classes`      | list<string> | `['smallest','small','medium','large','largest']`      | CSS classes used by `<tags:tagCloud>` to scale by frequency      |

Example `application.groovy`:

```groovy
grails {
    taggable {
        preserve.case = false
        css.classes = ['tag-xs', 'tag-sm', 'tag-md', 'tag-lg', 'tag-xl']
        tag.table = 'my_tag'
        tagLink.table = 'my_tag_link'
    }
}
```

## Testing

### Unit Tests (`plugin/src/test/`)

Unit tests use the **Spock Framework** and run on JUnit Platform.

### Integration / Functional Tests (`examples/app1/`)

The example app under `examples/app1/` declares taggable domain classes and exercises the `Taggable` trait, the
`TaggableService`, and the `<tags:tagCloud>` taglib against a real datastore. Integration and functional tests added
here depend on the plugin as a real consumer would.

## Build-Logic Convention Plugins

Convention plugins in `build-logic/src/main/groovy/` standardize build configuration:

| Plugin                   | Purpose                                                                              |
|--------------------------|--------------------------------------------------------------------------------------|
| `app-run.gradle`         | Debug flags for `bootRun`                                                            |
| `compile.gradle`         | Java/Groovy compilation settings (UTF-8, incremental, Java release from `.sdkmanrc`) |
| `docs.gradle`            | Documentation aggregation (Groovydoc + Asciidoctor)                                  |
| `example-app.gradle`     | Example app config (grails-web, GSP, assets)                                         |
| `grails-assets.gradle`   | Asset pipeline with Bootstrap/jQuery WebJars                                         |
| `grails-plugin.gradle`   | Grails plugin application                                                            |
| `publish.gradle`         | Per-project Maven publishing metadata                                                |
| `publish-root.gradle`    | Root-level Nexus publishing workaround                                               |
| `testing.gradle`         | Test framework config (Spock, JUnit Platform, test-logger)                           |

## CI/CD

- **CI** (`.github/workflows/ci.yml`): Builds and tests on push/PR; publishes snapshots to Maven Central Snapshots on
  push to release branches.
- **Release** (`.github/workflows/release.yml`): 4-stage pipeline triggered by GitHub release — stage artifacts, release
  to Maven Central, publish docs to GitHub Pages, bump version.
- **Release Notes** (`.github/workflows/release-notes.yml`): Auto-drafts release notes using release-drafter with
  category labels.

## Code Conventions

- Groovy source files use standard Grails conventions (services, domain classes, and taglibs in `grails-app/`, other
  classes in `src/main/groovy/`).
- **Use `def` for local variables** where the type is inferred from the right-hand side (e.g., constructor calls,
  method calls, casts, factory methods). Explicit types should only be used for local variables when the type cannot
  be inferred or when needed for `@CompileStatic` compilation. This applies to both production code and tests.
- When writing Gradle, always use the latest best practices to avoid eager initialization.
