package com.ai.skills.mcp.validation;

import com.ai.core.reporting.StepLogger;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.validation.diagnostics.McpDiagnostic;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class McpNegativeScenarioValidation {
    private McpNegativeScenarioValidation() {
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
        boolean expectsCodeAbsence = expected.toLowerCase(Locale.ROOT).contains("expected invariant: no ");
        List<McpPackageViolation> targetViolations = result.violations().stream()
                .filter(violation -> expectedCodes.contains(violation.code()))
                .collect(Collectors.toList());
        boolean scenarioSatisfied = expectsCodeAbsence
                ? targetViolations.isEmpty()
                : expectedCodes.stream().allMatch(code -> McpViolationValidation.hasCode(result, code));

        McpDiagnostic.builder(title)
                .packageRoot(packageRoot)
                .htmlOwner("linked by MCP rule metadata / expected failure code")
                .profile("negative fixture contract")
                .requiredArtifacts(cleanArtifactLabel(artifact))
                .expectedInvariant(expected)
                .status(expectsCodeAbsence ? "expected non-violation" : "expected rejection")
                .violations(targetViolations)
                .evidenceArtifact(cleanArtifactLabel(artifact))
                .coldReason(coldReason(expectsCodeAbsence, scenarioSatisfied))
                .repairHint(repairHint(expectsCodeAbsence, scenarioSatisfied))
                .build()
                .log();

        StepLogger.logDetail("Validator total blockers: " + result.blockerCount());
        StepLogger.logDetail("Validator total warnings: " + result.warningCount());
        for (String code : expectedCodes) {
            StepLogger.logDetail((expectsCodeAbsence ? "Forbidden" : "Expected")
                    + " code observed [" + code + "]: "
                    + (McpViolationValidation.hasCode(result, code) ? "yes" : "no"));
        }
        targetViolations.forEach(violation -> logObservedViolation(packageRoot, artifact, violation));
        int unrelated = result.violations().size() - targetViolations.size();
        if (unrelated > 0) {
            StepLogger.logDetail("Collateral fixture violations summarized, not expanded: " + unrelated);
            StepLogger.logDetail("Reason: this negative test proves the targeted code path, not the full package health.");
        }
    }

    private static String coldReason(boolean expectsCodeAbsence, boolean scenarioSatisfied) {
        if (expectsCodeAbsence) {
            return scenarioSatisfied
                    ? "validator ignored the documentation-only mention and did not raise the forbidden code."
                    : "validator raised a forbidden code for documentation-only text.";
        }
        return scenarioSatisfied
                ? "validator rejected the intentional mutation through the expected failure code(s)."
                : "validator did not expose every expected failure code.";
    }

    private static String repairHint(boolean expectsCodeAbsence, boolean scenarioSatisfied) {
        if (expectsCodeAbsence) {
            return scenarioSatisfied
                    ? "No repair required; keep annotation scanning comment-safe."
                    : "Tighten Java annotation scanning so comments/imports/string literals do not trigger blockers.";
        }
        return scenarioSatisfied
                ? "No repair required for the fixture; keep the production package away from this mutation."
                : "Tighten the MCP rule or the negative fixture until the expected code is visible.";
    }

    private static void logObservedViolation(Path packageRoot, String assertedArtifact, McpPackageViolation violation) {
        StepLogger.logDetail("Observed violation [" + violation.code() + "]: " + violation.message());
        StepLogger.logDetail("Observed target: " + cleanArtifactLabel(assertedArtifact));
        if (violation.path() != null) {
            StepLogger.logDetail("Validator artifact scope: " + safeRelative(packageRoot, violation.path()));
        }
    }

    private static String cleanArtifactLabel(String artifact) {
        return artifact.replaceFirst("^Asserted artifacts?:\\s*", "");
    }

    private static String safeRelative(Path root, Path path) {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path normalizedPath = path.toAbsolutePath().normalize();
        return normalizedPath.startsWith(normalizedRoot)
                ? normalizedRoot.relativize(normalizedPath).toString()
                : normalizedPath.toString();
    }
}
