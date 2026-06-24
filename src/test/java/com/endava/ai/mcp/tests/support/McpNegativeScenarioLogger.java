package com.endava.ai.mcp.tests.support;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.validation.McpDiagnostic;

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
        StepLogger.startStep("Fixture mutation");
        StepLogger.logDetail("Fixture package root: " + packageRoot);
        StepLogger.logDetail("Mutation: " + mutation);
        StepLogger.pass("Mutation recorded");

        List<String> expectedCodes = Pattern.compile("[a-z]+(?:[-.][a-z0-9]+)+")
                .matcher(expected)
                .results()
                .map(match -> match.group())
                .collect(Collectors.toList());
        List<McpPackageViolation> targetViolations = result.violations().stream()
                .filter(violation -> expectedCodes.contains(violation.code()))
                .collect(Collectors.toList());

        McpDiagnostic.builder(title)
                .packageRoot(packageRoot)
                .htmlOwner("linked by MCP rule metadata / expected failure code")
                .profile("negative fixture contract")
                .requiredArtifacts(artifact.replaceFirst("^Asserted artifacts?:\\s*", ""))
                .expectedInvariant(expected)
                .status("expected rejection")
                .violations(result.violations())
                .evidenceArtifact(artifact.replaceFirst("^Asserted artifacts?:\\s*", ""))
                .coldReason(expectedCodes.stream().allMatch(code -> McpViolationAssert.hasCode(result, code))
                        ? "validator rejected the intentional mutation through the expected failure code(s)."
                        : "validator did not expose every expected failure code.")
                .repairHint(expectedCodes.stream().allMatch(code -> McpViolationAssert.hasCode(result, code))
                        ? "No repair required for the fixture; keep the production package away from this mutation."
                        : "Tighten the MCP rule or the negative fixture until the expected code is visible.")
                .build()
                .log();

        expectedCodes.forEach(code -> StepLogger.logDetail("Expected code observed [" + code + "]: "
                + (McpViolationAssert.hasCode(result, code) ? "yes" : "no")));
        targetViolations.forEach(violation -> logObservedViolation(packageRoot, artifact, violation));
        int unrelated = result.violations().size() - targetViolations.size();
        if (unrelated > 0) {
            StepLogger.logDetail("Other fixture violations hidden from this negative-test report: " + unrelated);
            StepLogger.logDetail("Reason: this test proves the expected mutation contract, not the full package health.");
        }
    }

    private static void logObservedViolation(Path packageRoot, String assertedArtifact, McpPackageViolation violation) {
        StepLogger.logDetail("Observed violation [" + violation.code() + "]: " + violation.message());
        StepLogger.logDetail("Observed target: " + assertedArtifact.replaceFirst("^Asserted artifacts?:\\s*", ""));
        if (violation.path() != null) {
            StepLogger.logDetail("Validator artifact scope: " + safeRelative(packageRoot, violation.path()));
        }
    }

    private static String safeRelative(Path root, Path path) {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path normalizedPath = path.toAbsolutePath().normalize();
        return normalizedPath.startsWith(normalizedRoot)
                ? normalizedRoot.relativize(normalizedPath).toString()
                : normalizedPath.toString();
    }
}
