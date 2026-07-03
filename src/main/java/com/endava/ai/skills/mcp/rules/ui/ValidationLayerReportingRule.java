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
import java.util.regex.Pattern;

@McpContract(id = "ui.test-architecture.validation-layer-reporting",
        category = McpRuleCategory.UI_WRAPPER,
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
            Path source = context.sourceAuditScope().sourceRoot().resolve(relativeFile).normalize();
            if (!Files.isRegularFile(source)) {
                if (context.sourceAuditScope().canProveSourcePresence()) {
                    result.add(McpPackageViolation.blocker("ui-test.architecture.source-missing",
                            "Diff-touched UI validation source does not exist under audited source root "
                                    + context.sourceAuditScope().sourceRootDescription() + ": " + relativeFile,
                            context.packageRoot()));
                } else {
                    result.add(McpViolationBuilder.warning("ui-test.architecture.requires-source-root")
                            .message("Cannot audit diff-touched UI validation source because package root is external and -Dmcp.project.root was not provided: "
                                    + relativeFile)
                            .path(context.packageRoot())
                            .meaning("MCP cannot prove validation-layer StepLogger/assertion discipline from the validator workspace")
                            .repair("rerun package audit with -Dmcp.project.root=<audited-project-root>")
                            .artifact(relativeFile)
                            .build());
                }
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
