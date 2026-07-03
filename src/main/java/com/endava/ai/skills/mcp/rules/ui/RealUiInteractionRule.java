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

@McpContract(id = "ui.test-architecture.real-ui-interaction",
        category = McpRuleCategory.UI_WRAPPER,
        htmlReq = "ui-business-frontier-adapter.html",
        artifact = "src/test/java/**/ui/**/*Test.java")
public final class RealUiInteractionRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        for (String relativeFile : UiArchitectureFiles.diffTouchedUiArchitectureFiles(context, result)) {
            if (!UiArchitectureFiles.isUiTestFile(context, relativeFile)) {
                continue;
            }
            Path source = context.sourceAuditScope().sourceRoot().resolve(relativeFile).normalize();
            if (!Files.isRegularFile(source)) {
                addSourceUnavailable(context, result, relativeFile, "test source");
                continue;
            }
            String java = context.files().read(source, result);
            if (!java.contains("@Test")) {
                continue;
            }
            if (!usesDeclaredUiExecutionLayer(java)) {
                result.add(McpPackageViolation.blocker("ui-test.architecture.layer-delegation-missing",
                        relativeFile + " must delegate business interaction/assertion through service/page/actions/validation/component layers", source));
            }
            if (!reachesRealUiInteraction(context, java, result)) {
                result.add(McpPackageViolation.blocker("ui-test.architecture.ui-interaction-missing",
                        relativeFile + " extends BaseTestUI but does not reach a real UI action/page/component layer; do not sell service/model assertions as UI proof", source));
            }
        }
    }

    private boolean usesDeclaredUiExecutionLayer(String java) {
        return java.contains(".service.")
                || java.contains(".page.")
                || java.contains(".pages.")
                || java.contains(".actions.")
                || java.contains(".validation.")
                || java.contains(".validator.")
                || java.contains(".component.");
    }

    private boolean reachesRealUiInteraction(McpPackageContext context,
                                             String java,
                                             McpPackageValidationResult result) {
        if (UiArchitectureFiles.containsUiInteractionToken(java)) {
            return true;
        }
        if (context.sourceAuditScope().requiresExplicitSourceRoot()) {
            result.add(McpViolationBuilder.warning("ui-test.architecture.requires-source-root")
                    .message("Cannot follow imported UI action/page/component sources because package root is external and -Dmcp.project.root was not provided")
                    .path(context.packageRoot())
                    .meaning("MCP cannot prove real UI interaction through imports from the validator workspace")
                    .repair("rerun package audit with -Dmcp.project.root=<audited-project-root>")
                    .artifact("src/main/java imported UI classes")
                    .build());
            return true;
        }
        for (String importedClass : UiArchitectureFiles.importedProjectClasses(java)) {
            Path importedSource = context.sourceAuditScope().sourceRoot()
                    .resolve(McpProjectLayout.MAIN_JAVA_ROOT + "/" + importedClass.replace('.', '/') + ".java")
                    .normalize();
            if (!Files.isRegularFile(importedSource)) {
                continue;
            }
            String importedJava = context.files().read(importedSource, result);
            if (UiArchitectureFiles.containsUiInteractionToken(importedJava)) {
                return true;
            }
        }
        return false;
    }

    private void addSourceUnavailable(McpPackageContext context,
                                      McpPackageValidationResult result,
                                      String relativeFile,
                                      String sourceKind) {
        if (context.sourceAuditScope().canProveSourcePresence()) {
            result.add(McpPackageViolation.blocker("ui-test.architecture.source-missing",
                    "Diff-touched UI " + sourceKind + " does not exist under audited source root "
                            + context.sourceAuditScope().sourceRootDescription() + ": " + relativeFile,
                    context.packageRoot()));
        } else {
            result.add(McpViolationBuilder.warning("ui-test.architecture.requires-source-root")
                    .message("Cannot audit diff-touched UI " + sourceKind
                            + " because package root is external and -Dmcp.project.root was not provided: "
                            + relativeFile)
                    .path(context.packageRoot())
                    .meaning("MCP cannot prove real UI interaction from the validator workspace")
                    .repair("rerun package audit with -Dmcp.project.root=<audited-project-root>")
                    .artifact(relativeFile)
                    .build());
        }
    }
}
