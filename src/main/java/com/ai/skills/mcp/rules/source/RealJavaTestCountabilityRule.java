package com.ai.skills.mcp.rules.source;


import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpValidationRule;
import com.ai.skills.mcp.rules.McpRuleSupport;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.McpViolationBuilder;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.context.McpProjectLayout;
import com.ai.skills.mcp.context.McpRunArtifacts;
import com.ai.skills.mcp.validation.run.McpTestNgXmlMethods;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@McpContract(id = "run.countability.xml-to-java-test",
        category = McpRuleCategory.RUN,
        htmlReq = "execute-and-understand-run.html",
        artifact = "runs/run_##/validation/testng-results.xml")
public final class RealJavaTestCountabilityRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        for (Path runDirectory : context.files().runDirectories(context.paths().runsRoot(), result)) {
            validateRun(context, result, runDirectory);
        }
    }

    private void validateRun(McpPackageContext context, McpPackageValidationResult result, Path runDirectory) {
        Path xml = runDirectory.resolve(McpRunArtifacts.TESTNG_RESULTS);
        Path runState = runDirectory.resolve(McpRunArtifacts.STATE);
        Path commandResult = runDirectory.resolve(McpRunArtifacts.COMMAND_RESULT);
        boolean historicalHtmlExample = isHistoricalHtmlExamplePackage(context.packageRoot());
        if (!Files.isRegularFile(xml) || !Files.isRegularFile(runState)) {
            return;
        }

        String xmlText = context.files().read(xml, result);
        String stateText = context.files().read(runState, result);
        int testsProduced = McpRuleSupport.firstIntValue(stateText, "tests_produced", "test_count", "tests") == null
                ? -1
                : McpRuleSupport.firstIntValue(stateText, "tests_produced", "test_count", "tests");
        Map<String, Set<String>> passedMethodsByClass = McpTestNgXmlMethods.passedRealMethodsByClass(xmlText);
        int passedMethodCount = passedMethodsByClass.values().stream().mapToInt(Set::size).sum();

        if (passedMethodCount == 0) {
            result.add(McpPackageViolation.blocker("real-test-countability.xml-methods-missing",
                    "Run XML must list real passed TestNG @Test method entries, not config methods or only counters",
                    xml));
        }
        if (testsProduced >= 0 && passedMethodCount != testsProduced) {
            result.add(McpPackageViolation.blocker("real-test-countability.count-drift",
                    "run-state tests_produced must match passed XML test-method count: tests_produced="
                            + testsProduced + ", xml_passed_methods=" + passedMethodCount,
                    runState));
        }

        for (Map.Entry<String, Set<String>> entry : passedMethodsByClass.entrySet()) {
            Path source = context.sourceAuditScope().sourceRoot()
                    .resolve(McpProjectLayout.TEST_JAVA_ROOT)
                    .resolve(entry.getKey().replace('.', '/') + ".java")
                    .normalize();
            if (!Files.isRegularFile(source)) {
                if (context.sourceAuditScope().canProveSourcePresence()) {
                    result.add(McpPackageViolation.blocker("real-test-countability.source-missing",
                            "XML references a TestNG class that does not exist under audited source root "
                                    + context.sourceAuditScope().sourceRootDescription() + ": " + entry.getKey(),
                            xml));
                } else {
                    result.add(McpViolationBuilder.warning("real-test-countability.requires-source-root")
                            .message("Cannot prove XML TestNG class source because package root is external and -Dmcp.project.root was not provided: "
                                    + entry.getKey())
                            .path(xml)
                            .meaning("MCP cannot compare XML methods with Java @Test methods without the audited project source root")
                            .repair("rerun package audit with -Dmcp.project.root=<audited-project-root>")
                            .artifact("runs/*/validation/testng-results.xml + src/test/java")
                            .build());
                }
                continue;
            }
            String sourceText = context.files().read(source, result);
            for (String method : entry.getValue()) {
                if (!isAnnotatedTestMethod(sourceText, method)) {
                    if (historicalHtmlExample) {
                        result.add(McpViolationBuilder.warning("real-test-countability.historical-source-drift")
                                .message("Historical html-ex XML references a method that is not present in the current live source root: "
                                        + entry.getKey() + "#" + method)
                                .path(source)
                                .meaning("The saved html-ex package is a frozen artifact, while -Dmcp.project.root points at source code that may have been refactored later")
                                .repair("validate historical examples with the html-ex corpus/candidate profile, or provide the matching source snapshot when strict source countability is required")
                                .artifact("runs/*/validation/testng-results.xml + src/test/java source snapshot")
                                .build());
                    } else {
                        result.add(McpPackageViolation.blocker("real-test-countability.method-missing",
                                "XML passed method is not a real Java @Test method: "
                                        + entry.getKey() + "#" + method,
                                source));
                    }
                }
            }
        }

        String command = context.files().readIfRegular(commandResult, result).toLowerCase();
        if (!command.contains("mvn") && !command.contains("maven") && !command.contains("surefire")) {
            result.add(McpPackageViolation.blocker("real-test-countability.maven-proof-missing",
                    "Run command-result.md must show Maven/Surefire execution proof for the counted tests",
                    commandResult));
        }
    }

    private boolean isHistoricalHtmlExamplePackage(Path packageRoot) {
        String normalized = packageRoot.toAbsolutePath().normalize().toString()
                .replace('\\', '/')
                .toLowerCase();
        return normalized.contains("/mcp/html-ex/");
    }

    private boolean isAnnotatedTestMethod(String sourceText, String methodName) {
        Pattern methodPattern = Pattern.compile("(?s)@Test(?:\\s*\\([^)]*\\))?\\s+public\\s+void\\s+"
                + Pattern.quote(methodName) + "\\s*\\(");
        return methodPattern.matcher(sourceText).find();
    }
}
