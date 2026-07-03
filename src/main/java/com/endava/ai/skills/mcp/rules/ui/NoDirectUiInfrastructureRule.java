package com.endava.ai.skills.mcp.rules.ui;

import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.McpViolationBuilder;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;

@McpContract(id = "ui.test-architecture.no-direct-infra",
        category = McpRuleCategory.UI_WRAPPER,
        htmlReq = "Living_Architecture_UI_API_doc_v1_0.html",
        artifact = "review/root-source-thin-no-index.diff")
public final class NoDirectUiInfrastructureRule implements McpValidationRule {
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
                            "Diff-touched UI test source does not exist under audited source root "
                                    + context.sourceAuditScope().sourceRootDescription() + ": " + relativeFile,
                            context.packageRoot()));
                } else {
                    result.add(McpViolationBuilder.warning("ui-test.architecture.requires-source-root")
                            .message("Cannot audit direct UI infrastructure usage because package root is external and -Dmcp.project.root was not provided: "
                                    + relativeFile)
                            .path(context.packageRoot())
                            .meaning("MCP cannot prove generated UI tests avoid framework internals from the validator workspace")
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
            if (containsForbiddenUiArchitectureToken(java)) {
                result.add(McpPackageViolation.blocker("ui-test.architecture.direct-engine-or-reporting",
                        relativeFile + " must not access UI engine/reporting infrastructure directly", source));
            }
            if (containsDirectAssertionInTestLayer(java)) {
                result.add(McpPackageViolation.blocker("ui-test.architecture.direct-assertion",
                        relativeFile + " must not assert directly in the test layer; move assertions to a validation layer", source));
            }
        }
    }

    private boolean containsForbiddenUiArchitectureToken(String java) {
        return java.contains("DriverManager.")
                || java.contains("UIEngine")
                || java.contains("StepLogger.")
                || java.contains("ReportingManager.")
                || java.contains("ExtentReports")
                || java.contains("Allure.")
                || java.contains("WebDriver ")
                || java.contains("Page ")
                || java.contains("Locator ");
    }

    private boolean containsDirectAssertionInTestLayer(String java) {
        return UiArchitectureFiles.containsAssertion(java)
                || java.contains("assertThat(")
                || java.contains("Assertions.assert")
                || java.contains("assertEquals(")
                || java.contains("assertTrue(")
                || java.contains("assertFalse(");
    }
}
