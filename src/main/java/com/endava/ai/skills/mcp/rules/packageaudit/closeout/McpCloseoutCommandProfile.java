package com.endava.ai.skills.mcp.rules.packageaudit.closeout;

import com.endava.ai.skills.mcp.contracts.execution.McpExecutionProfile;

import java.util.List;
import java.util.Locale;

enum McpCloseoutCommandProfile {
    NONE(0, List.of()),
    AD_HOC_MCP(1, List.of()),
    CANDIDATE(2, List.of(McpExecutionProfile.CANDIDATE.suiteFileName(), "mcpcandidatecontracttest")),
    FULL_SWEEP(3, List.of(McpExecutionProfile.FULL.suiteFileName(), "mcp full gate", "full mcp suite")),
    PACKAGE_AUDIT(4, List.of(McpExecutionProfile.PACKAGE_AUDIT.suiteFileName(), "mcp package-audit",
            "mcp package audit", "package-audit")),
    PACKAGE_CLOSEOUT(5, List.of(McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName(), "mcp package close-out",
            "mcp package closeout", McpExecutionProfile.PACKAGE_CLOSEOUT.id()));

    final int rank;
    private final List<String> evidenceMarkers;

    McpCloseoutCommandProfile(int rank, List<String> evidenceMarkers) {
        this.rank = rank;
        this.evidenceMarkers = evidenceMarkers;
    }

    static boolean hasPublishableCommand(String text) {
        String lower = normalize(text);
        return hasAdHocMcpWildcard(lower) || strongest(lower).rank >= FULL_SWEEP.rank;
    }

    static boolean hasFinalCloseoutCommand(String text) { return hasFinalCloseoutEvidence(text); }

    static boolean hasFinalCloseoutEvidence(String text) { return PACKAGE_CLOSEOUT.matchesEvidence(normalize(text)); }

    static boolean hasNonCloseoutPrimaryEvidence(String text) {
        String lower = normalize(text);
        return FULL_SWEEP.matchesEvidence(lower) || CANDIDATE.matchesEvidence(lower);
    }

    static McpCloseoutCommandProfile fromCommand(String text) {
        if (text == null || text.isBlank()) {
            return NONE;
        }
        String lower = normalize(text);
        for (McpCloseoutCommandProfile profile : List.of(PACKAGE_CLOSEOUT, PACKAGE_AUDIT, FULL_SWEEP, CANDIDATE)) {
            if (profile.matchesEvidence(lower)) {
                return profile;
            }
        }
        if (hasAdHocMcpWildcard(lower)) { return FULL_SWEEP; }
        return lower.contains("mcp") && lower.contains("test") ? AD_HOC_MCP : NONE;
    }

    static McpCloseoutCommandProfile strongest(String... commands) {
        McpCloseoutCommandProfile strongest = NONE;
        for (String command : commands) {
            McpCloseoutCommandProfile profile = fromCommand(command);
            if (profile.rank > strongest.rank) { strongest = profile; }
        }
        return strongest;
    }

    private boolean matchesEvidence(String lowerText) { return evidenceMarkers.stream().anyMatch(lowerText::contains); }

    private static boolean hasAdHocMcpWildcard(String lower) {
        return lower.contains("-dtest=mcp*test")
                || lower.contains("-dtest=\"mcp*test\"")
                || lower.contains("-dtest='mcp*test'");
    }

    private static String normalize(String text) { return text == null ? "" : text.toLowerCase(Locale.ROOT); }
}
