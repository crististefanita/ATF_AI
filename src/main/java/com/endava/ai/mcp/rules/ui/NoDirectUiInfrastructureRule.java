package com.endava.ai.mcp.rules.ui;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;

@com.endava.ai.mcp.contract.McpContract(id = "ui.test-architecture.no-direct-infra",
        category = com.endava.ai.mcp.contract.McpRuleCategory.UI_WRAPPER,
        htmlReq = "Living_Architecture_UI_API_doc_v1_0.html",
        artifact = "review/root-source-thin-no-index.diff")
public final class NoDirectUiInfrastructureRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        for (String relativeFile : UiArchitectureFiles.diffTouchedUiArchitectureFiles(context, result)) {
            if (!UiArchitectureFiles.isUiTestFile(context, relativeFile)) {
                continue;
            }
            Path source = context.projectRoot().resolve(relativeFile).normalize();
            if (!Files.isRegularFile(source)) {
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
