package com.endava.ai.skills.mcp.validation;

import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.McpViolationSeverity;
import com.endava.ai.skills.mcp.validation.diagnostics.McpViolationFormatter;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class McpAuditViolationAssertions {
    private final McpPackageAuditContext context;

    McpAuditViolationAssertions(McpPackageAuditContext context) {
        this.context = context;
    }

    void assertNoBlockers(String axis, Predicate<McpPackageViolation> filter, List<String> details) {
        List<McpPackageViolation> matching = context.result().violations().stream()
                .filter(filter)
                .collect(Collectors.toList());
        List<McpPackageViolation> blockers = matching.stream()
                .filter(violation -> violation.severity() == McpViolationSeverity.BLOCKER)
                .collect(Collectors.toList());
        List<McpPackageViolation> warnings = matching.stream()
                .filter(violation -> violation.severity() == McpViolationSeverity.WARNING)
                .collect(Collectors.toList());
        if (blockers.isEmpty()) {
            McpAuditSteps.pass(axis, context.packageRoot(), details, warnings);
            return;
        }

        McpAuditSteps.fail(axis, context.packageRoot(), details, blockers, warnings);
        throw new AssertionError(McpViolationFormatter.format(context.packageRoot(), blockers));
    }

    void assertNoSyntheticViolations(String axis,
                                     List<String> messages,
                                     Path path,
                                     String code,
                                     List<String> details) {
        if (messages.isEmpty()) {
            McpAuditSteps.pass(axis, context.packageRoot(), details);
            return;
        }

        List<McpPackageViolation> blockers = messages.stream()
                .map(message -> McpPackageViolation.blocker(code, message, path))
                .collect(Collectors.toList());
        assertNoViolations(axis, blockers, details);
    }

    void assertNoViolations(String axis, List<McpPackageViolation> blockers, List<String> details) {
        if (blockers.isEmpty()) {
            McpAuditSteps.pass(axis, context.packageRoot(), details);
            return;
        }

        McpAuditSteps.fail(axis, context.packageRoot(), details, blockers);
        throw new AssertionError(McpViolationFormatter.format(context.packageRoot(), blockers));
    }
}
