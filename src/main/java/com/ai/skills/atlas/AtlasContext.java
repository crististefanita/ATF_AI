package com.ai.skills.atlas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class AtlasContext {
    public static final String ATLAS_HTML        = "docs/atlas/business-test-atlas.html";
    public static final String SHARED_CSS        = "docs/shared-report-reference.css";
    public static final String SOURCE_ROOT       = "src/test/java/com/ai/ui/tests";
    public static final String UI_SOURCE_ROOT    = "src/main/java/com/ai/ui";
    public static final String REQUIREMENTS_HTML = "docs/prompts/html_ro/atlas/business-test-atlas-reference.html";
    public static final String ATLAS_DIRECTORY   = "docs/atlas";
    public static final String VALIDATION_DIRECTORY = ATLAS_DIRECTORY + "/validation";
    public static final String BUSINESS_ATLAS_FILE = "business-test-atlas.html";
    public static final String BUSINESS_ATLAS_SUFFIX = "_business-test-atlas.html";
    public static final String FRAMEWORK_ATLAS_SUFFIX = "_framework-test-atlas.html";
    public static final String ATLAS_EXTENT_REPORT_FILE = "atlas-tests-extent-report.html";
    public static final String ATLAS_EXTENT_REPORT_SUFFIX = "_atlas-tests-extent-report.html";
    public static final String FRAMEWORK_ATLAS_EXTENT_REPORT_SUFFIX = "_framework-atlas-tests-extent-report.html";
    public static final String FRAMEWORK_SCOPE = "framework-under-test";
    public static final String HTML_SUFFIX = ".html";
    public static final String NONSTANDARD_HTML_SUFFIX = ".nonstandard.html";
    public static final String README_SUFFIX = ".README.md";
    public static final String COMMAND_SUFFIX = ".command.txt";
    public static final String TESTNG_XML_SUFFIX = ".testng.xml";
    public static final String MAVEN_LOG_SUFFIX = ".maven.log";

    private static final String CORE_MAIN_ROOT = "src/main/java/com/ai/core";
    private static final String GENERIC_MAIN_ROOT = "src/main/java/com/ai";
    private static final String ATLAS_HTML_PROPERTY        = "atlas.html";
    private static final String SHARED_CSS_PROPERTY        = "atlas.sharedCss";
    private static final String SOURCE_ROOT_PROPERTY       = "atlas.sourceRoot";
    private static final String UI_SOURCE_ROOT_PROPERTY    = "atlas.uiSourceRoot";
    private static final String REQUIREMENTS_HTML_PROPERTY = "atlas.requirementsHtml";
    private static final Pattern DECLARED_TEST_SOURCE_ROOT = Pattern.compile(
            "(?:[A-Za-z]:[/\\\\][^<\\s]+[/\\\\])?(src[/\\\\]test[/\\\\]java[/\\\\]com[/\\\\]atf[/\\\\]ai[/\\\\][A-Za-z0-9_./\\\\-]+)"
    );
    private static final Pattern DECLARED_ATLAS_HTML = Pattern.compile(
            "([a-z0-9-]+_)?" + Pattern.quote(BUSINESS_ATLAS_FILE),
            Pattern.CASE_INSENSITIVE
    );

    private final Path projectRoot;
    private final Path atlasHtml;
    private final Path sharedCss;
    private final Path sourceRoot;
    private final Path uiSourceRoot;
    private final Path requirementsHtml;
    private final String atlasText;

    private AtlasContext(Path projectRoot) throws IOException {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.atlasHtml = resolveAtlasPath(projectRoot);
        this.sharedCss = resolveConfiguredPath(projectRoot, SHARED_CSS_PROPERTY, SHARED_CSS);
        this.requirementsHtml = resolveConfiguredPath(projectRoot, REQUIREMENTS_HTML_PROPERTY, REQUIREMENTS_HTML);
        this.atlasText = Files.exists(atlasHtml)
                ? Files.readString(atlasHtml, StandardCharsets.UTF_8)
                : "";
        this.sourceRoot = resolveSourceRoot(projectRoot, atlasText);
        this.uiSourceRoot = resolveTraversedMainRoot(projectRoot, atlasText);
    }

    private static Path resolveConfiguredPath(Path projectRoot, String propertyName, String defaultValue) {
        String value = System.getProperty(propertyName, defaultValue);
        Path configured = Path.of(value);
        return (configured.isAbsolute() ? configured : projectRoot.resolve(configured)).normalize();
    }

    private static Path resolveSourceRoot(Path projectRoot, String atlasText) {
        String configured = System.getProperty(SOURCE_ROOT_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return resolvePath(projectRoot, configured);
        }
        Matcher matcher = DECLARED_TEST_SOURCE_ROOT.matcher(atlasText);
        if (matcher.find()) {
            return resolvePath(projectRoot, matcher.group(1));
        }
        return resolvePath(projectRoot, SOURCE_ROOT);
    }

    private static Path resolveTraversedMainRoot(Path projectRoot, String atlasText) {
        String configured = System.getProperty(UI_SOURCE_ROOT_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return resolvePath(projectRoot, configured);
        }
        if (atlasText.contains(CORE_MAIN_ROOT)) {
            return resolvePath(projectRoot, GENERIC_MAIN_ROOT);
        }
        return resolvePath(projectRoot, UI_SOURCE_ROOT);
    }

    private static Path resolvePath(Path projectRoot, String value) {
        Path configured = Path.of(value);
        return (configured.isAbsolute() ? configured : projectRoot.resolve(configured)).normalize();
    }

    private static Path resolveAtlasPath(Path projectRoot) {
        String configuredValue = System.getProperty(ATLAS_HTML_PROPERTY);
        if (configuredValue != null && !configuredValue.isBlank()) {
            Path configured = Path.of(configuredValue);
            return (configured.isAbsolute() ? configured : projectRoot.resolve(configured)).normalize();
        }

        Path scoped = projectRoot.resolve(ATLAS_DIRECTORY)
                .resolve(projectScope(projectRoot) + BUSINESS_ATLAS_SUFFIX)
                .normalize();
        if (Files.exists(scoped)) {
            return scoped;
        }

        Path generic = projectRoot.resolve(ATLAS_HTML).normalize();
        if (Files.exists(generic)) {
            return generic;
        }

        Path framework = projectRoot.resolve(ATLAS_DIRECTORY)
                .resolve(FRAMEWORK_SCOPE + BUSINESS_ATLAS_SUFFIX)
                .normalize();
        if (Files.exists(framework)) {
            return framework;
        }

        return firstDeclaredAtlas(projectRoot.resolve(ATLAS_DIRECTORY)).orElse(generic);
    }

    private static String projectScope(Path projectRoot) {
        Path atfFolder = projectRoot.getParent();
        if (atfFolder == null || atfFolder.getFileName() == null) {
            return "ATF";
        }
        return atfFolder.getFileName().toString().toUpperCase(Locale.ROOT);
    }

    private static Optional<Path> firstDeclaredAtlas(Path atlasDir) {
        if (!Files.isDirectory(atlasDir)) {
            return Optional.empty();
        }
        try (Stream<Path> files = Files.list(atlasDir)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)
                            .matches(DECLARED_ATLAS_HTML.pattern()))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .findFirst()
                    .map(Path::normalize);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static AtlasContext fromWorkingDirectory() throws IOException {
        return new AtlasContext(findProjectRoot(Path.of("").toAbsolutePath()));
    }

    private static Path findProjectRoot(Path start) {
        Path current = start.toAbsolutePath().normalize();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml"))
                    && Files.exists(current.resolve("src"))
                    && Files.exists(current.resolve("docs"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate test-framework project root from " + start);
    }

    public String lowerAtlasText() {
        return atlasText.toLowerCase(Locale.ROOT);
    }
}
