package com.ai.atf.ui.component.engine;

import com.ai.core.support.contract.ids.AtfSourcePaths;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class UiEngineContractSourceSupport {
    static final Path MAIN_UI_ROOT = AtfSourcePaths.MAIN_UI_ROOT;
    static final Path UI_ENGINE_ROOT = AtfSourcePaths.MAIN_UI_CORE_ROOT.resolve("engine");
    static final Path UI_RUNTIME_ROOT = AtfSourcePaths.MAIN_UI_CORE_ROOT.resolve("runtime");
    static final Path UI_SUPPORT_ROOT = MAIN_UI_ROOT.resolve("support");

    private UiEngineContractSourceSupport() {
    }

    static boolean containsStepLoggerReference(Path source) {
        try {
            String content = Files.readString(source);
            return content.contains("import com.ai.core.reporting.StepLogger")
                    || content.contains("StepLogger.");
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot read UI engine source: " + source, failure);
        }
    }

    static boolean hasBrowserSpecificImport(Path source) {
        try {
            String content = Files.readString(source);
            return content.contains("import com.microsoft.playwright.")
                    || content.contains("import org.openqa.selenium.");
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot read Java source: " + source, failure);
        }
    }

    static boolean isUiEnginePackage(Path source) {
        String normalized = AtfSourcePaths.unixPath(source);
        return normalized.contains(AtfSourcePaths.unixPath(AtfSourcePaths.MAIN_UI_CORE_ROOT.resolve("engine")) + "/");
    }

    static void assertSourceContains(Path source, String token) throws IOException {
        Assert.assertTrue(
                Files.readString(source).contains(token),
                "Expected " + source + " to contain token: " + token
        );
    }

    static long directJavaClassCount(Path packageRoot) throws IOException {
        try (java.util.stream.Stream<Path> files = Files.list(packageRoot)) {
            return files
                    .filter(AtfSourcePaths::isJavaSource)
                    .count();
        }
    }

    static Path uiPath(String first, String... more) {
        return MAIN_UI_ROOT.resolve(Path.of(first, more));
    }

    static Path uiEngineSource(String classNameSuffix) {
        return AtfSourcePaths.mainJava(AtfSourcePaths.UI_CORE_PACKAGE + ".engine." + classNameSuffix);
    }
}
