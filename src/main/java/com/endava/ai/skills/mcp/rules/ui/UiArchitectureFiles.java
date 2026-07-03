package com.endava.ai.skills.mcp.rules.ui;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.context.McpProjectLayout;
import com.endava.ai.skills.mcp.context.McpRunArtifacts;
import com.endava.ai.skills.mcp.fs.DiffFileExtractor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

final class UiArchitectureFiles {
    private static final DiffFileExtractor DIFF_FILE_EXTRACTOR = new DiffFileExtractor();

    private UiArchitectureFiles() {
    }

    static Set<String> diffTouchedUiArchitectureFiles(McpPackageContext context, McpPackageValidationResult result) {
        Set<String> files = new LinkedHashSet<>();
        addReferencedUiArchitectureFiles(context, context.paths().rootDiff(), result, files);
        for (Path runDirectory : context.files().runDirectories(context.paths().runsRoot(), result)) {
            addReferencedUiArchitectureFiles(context, runDirectory.resolve(McpRunArtifacts.DIFF), result, files);
        }
        return files;
    }

    static boolean isUiTestFile(McpPackageContext context, String file) {
        return isRuntimeUiTestFile(context, file) || isGenericUiTestFile(file);
    }

    static boolean isUiValidationFile(String file) {
        return file.startsWith(McpProjectLayout.MAIN_JAVA_ROOT + "/")
                && file.contains("/ui/")
                && (file.contains("/validation/") || file.contains("/validator/"))
                && file.endsWith(".java");
    }

    static boolean containsAssertion(String java) {
        return java.contains("import org.testng.Assert;")
                || java.contains("org.testng.Assert.")
                || java.contains("Assert.assert");
    }

    static Set<String> importedProjectClasses(String java) {
        Set<String> imports = new LinkedHashSet<>();
        for (String line : java.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith(McpProjectLayout.UI_IMPORT_PREFIX)
                    && !trimmed.endsWith(".*;")
                    && trimmed.endsWith(";")) {
                imports.add(trimmed.substring("import ".length(), trimmed.length() - 1));
            }
        }
        return imports;
    }

    static boolean containsUiInteractionToken(String java) {
        return java.contains("UIActions.")
                || java.contains("DriverManager.getEngine()")
                || java.contains(".getEngine().")
                || java.contains(".pages.")
                || java.contains(".page.")
                || java.contains(".actions.")
                || java.contains(".component.");
    }

    private static void addReferencedUiArchitectureFiles(McpPackageContext context,
                                                         Path diff,
                                                         McpPackageValidationResult result,
                                                         Set<String> files) {
        if (!Files.isRegularFile(diff)) {
            return;
        }
        for (String relative : DIFF_FILE_EXTRACTOR.referencedFiles(context.files().read(diff, result))) {
            String normalized = relative.replace('\\', '/');
            if (isSeedRecording(context, normalized)) {
                continue;
            }
            if (isUiTestFile(context, normalized) || isUiValidationFile(normalized)) {
                files.add(normalized);
            }
        }
    }

    private static boolean isSeedRecording(McpPackageContext context, String file) {
        String seed = context.runtime().property("SEED_RECORDING_CLASS").replace('\\', '/');
        return !seed.isBlank() && file.equals(seed);
    }

    private static boolean isRuntimeUiTestFile(McpPackageContext context, String file) {
        String uiTestRoot = context.runtime().property("UI_TEST_ROOT").replace('\\', '/');
        return !uiTestRoot.isBlank() && file.startsWith(uiTestRoot.endsWith("/") ? uiTestRoot : uiTestRoot + "/");
    }

    private static boolean isGenericUiTestFile(String file) {
        return file.startsWith(McpProjectLayout.TEST_JAVA_ROOT + "/")
                && file.contains("/ui/")
                && (file.endsWith("Test.java") || file.endsWith("Tests.java"));
    }
}
