package com.endava.ai.atlas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AtlasContext {
    public static final String ATLAS_HTML        = "docs/atlas/business-test-atlas.html";
    public static final String SHARED_CSS        = "docs/shared-report-reference.css";
    public static final String SOURCE_ROOT       = "src/test/java/com/endava/ai/ui/tests";
    public static final String UI_SOURCE_ROOT    = "src/main/java/com/endava/ai/ui";
    public static final String REQUIREMENTS_HTML = "docs/prompts/iterativ/business-test-atlas-reference.html";

    private final Path projectRoot;
    private final Path atlasHtml;
    private final Path sharedCss;
    private final Path sourceRoot;
    private final Path uiSourceRoot;
    private final Path requirementsHtml;
    private final String atlasText;

    private AtlasContext(Path projectRoot) throws IOException {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.atlasHtml = this.projectRoot.resolve(ATLAS_HTML).normalize();
        this.sharedCss = this.projectRoot.resolve(SHARED_CSS).normalize();
        this.sourceRoot = this.projectRoot.resolve(SOURCE_ROOT).normalize();
        this.uiSourceRoot = this.projectRoot.resolve(UI_SOURCE_ROOT).normalize();
        this.requirementsHtml = this.projectRoot.resolve(REQUIREMENTS_HTML).normalize();
        this.atlasText = Files.exists(atlasHtml)
                ? Files.readString(atlasHtml, StandardCharsets.UTF_8)
                : "";
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

    public Path projectRoot() {
        return projectRoot;
    }

    public Path atlasHtml() {
        return atlasHtml;
    }

    public Path sharedCss() {
        return sharedCss;
    }

    public Path sourceRoot() {
        return sourceRoot;
    }

    public Path uiSourceRoot() {
        return uiSourceRoot;
    }

    public Path requirementsHtml() {
        return requirementsHtml;
    }

    public String atlasText() {
        return atlasText;
    }

    public String lowerAtlasText() {
        return atlasText.toLowerCase(java.util.Locale.ROOT);
    }
}
