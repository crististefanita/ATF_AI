package com.endava.ai.skills.mcp.validation;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.validation.diagnostics.McpBlockerRootCause;
import com.endava.ai.skills.mcp.validation.diagnostics.McpDiagnostic;
import com.endava.ai.skills.mcp.validation.diagnostics.McpRuleResult;

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
        McpDiagnostic.builder(result.title())
                .packageRoot(result.packageRoot())
                .requiredArtifacts(result.evidenceArtifact())
                .expectedInvariant(expectedInvariant(result))
                .status(result.status())
                .blockers(result.blockers())
                .warnings(result.warnings())
                .evidenceArtifact(result.evidenceArtifact())
                .coldReason(result.coldReason())
                .repairHint(result.repairHint())
                .build()
                .log();
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

    private static String expectedInvariant(McpRuleResult result) {
        List<String> matching = result.details().stream()
                .filter(detail -> detail != null && !detail.isBlank())
                .filter(detail -> startsWithAny(detail,
                        "asserts:", "expected:", "forbidden:", "target", "runtime keys:", "runtime values:"))
                .collect(Collectors.toList());
        if (matching.isEmpty()) {
            return result.title();
        }
        return String.join(" | ", matching);
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

    private static void logBlockerSection(Path packageRoot, List<McpPackageViolation> blockers) {
        StepLogger.startStep("Observed blockers");
        StepLogger.logDetail("Blockers found: " + blockers.size());
        StepLogger.logDetail("Primary repair focus: " + McpBlockerRootCause.primaryFocus(blockers));
        McpBlockerRootCause.summarize(blockers).forEach(summary ->
                StepLogger.logDetail("Root-cause group: " + summary.key()
                        + " = " + summary.count()
                        + " | " + summary.label()
                        + " | repair: " + summary.repair()));
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
        if (blocker.failureMeaning() != null && !blocker.failureMeaning().isBlank()) {
            StepLogger.logDetail("Failure meaning: " + blocker.failureMeaning());
        }
        if (blocker.repairAction() != null && !blocker.repairAction().isBlank()) {
            StepLogger.logDetail("Repair action: " + blocker.repairAction());
        }
        if (blocker.artifactToRegenerate() != null && !blocker.artifactToRegenerate().isBlank()) {
            StepLogger.logDetail("Artifact to regenerate: " + blocker.artifactToRegenerate());
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
