package com.endava.ai.skills.mcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class McpPackageValidationResult {
    private final List<McpPackageViolation> violations = new ArrayList<>();

    public void add(McpPackageViolation violation) {
        violations.add(violation);
    }

    public List<McpPackageViolation> violations() {
        return Collections.unmodifiableList(violations);
    }

    public boolean hasBlockers() {
        return violations.stream().anyMatch(violation -> violation.severity() == McpViolationSeverity.BLOCKER);
    }

    public long blockerCount() {
        return count(McpViolationSeverity.BLOCKER);
    }

    public long warningCount() {
        return count(McpViolationSeverity.WARNING);
    }

    public long recommendationCount() {
        return count(McpViolationSeverity.RECOMMENDATION);
    }

    private long count(McpViolationSeverity severity) {
        return violations.stream().filter(violation -> violation.severity() == severity).count();
    }
}
