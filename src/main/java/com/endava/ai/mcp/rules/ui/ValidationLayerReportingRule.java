package com.endava.ai.mcp.rules.ui;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

@com.endava.ai.mcp.contract.McpContract(id = "ui.test-architecture.validation-layer-reporting",
        category = com.endava.ai.mcp.contract.McpRuleCategory.UI_WRAPPER,
        htmlReq = "Living_Architecture_UI_API_doc_v1_0.html",
        artifact = "src/main/java/**/ui/**/validation/*.java")
public final class ValidationLayerReportingRule implements McpValidationRule {
    private static final Pattern MUTED_ASSERT_EQUALS = Pattern.compile("Assert\\.assertEquals\\s*\\([^,]+,[^,]+\\)\\s*;");
    private static final Pattern MUTED_ASSERT_TRUE = Pattern.compile("Assert\\.assert(True|False)\\s*\\([^,]+\\)\\s*;");

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        for (String relativeFile : UiArchitectureFiles.diffTouchedUiArchitectureFiles(context, result)) {
            if (!UiArchitectureFiles.isUiValidationFile(relativeFile)) {
                continue;
            }
            Path source = context.projectRoot().resolve(relativeFile).normalize();
            if (!Files.isRegularFile(source)) {
                result.add(McpPackageViolation.blocker("ui-test.architecture.source-missing",
                        "Diff-touched UI validation source does not exist: " + relativeFile,
                        context.packageRoot()));
                continue;
            }
            String java = context.files().read(source, result);
            if (!UiArchitectureFiles.containsAssertion(java)) {
                continue;
            }
            if (!java.contains("StepLogger.")) {
                result.add(McpPackageViolation.blocker("ui-test.architecture.validation-reporting-missing",
                        relativeFile + " uses assertions but does not emit validation steps/details through StepLogger", source));
            }
            if (MUTED_ASSERT_EQUALS.matcher(java).find() || MUTED_ASSERT_TRUE.matcher(java).find()) {
                result.add(McpPackageViolation.blocker("ui-test.architecture.validation-message-missing",
                        relativeFile + " uses assertions without explicit messages; validation details will not be audit-grade in Extent", source));
            }
        }
    }
}
