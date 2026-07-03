package com.endava.ai.skills.mcp.rules.ui;

import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.McpViolationBuilder;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.context.McpProjectLayout;
import com.endava.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;

@McpContract(id = "ui.test-architecture.base-test",
        category = McpRuleCategory.UI_WRAPPER,
        htmlReq = "ui-business-frontier-adapter.html",
        artifact = "review/root-source-thin-no-index.diff")
public final class BaseTestUiRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        for (String relativeFile : UiArchitectureFiles.diffTouchedUiArchitectureFiles(context, result)) {
            if (!UiArchitectureFiles.isUiTestFile(context, relativeFile)) {
                continue;
            }
            Path source = context.sourceAuditScope().sourceRoot().resolve(relativeFile).normalize();
            if (!Files.isRegularFile(source)) {
                if (context.sourceAuditScope().canProveSourcePresence()) {
                    result.add(McpPackageViolation.blocker("ui-test.architecture.source-missing",
                            "Diff-touched UI architecture source does not exist under audited source root "
                                    + context.sourceAuditScope().sourceRootDescription() + ": " + relativeFile,
                            context.packageRoot()));
                } else {
                    result.add(McpViolationBuilder.warning("ui-test.architecture.requires-source-root")
                            .message("Cannot audit diff-touched UI test source because package root is external and -Dmcp.project.root was not provided: "
                                    + relativeFile)
                            .path(context.packageRoot())
                            .meaning("MCP cannot prove BaseTestUI/reporting ownership from the validator workspace")
                            .repair("rerun package audit with -Dmcp.project.root=<audited-project-root>")
                            .artifact(relativeFile)
                            .build());
                }
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
