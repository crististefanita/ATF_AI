package com.ai.skills.mcp.rules.ui;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.context.McpProjectLayout;
import com.ai.skills.mcp.context.McpRunArtifacts;
import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.fs.DiffFileExtractor;
import com.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

@McpContract(id = "ui-api-generated.structure.modern-layout",
        category = McpRuleCategory.UI_WRAPPER,
        htmlReq = "Living_Architecture_UI_API_doc_v1_0.html",
        artifact = "review/root-source-thin-no-index.diff")
public final class GeneratedUiApiStructureRule implements McpValidationRule {
    private static final String UI_SUPPORT = McpProjectLayout.UI_MAIN_PATH + "support/";
    private static final String UI_TESTS = McpProjectLayout.TEST_BASE_PATH + "/ui/tests/";
    private static final String API_SUPPORT = McpProjectLayout.API_MAIN_PATH + "support/";
    private static final String API_TESTS = McpProjectLayout.TEST_BASE_PATH + "/api/tests/";

    private final DiffFileExtractor diffFileExtractor = new DiffFileExtractor();

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        for (String file : referencedJavaFiles(context, result)) {
            if (isLegacyUiPath(file)) {
                result.add(McpPackageViolation.blocker("ui-generated.structure.legacy-path",
                        "Generated UI code must use ui.support.* and ui.tests.<business-area>.*: " + file,
                        context.packageRoot()));
            }
            if (isLegacyApiPath(file)) {
                result.add(McpPackageViolation.blocker("api-generated.structure.legacy-path",
                        "Generated API code must use api.support.* and api.tests.<business-area>.*: " + file,
                        context.packageRoot()));
            }
        }
    }

    private Set<String> referencedJavaFiles(McpPackageContext context, McpPackageValidationResult result) {
        Set<String> files = new LinkedHashSet<>();
        addReferencedFiles(context, context.paths().rootDiff(), result, files);
        for (Path runDirectory : context.files().runDirectories(context.paths().runsRoot(), result)) {
            addReferencedFiles(context, runDirectory.resolve(McpRunArtifacts.DIFF), result, files);
        }
        return files;
    }

    private void addReferencedFiles(McpPackageContext context,
                                    Path diff,
                                    McpPackageValidationResult result,
                                    Set<String> files) {
        if (!Files.isRegularFile(diff)) {
            return;
        }
        for (String referencedFile : diffFileExtractor.referencedFiles(context.files().read(diff, result))) {
            String normalized = referencedFile.replace('\\', '/');
            if (normalized.endsWith(".java")) {
                files.add(normalized);
            }
        }
    }

    private boolean isLegacyUiPath(String file) {
        if (file.startsWith(McpProjectLayout.UI_CORE_MAIN_PATH)) {
            return false;
        }
        if (file.startsWith(McpProjectLayout.UI_MAIN_PATH)) {
            return !file.startsWith(UI_SUPPORT);
        }
        return file.startsWith(UI_TESTS) && isDirectChild(file, UI_TESTS);
    }

    private boolean isLegacyApiPath(String file) {
        if (file.startsWith(McpProjectLayout.API_CORE_MAIN_PATH)) {
            return false;
        }
        if (file.startsWith(McpProjectLayout.API_MAIN_PATH)) {
            return !file.startsWith(API_SUPPORT);
        }
        return file.startsWith(API_TESTS) && isDirectChild(file, API_TESTS);
    }

    private boolean isDirectChild(String file, String root) {
        String remainder = file.substring(root.length());
        return !remainder.contains("/");
    }
}
