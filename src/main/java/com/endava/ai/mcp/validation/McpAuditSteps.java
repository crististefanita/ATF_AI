package com.endava.ai.mcp.validation;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.mcp.McpPackageViolation;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class McpAuditSteps {
    private static final int MAX_VISIBLE_BLOCKERS = 12;

    private McpAuditSteps() {
    }

    static void pass(String title, Path packageRoot, List<String> details) {
        pass(title, packageRoot, details, List.of());
    }

    static void pass(String title,
                     Path packageRoot,
                     List<String> details,
                     List<McpPackageViolation> warnings) {
        log(McpRuleResult.pass(title, packageRoot, details, warnings));
    }

    static void fail(String title, Path packageRoot, List<String> details, List<McpPackageViolation> blockers) {
        fail(title, packageRoot, details, blockers, List.of());
    }

    static void fail(String title,
                     Path packageRoot,
                     List<String> details,
                     List<McpPackageViolation> blockers,
                     List<McpPackageViolation> warnings) {
        log(McpRuleResult.fail(title, packageRoot, details, blockers, warnings));
    }

    private static void log(McpRuleResult result) {
        StepLogger.startStep(result.title());
        StepLogger.logDetail("Validation axis: " + result.title());
        StepLogger.logDetail("Outcome: " + result.status());
        logExpectedContract(result);
        logObservedValidatorResult(result);
        logColdVerdict(result);
        logRepairHint(result);
        if (result.passed()) {
            StepLogger.pass("MCP audit passed: " + result.title());
            return;
        }
        logBlockerSection(result.packageRoot(), result.blockers());
        StepLogger.fail("MCP audit failed: " + result.title() + " [" + result.blockers().size() + " blocker(s)]",
                result.blockers().stream()
                        .map(McpPackageViolation::toString)
                        .collect(Collectors.joining(System.lineSeparator())));
    }

    private static void logExpectedContract(McpRuleResult result) {
        StepLogger.startStep("Expected contract");
        StepLogger.logDetail("HTML owner: linked by MCP rule metadata / HTML-req for this validation axis.");
        StepLogger.logDetail("MCP profile: latest generated package contract axis.");
        logGroupedDetails("Required artifacts", result.details(),
                "artifact:", "artifacts:", "relative path", "relative paths:",
                "required", "required package spine:", "required run spine:",
                "source truth:", "discovery artifact:", "discovery artifacts:");
        logGroupedDetails("Expected invariant", result.details(),
                "asserts:", "expected:", "forbidden:", "target", "runtime keys:", "runtime values:");
        if (result.details().stream().noneMatch(detail -> containsAny(detail, "asserts:", "expected:", "forbidden:"))) {
            StepLogger.logDetail("Expected invariant: " + result.title());
        }
        StepLogger.pass("Expected contract recorded");
    }

    private static void logObservedValidatorResult(McpRuleResult result) {
        StepLogger.startStep("Observed validator result");
        StepLogger.logDetail("Package: " + result.packageRoot());
        StepLogger.logDetail("Rule: " + result.title());
        StepLogger.logDetail("Status: " + result.status());
        StepLogger.logDetail("Blockers: " + result.blockers().size());
        StepLogger.logDetail("Warnings: " + result.warnings().size());
        StepLogger.logDetail("Evidence artifact: " + result.evidenceArtifact());
        StepLogger.pass("Observed validator result recorded");
    }

    private static void logColdVerdict(McpRuleResult result) {
        StepLogger.startStep("Cold verdict");
        StepLogger.logDetail("PASS / FAIL: " + result.status());
        StepLogger.logDetail("Why: " + result.coldReason());
        StepLogger.pass("Cold verdict recorded");
    }

    private static void logRepairHint(McpRuleResult result) {
        StepLogger.startStep("Repair hint");
        StepLogger.logDetail(result.repairHint());
        StepLogger.pass("Repair hint recorded");
    }

    private static void logGroupedDetails(String label, List<String> details, String... needles) {
        List<String> matching = details.stream()
                .filter(detail -> detail != null && !detail.isBlank())
                .filter(detail -> startsWithAny(detail, needles))
                .collect(Collectors.toList());
        if (matching.isEmpty()) {
            StepLogger.logDetail(label + ": not explicitly declared by this test; see validation axis.");
            return;
        }
        matching.forEach(detail -> StepLogger.logDetail(label + ": " + detail));
    }

    private static boolean startsWithAny(String value, String... prefixes) {
        if (value == null) {
            return false;
        }
        String lower = value.trim().toLowerCase(java.util.Locale.ROOT);
        for (String prefix : prefixes) {
            if (lower.startsWith(prefix.toLowerCase(java.util.Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAny(String value, String... needles) {
        if (value == null) {
            return false;
        }
        String lower = value.toLowerCase(java.util.Locale.ROOT);
        for (String needle : needles) {
            if (lower.contains(needle.toLowerCase(java.util.Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static void logBlockerSection(Path packageRoot, List<McpPackageViolation> blockers) {
        StepLogger.startStep("Observed blockers");
        StepLogger.logDetail("Blockers found: " + blockers.size());
        summarizeByCode(blockers).forEach((code, count) ->
                StepLogger.logDetail("Blocker group: " + code + " = " + count));

        blockers.stream()
                .limit(MAX_VISIBLE_BLOCKERS)
                .forEach(blocker -> logBlocker(packageRoot, blocker));
        if (blockers.size() > MAX_VISIBLE_BLOCKERS) {
            StepLogger.logDetail("More blockers hidden from inline list: "
                    + (blockers.size() - MAX_VISIBLE_BLOCKERS)
                    + ". Full list is preserved in the code block below.");
        }

        String fullBlockerList = blockers.stream()
                .map(McpPackageViolation::toString)
                .collect(Collectors.joining(System.lineSeparator()));
        StepLogger.logCodeBlock(fullBlockerList);
        StepLogger.pass("Recorded blocker details");
    }

    private static Map<String, Long> summarizeByCode(List<McpPackageViolation> blockers) {
        return blockers.stream()
                .collect(Collectors.groupingBy(
                        McpPackageViolation::code,
                        java.util.LinkedHashMap::new,
                        Collectors.counting()));
    }

    private static void logBlocker(Path packageRoot, McpPackageViolation blocker) {
        StepLogger.logDetail("BLOCKER [" + blocker.code() + "]: " + blocker.message());
        if (blocker.path() != null) {
            StepLogger.logDetail("Artifact: " + relativize(packageRoot, blocker.path()));
        }
    }

    private static String relativize(Path packageRoot, Path path) {
        Path absolutePackage = packageRoot.toAbsolutePath().normalize();
        Path absolutePath = path.toAbsolutePath().normalize();
        try {
            if (absolutePath.startsWith(absolutePackage)) {
                String relative = absolutePackage.relativize(absolutePath).toString().replace('\\', '/');
                return relative.isBlank() ? "." : relative;
            }
        } catch (IllegalArgumentException ignored) {
            // Different roots; fall through to the absolute path.
        }
        return absolutePath.toString().replace('\\', '/');
    }
}
