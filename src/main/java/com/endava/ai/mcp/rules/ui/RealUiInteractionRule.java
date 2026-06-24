package com.endava.ai.mcp.rules.ui;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.context.McpProjectLayout;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;

@com.endava.ai.mcp.contract.McpContract(id = "ui.test-architecture.real-ui-interaction",
        category = com.endava.ai.mcp.contract.McpRuleCategory.UI_WRAPPER,
        htmlReq = "ui-business-frontier-adapter.html",
        artifact = "src/test/java/**/ui/**/*Test.java")
public final class RealUiInteractionRule implements McpValidationRule {
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
        for (String importedClass : UiArchitectureFiles.importedProjectClasses(java)) {
            Path importedSource = context.projectRoot()
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
}
