package com.ai.skills.mcp.validation.diagnostics;

import com.ai.skills.mcp.McpPackageViolation;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class McpRuleResult {
    private final String title;
    private final Path packageRoot;
    private final List<String> details;
    private final List<McpPackageViolation> blockers;
    private final List<McpPackageViolation> warnings;

    private McpRuleResult(String title,
                          Path packageRoot,
                          List<String> details,
                          List<McpPackageViolation> blockers,
                          List<McpPackageViolation> warnings) {
        this.title = title;
        this.packageRoot = packageRoot;
        this.details = List.copyOf(details);
        this.blockers = List.copyOf(blockers);
        this.warnings = List.copyOf(warnings);
    }

    public static McpRuleResult pass(String title,
                                     Path packageRoot,
                                     List<String> details,
                                     List<McpPackageViolation> warnings) {
        return new McpRuleResult(title, packageRoot, details, List.of(), warnings);
    }

    public static McpRuleResult fail(String title,
                                     Path packageRoot,
                                     List<String> details,
                                     List<McpPackageViolation> blockers,
                                     List<McpPackageViolation> warnings) {
        return new McpRuleResult(title, packageRoot, details, blockers, warnings);
    }

    public boolean passed() {
        return blockers.isEmpty();
    }

    public String status() {
        return passed() ? "PASS" : "FAIL";
    }

    public String evidenceArtifact() {
        return details.stream()
                .filter(detail -> detail != null && !detail.isBlank())
                .filter(detail -> startsWithAny(detail,
                        "artifact:", "artifacts:", "relative path", "relative paths:",
                        "source truth:", "discovery artifact:", "discovery artifacts:"))
                .findFirst()
                .orElse("see package root and MCP rule metadata");
    }

    public String coldReason() {
        if (!passed()) {
            return blockers.size() + " blocker(s) violate the contract for " + title + ".";
        }
        if (!warnings.isEmpty()) {
            return "contract passed with " + warnings.size() + " warning(s); keep calibration visible.";
        }
        return "no blocker violations for " + title + ".";
    }

    public String repairHint() {
        return passed()
                ? "No repair required now; rerun the MCP package close-out gate after any report, state, diff, CSS, or proof edit."
                : "Repair listed blockers, regenerate affected artifacts, then rerun the MCP package close-out gate.";
    }

    public static boolean startsWithAny(String value, String... prefixes) {
        if (value == null) {
            return false;
        }
        String lower = value.trim().toLowerCase(Locale.ROOT);
        for (String prefix : prefixes) {
            if (lower.startsWith(prefix.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
