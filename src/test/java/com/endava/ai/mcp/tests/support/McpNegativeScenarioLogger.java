package com.endava.ai.mcp.tests.support;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.McpViolationSeverity;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class McpNegativeScenarioLogger {
    private McpNegativeScenarioLogger() {
    }

    public static void logValidatorScenario(String title,
                                            Path packageRoot,
                                            String mutation,
                                            String expected,
                                            String artifact,
                                            McpPackageValidationResult result) {
        StepLogger.startStep(title);
        StepLogger.logDetail("Validation axis: " + title);
        StepLogger.logDetail("Outcome: expected rejection");

        StepLogger.startStep("Fixture mutation");
        StepLogger.logDetail("Fixture package root: " + packageRoot);
        StepLogger.logDetail("Mutation: " + mutation);
        StepLogger.pass("Mutation recorded");

        StepLogger.startStep("Expected contract");
        StepLogger.logDetail("HTML owner: linked by MCP rule metadata / expected failure code.");
        StepLogger.logDetail("MCP profile: negative fixture contract.");
        StepLogger.logDetail("Required artifacts: " + artifact.replaceFirst("^Asserted artifacts?:\\s*", ""));
        StepLogger.logDetail("Expected invariant: " + expected);
        List<String> expectedCodes = Pattern.compile("[a-z]+(?:[-.][a-z0-9]+)+")
                .matcher(expected)
                .results()
                .map(match -> match.group())
                .collect(Collectors.toList());
        expectedCodes.forEach(code -> StepLogger.logDetail("Expected code observed [" + code + "]: "
                + (McpViolationAssert.hasCode(result, code) ? "yes" : "no")));
        StepLogger.pass("Expected contract recorded");

        StepLogger.startStep("Observed validator result");
        StepLogger.logDetail("Package: " + packageRoot);
        StepLogger.logDetail("Rule: " + title);
        StepLogger.logDetail("Status: expected rejection");
        StepLogger.logDetail("Blockers: " + countSeverity(result, McpViolationSeverity.BLOCKER));
        StepLogger.logDetail("Warnings: " + countSeverity(result, McpViolationSeverity.WARNING));
        StepLogger.logDetail("Evidence artifact: " + artifact.replaceFirst("^Asserted artifacts?:\\s*", ""));
        List<McpPackageViolation> targetViolations = result.violations().stream()
                .filter(violation -> expectedCodes.contains(violation.code()))
                .collect(Collectors.toList());
        targetViolations.forEach(violation -> logObservedViolation(packageRoot, artifact, violation));
        int unrelated = result.violations().size() - targetViolations.size();
        if (unrelated > 0) {
            StepLogger.logDetail("Other fixture violations hidden from this negative-test report: " + unrelated);
            StepLogger.logDetail("Reason: this test proves the expected mutation contract, not the full package health.");
        }
        StepLogger.pass("Observed validator result recorded");

        StepLogger.startStep("Cold verdict");
        boolean passed = expectedCodes.stream().allMatch(code -> McpViolationAssert.hasCode(result, code));
        StepLogger.logDetail("PASS / FAIL: " + (passed ? "PASS" : "FAIL"));
        StepLogger.logDetail("Why: " + (passed
                ? "validator rejected the intentional mutation through the expected failure code(s)."
                : "validator did not expose every expected failure code."));
        StepLogger.pass("Cold verdict recorded");

        StepLogger.startStep("Repair hint");
        StepLogger.logDetail(passed
                ? "No repair required for the fixture; keep the production package away from this mutation."
                : "Tighten the MCP rule or the negative fixture until the expected code is visible.");
        StepLogger.pass("Repair hint recorded");
    }

    private static void logObservedViolation(Path packageRoot, String assertedArtifact, McpPackageViolation violation) {
        StepLogger.logDetail("Observed violation [" + violation.code() + "]: " + violation.message());
        StepLogger.logDetail("Observed target: " + assertedArtifact.replaceFirst("^Asserted artifacts?:\\s*", ""));
        if (violation.path() != null) {
            StepLogger.logDetail("Validator artifact scope: " + safeRelative(packageRoot, violation.path()));
        }
    }

    private static long countSeverity(McpPackageValidationResult result, McpViolationSeverity severity) {
        return result.violations().stream()
                .filter(violation -> violation.severity() == severity)
                .count();
    }

    private static String safeRelative(Path root, Path path) {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path normalizedPath = path.toAbsolutePath().normalize();
        return normalizedPath.startsWith(normalizedRoot)
                ? normalizedRoot.relativize(normalizedPath).toString()
                : normalizedPath.toString();
    }
}
