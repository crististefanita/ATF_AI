package com.endava.ai.skills.mcp.validation.diagnostics;

import com.endava.ai.skills.mcp.McpPackageViolation;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

final class McpRootCauseViolationMatchers {
    private static final Map<String, Predicate<McpPackageViolation>> MATCHERS = Map.of(
            "final-mcp-evidence", McpRootCauseViolationMatchers::isFinalMcpEvidence,
            "report-links", McpRootCauseViolationMatchers::isReportLink,
            "report-shell", McpRootCauseViolationMatchers::isReportShell,
            "run-spine", McpRootCauseViolationMatchers::isRunSpine,
            "score-truth", McpRootCauseViolationMatchers::isScoreTruth,
            "provenance", McpRootCauseViolationMatchers::isProvenance,
            "business-proof", McpRootCauseViolationMatchers::isBusinessProof,
            "source-architecture", McpRootCauseViolationMatchers::isSourceArchitecture);

    private McpRootCauseViolationMatchers() {
    }

    static Predicate<McpPackageViolation> matcherFor(String id) {
        return MATCHERS.get(id);
    }

    static List<String> executableGroupKeys() {
        return List.copyOf(MATCHERS.keySet());
    }

    private static boolean isFinalMcpEvidence(McpPackageViolation violation) {
        String text = searchable(violation);
        return codeStarts(violation, "mcp.gate-result")
                || codeStarts(violation, "mcp.extent")
                || codeStarts(violation, "mcp.xml")
                || codeStarts(violation, "mcp.method-ledger")
                || codeStarts(violation, "mcp.evidence-counts")
                || codeStarts(violation, "mcp.skipped-profile")
                || text.contains("validation/mcp")
                || text.contains("package close-out")
                || text.contains("package-closeout");
    }

    private static boolean isReportLink(McpPackageViolation violation) {
        String text = searchable(violation);
        return codeStarts(violation, "report.mcp-evidence")
                || codeStarts(violation, "report.governance-evidence")
                || codeStarts(violation, "report.run-evidence")
                || codeStarts(violation, "report.artifact")
                || codeStarts(violation, "link.")
                || text.contains("hyperlink")
                || text.contains("broken link");
    }

    private static boolean isReportShell(McpPackageViolation violation) {
        return codeStarts(violation, "report.visual")
                || codeStarts(violation, "report.template")
                || codeStarts(violation, "report.opening")
                || codeStarts(violation, "report.executive")
                || codeStarts(violation, "report.score-card")
                || codeStarts(violation, "report.css")
                || codeStarts(violation, "report.root-run")
                || codeStarts(violation, "report.mcp-skipped-explanation");
    }

    private static boolean isRunSpine(McpPackageViolation violation) {
        String code = violation.code();
        return code.startsWith("file.")
                || code.startsWith("run.")
                || code.startsWith("run-state.")
                || code.startsWith("run-diff.")
                || code.startsWith("run-learning.")
                || code.startsWith("artifact.spine")
                || code.startsWith("package.inventory");
    }

    private static boolean isScoreTruth(McpPackageViolation violation) {
        String text = searchable(violation);
        return violation.code().startsWith("score.")
                || text.contains("score cap")
                || text.contains("overall/cap");
    }

    private static boolean isProvenance(McpPackageViolation violation) {
        String text = searchable(violation);
        return violation.code().contains("origin")
                || violation.code().contains("sha")
                || violation.code().contains("hash")
                || violation.code().contains("freshness")
                || text.contains("sha256")
                || text.contains("provenance")
                || text.contains("immutable");
    }

    private static boolean isBusinessProof(McpPackageViolation violation) {
        String code = violation.code();
        String text = searchable(violation);
        return code.startsWith("business-evidence.")
                || code.startsWith("claim-action.")
                || code.startsWith("next-frontier.")
                || code.startsWith("candidate.")
                || text.contains("business object")
                || text.contains("proof mapping")
                || text.contains("evidence chain");
    }

    private static boolean isSourceArchitecture(McpPackageViolation violation) {
        String code = violation.code();
        String text = searchable(violation);
        return code.startsWith("source.")
                || code.startsWith("source-evolution.")
                || code.startsWith("architecture.")
                || code.startsWith("package-source.")
                || code.startsWith("boundary.")
                || text.contains("atf core")
                || text.contains("mcp core")
                || text.contains("ui.core")
                || text.contains("api.core");
    }

    private static boolean codeStarts(McpPackageViolation violation, String prefix) {
        return violation.code().startsWith(prefix);
    }

    private static String searchable(McpPackageViolation violation) {
        Path path = violation.path();
        return (violation.code() + "\n"
                + violation.message() + "\n"
                + (path == null ? "" : path.toString()) + "\n"
                + nullToEmpty(violation.failureMeaning()) + "\n"
                + nullToEmpty(violation.repairAction()))
                .toLowerCase(Locale.ROOT)
                .replace('\\', '/');
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
