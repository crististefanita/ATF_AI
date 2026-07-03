package com.ai.skills.mcp.rules.ui;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.McpViolationBuilder;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

@McpContract(id = "ui.test-architecture.framework-reporting-enabled",
        category = McpRuleCategory.UI_WRAPPER,
        htmlReq = "Living_Architecture_UI_API_doc_v1_0.html",
        artifact = "review/root-source-thin-no-index.diff")
public final class NoFrameworkReportingInGeneratedUiTestsRule implements McpValidationRule {
    private static final Pattern NO_FRAMEWORK_REPORTING_ANNOTATION =
            Pattern.compile("(?m)^\\s*@(?:[\\w.]+\\.)?NoFrameworkReporting\\b");

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
                            .message("Cannot audit @NoFrameworkReporting usage because package root is external and -Dmcp.project.root was not provided: "
                                    + relativeFile)
                            .path(context.packageRoot())
                            .meaning("MCP cannot prove UI reporting visibility from the validator workspace")
                            .repair("rerun package audit with -Dmcp.project.root=<audited-project-root>")
                            .artifact(relativeFile)
                            .build());
                }
                continue;
            }
            String java = context.files().read(source, result);
            if (usesNoFrameworkReporting(java)) {
                result.add(McpPackageViolation.blocker(
                        "ui-test.architecture.framework-reporting-disabled",
                        relativeFile + " must not use @NoFrameworkReporting; generated MCP UI tests must stay visible "
                                + "in the framework Extent/Allure reporting lifecycle",
                        source));
            }
        }
    }

    private boolean usesNoFrameworkReporting(String java) {
        return NO_FRAMEWORK_REPORTING_ANNOTATION.matcher(stripJavaComments(java)).find();
    }

    private String stripJavaComments(String java) {
        StringBuilder clean = new StringBuilder(java.length());
        boolean blockComment = false;
        boolean lineComment = false;
        boolean stringLiteral = false;
        boolean charLiteral = false;
        boolean escaped = false;
        for (int i = 0; i < java.length(); i++) {
            char current = java.charAt(i);
            char next = i + 1 < java.length() ? java.charAt(i + 1) : '\0';

            if (lineComment) {
                if (current == '\n') {
                    lineComment = false;
                    clean.append(current);
                }
                continue;
            }
            if (blockComment) {
                if (current == '*' && next == '/') {
                    blockComment = false;
                    i++;
                } else if (current == '\n') {
                    clean.append(current);
                }
                continue;
            }
            if (!stringLiteral && !charLiteral && current == '/' && next == '/') {
                lineComment = true;
                i++;
                continue;
            }
            if (!stringLiteral && !charLiteral && current == '/' && next == '*') {
                blockComment = true;
                i++;
                continue;
            }
            clean.append(current);

            if (escaped) {
                escaped = false;
                continue;
            }
            if ((stringLiteral || charLiteral) && current == '\\') {
                escaped = true;
            } else if (!charLiteral && current == '"') {
                stringLiteral = !stringLiteral;
            } else if (!stringLiteral && current == '\'') {
                charLiteral = !charLiteral;
            }
        }
        return clean.toString();
    }
}
