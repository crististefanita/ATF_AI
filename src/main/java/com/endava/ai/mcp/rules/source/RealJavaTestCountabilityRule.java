package com.endava.ai.mcp.rules.source;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.context.McpProjectLayout;
import com.endava.ai.mcp.context.McpRunArtifacts;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@com.endava.ai.mcp.contract.McpContract(id = "run.countability.xml-to-java-test",
        category = com.endava.ai.mcp.contract.McpRuleCategory.RUN,
        htmlReq = "execute-and-understand-run.html",
        artifact = "runs/run_##/validation/testng-results.xml")
public final class RealJavaTestCountabilityRule implements McpValidationRule {
    private static final Pattern CLASS_BLOCK = Pattern.compile(
            "(?is)<class\\s+[^>]*name=[\"']([^\"']+)[\"'][^>]*>(.*?)</class>");
    private static final Pattern PASSED_METHOD = Pattern.compile(
            "(?is)<test-method\\s+[^>]*name=[\"']([^\"']+)[\"'][^>]*status=[\"']PASS[\"']");

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
        if (!Files.isRegularFile(xml) || !Files.isRegularFile(runState)) {
            return;
        }

        String xmlText = context.files().read(xml, result);
        String stateText = context.files().read(runState, result);
        int testsProduced = McpRuleSupport.firstIntValue(stateText, "tests_produced", "test_count", "tests") == null
                ? -1
                : McpRuleSupport.firstIntValue(stateText, "tests_produced", "test_count", "tests");
        Map<String, Set<String>> passedMethodsByClass = passedMethodsByClass(xmlText);
        int passedMethodCount = passedMethodsByClass.values().stream().mapToInt(Set::size).sum();

        if (passedMethodCount == 0) {
            result.add(McpPackageViolation.blocker("real-test-countability.xml-methods-missing",
                    "Run XML must list real passed TestNG test-method entries, not only total/passed counters",
                    xml));
        }
        if (testsProduced >= 0 && passedMethodCount != testsProduced) {
            result.add(McpPackageViolation.blocker("real-test-countability.count-drift",
                    "run-state tests_produced must match passed XML test-method count: tests_produced="
                            + testsProduced + ", xml_passed_methods=" + passedMethodCount,
                    runState));
        }

        for (Map.Entry<String, Set<String>> entry : passedMethodsByClass.entrySet()) {
            Path source = context.projectRoot()
                    .resolve(McpProjectLayout.TEST_JAVA_ROOT)
                    .resolve(entry.getKey().replace('.', '/') + ".java")
                    .normalize();
            if (!Files.isRegularFile(source)) {
                result.add(McpPackageViolation.blocker("real-test-countability.source-missing",
                        "XML references a TestNG class that does not exist in src/test/java: " + entry.getKey(),
                        xml));
                continue;
            }
            String sourceText = context.files().read(source, result);
            for (String method : entry.getValue()) {
                if (!isAnnotatedTestMethod(sourceText, method)) {
                    result.add(McpPackageViolation.blocker("real-test-countability.method-missing",
                            "XML passed method is not a real Java @Test method: "
                                    + entry.getKey() + "#" + method,
                            source));
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

    private Map<String, Set<String>> passedMethodsByClass(String xmlText) {
        Map<String, Set<String>> methodsByClass = new LinkedHashMap<>();
        Matcher classMatcher = CLASS_BLOCK.matcher(xmlText);
        while (classMatcher.find()) {
            String className = classMatcher.group(1);
            String classBody = classMatcher.group(2);
            Matcher methodMatcher = PASSED_METHOD.matcher(classBody);
            while (methodMatcher.find()) {
                methodsByClass.computeIfAbsent(className, ignored -> new LinkedHashSet<>())
                        .add(methodMatcher.group(1));
            }
        }
        return methodsByClass;
    }

    private boolean isAnnotatedTestMethod(String sourceText, String methodName) {
        Pattern methodPattern = Pattern.compile("(?s)@Test(?:\\s*\\([^)]*\\))?\\s+public\\s+void\\s+"
                + Pattern.quote(methodName) + "\\s*\\(");
        return methodPattern.matcher(sourceText).find();
    }
}
