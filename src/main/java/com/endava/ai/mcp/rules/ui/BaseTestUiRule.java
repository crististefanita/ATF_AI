package com.endava.ai.mcp.rules.ui;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.context.McpProjectLayout;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;

@com.endava.ai.mcp.contract.McpContract(id = "ui.test-architecture.base-test",
        category = com.endava.ai.mcp.contract.McpRuleCategory.UI_WRAPPER,
        htmlReq = "ui-business-frontier-adapter.html",
        artifact = "review/root-source-thin-no-index.diff")
public final class BaseTestUiRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        for (String relativeFile : UiArchitectureFiles.diffTouchedUiArchitectureFiles(context, result)) {
            if (!UiArchitectureFiles.isUiTestFile(context, relativeFile)) {
                continue;
            }
            Path source = context.projectRoot().resolve(relativeFile).normalize();
            if (!Files.isRegularFile(source)) {
                result.add(McpPackageViolation.blocker("ui-test.architecture.source-missing",
                        "Diff-touched UI architecture source does not exist: " + relativeFile,
                        context.packageRoot()));
                continue;
            }
            String java = context.files().read(source, result);
            if (!java.contains("@Test")) {
                continue;
            }
            if (!java.contains("extends BaseTestUI") && !java.contains("extends " + McpProjectLayout.UI_BASE_TEST_CLASS)) {
                result.add(McpPackageViolation.blocker("ui-test.architecture.base-test-missing",
                        relativeFile + " must extend BaseTestUI", source));
            }
            if (!java.contains(McpProjectLayout.UI_BASE_TEST_IMPORT)
                    && !java.contains("extends " + McpProjectLayout.UI_BASE_TEST_CLASS)) {
                result.add(McpPackageViolation.blocker("ui-test.architecture.base-test-import-missing",
                        relativeFile + " must import/use " + McpProjectLayout.UI_BASE_TEST_CLASS, source));
            }
            if (java.contains("@Listeners")) {
                result.add(McpPackageViolation.blocker("ui-test.architecture.listener-owned-by-base",
                        relativeFile + " must not declare @Listeners; BaseTestUI owns UI listener wiring", source));
            }
        }
    }
}
