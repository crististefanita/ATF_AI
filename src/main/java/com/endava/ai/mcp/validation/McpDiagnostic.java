package com.endava.ai.mcp.validation;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.McpViolationSeverity;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Canonical Extent-facing explanation for one MCP validation axis.
 * Keep this object small: it teaches the agent what contract was checked,
 * what was observed, and what must be repaired without duplicating prose in tests.
 */
public final class McpDiagnostic {
    private final String title;
    private final Path packageRoot;
    private final String htmlOwner;
    private final String profile;
    private final String requiredArtifacts;
    private final String expectedInvariant;
    private final String status;
    private final List<McpPackageViolation> blockers;
    private final List<McpPackageViolation> warnings;
    private final String evidenceArtifact;
    private final String coldReason;
    private final String repairHint;

    private McpDiagnostic(Builder builder) {
        this.title = require(builder.title, "title");
        this.packageRoot = builder.packageRoot;
        this.htmlOwner = defaultText(builder.htmlOwner, "linked by MCP rule metadata / HTML-req for this validation axis");
        this.profile = defaultText(builder.profile, "latest generated package contract axis");
        this.requiredArtifacts = defaultText(builder.requiredArtifacts, "see package root and MCP rule metadata");
        this.expectedInvariant = defaultText(builder.expectedInvariant, title);
        this.status = defaultText(builder.status, builder.blockers.isEmpty() ? "PASS" : "FAIL");
        this.blockers = List.copyOf(builder.blockers);
        this.warnings = List.copyOf(builder.warnings);
        this.evidenceArtifact = defaultText(builder.evidenceArtifact, requiredArtifacts);
        this.coldReason = defaultText(builder.coldReason, defaultColdReason());
        this.repairHint = defaultText(builder.repairHint, defaultRepairHint());
    }

    public static Builder builder(String title) {
        return new Builder(title);
    }

    public void log() {
        StepLogger.startStep(title);
        StepLogger.logDetail("Validation axis: " + title);
        StepLogger.logDetail("Outcome: " + status);

        StepLogger.startStep("Expected contract");
        StepLogger.logDetail("HTML owner: " + htmlOwner);
        StepLogger.logDetail("MCP profile: " + profile);
        StepLogger.logDetail("Required artifacts: " + requiredArtifacts);
        StepLogger.logDetail("Expected invariant: " + expectedInvariant);
        StepLogger.pass("Expected contract recorded");

        StepLogger.startStep("Observed validator result");
        if (packageRoot != null) {
            StepLogger.logDetail("Package: " + packageRoot);
        }
        StepLogger.logDetail("Rule: " + title);
        StepLogger.logDetail("Status: " + status);
        StepLogger.logDetail("Blockers: " + blockers.size());
        StepLogger.logDetail("Warnings: " + warnings.size());
        StepLogger.logDetail("Evidence artifact: " + evidenceArtifact);
        blockers.forEach(violation -> StepLogger.logDetail(violation.toString()));
        StepLogger.pass("Observed validator result recorded");

        StepLogger.startStep("Cold verdict");
        StepLogger.logDetail("PASS / FAIL: " + status);
        StepLogger.logDetail("Why: " + coldReason);
        StepLogger.pass("Cold verdict recorded");

        StepLogger.startStep("Repair hint");
        StepLogger.logDetail(repairHint);
        StepLogger.pass("Repair hint recorded");
    }

    public List<McpPackageViolation> blockers() {
        return blockers;
    }

    public List<McpPackageViolation> warnings() {
        return warnings;
    }

    private String defaultColdReason() {
        if (!blockers.isEmpty()) {
            return blockers.size() + " blocker(s) violate the contract for " + title + ".";
        }
        if (!warnings.isEmpty()) {
            return "contract passed with " + warnings.size() + " warning(s); keep calibration visible.";
        }
        return "no blocker violations for " + title + ".";
    }

    private String defaultRepairHint() {
        return blockers.isEmpty()
                ? "No repair required now; rerun the full MCP gate after any report, state, diff, CSS, or proof edit."
                : "Repair listed blockers, regenerate affected artifacts, then rerun the full MCP gate.";
    }

    private static String require(String value, String field) {
        return Objects.requireNonNull(value, field);
    }

    private static String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public static final class Builder {
        private final String title;
        private Path packageRoot;
        private String htmlOwner;
        private String profile;
        private String requiredArtifacts;
        private String expectedInvariant;
        private String status;
        private List<McpPackageViolation> blockers = List.of();
        private List<McpPackageViolation> warnings = List.of();
        private String evidenceArtifact;
        private String coldReason;
        private String repairHint;

        private Builder(String title) {
            this.title = title;
        }

        public Builder packageRoot(Path packageRoot) {
            this.packageRoot = packageRoot;
            return this;
        }

        public Builder htmlOwner(String htmlOwner) {
            this.htmlOwner = htmlOwner;
            return this;
        }

        public Builder profile(String profile) {
            this.profile = profile;
            return this;
        }

        public Builder requiredArtifacts(String requiredArtifacts) {
            this.requiredArtifacts = requiredArtifacts;
            return this;
        }

        public Builder expectedInvariant(String expectedInvariant) {
            this.expectedInvariant = expectedInvariant;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder violations(List<McpPackageViolation> violations) {
            this.blockers = violations.stream()
                    .filter(violation -> violation.severity() == McpViolationSeverity.BLOCKER)
                    .collect(Collectors.toList());
            this.warnings = violations.stream()
                    .filter(violation -> violation.severity() == McpViolationSeverity.WARNING)
                    .collect(Collectors.toList());
            return this;
        }

        public Builder blockers(List<McpPackageViolation> blockers) {
            this.blockers = List.copyOf(blockers);
            return this;
        }

        public Builder warnings(List<McpPackageViolation> warnings) {
            this.warnings = List.copyOf(warnings);
            return this;
        }

        public Builder evidenceArtifact(String evidenceArtifact) {
            this.evidenceArtifact = evidenceArtifact;
            return this;
        }

        public Builder coldReason(String coldReason) {
            this.coldReason = coldReason;
            return this;
        }

        public Builder repairHint(String repairHint) {
            this.repairHint = repairHint;
            return this;
        }

        public McpDiagnostic build() {
            return new McpDiagnostic(this);
        }
    }
}
