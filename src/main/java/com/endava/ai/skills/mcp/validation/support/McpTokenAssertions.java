package com.endava.ai.skills.mcp.validation.support;

import java.util.ArrayList;
import java.util.List;

public final class McpTokenAssertions {
    private McpTokenAssertions() {
    }

    public static void requireToken(String content, String token, String message, List<String> blockers) {
        if (!content.contains(token)) {
            blockers.add(message + " [" + token + "]");
        }
    }

    public static void requireAnyToken(String content, List<String> tokens, String message, List<String> blockers) {
        for (String token : tokens) {
            if (content.contains(token)) {
                return;
            }
        }
        blockers.add(message + " [" + String.join(" | ", tokens) + "]");
    }

    public static boolean containsAny(String content, String... values) {
        for (String value : values) {
            if (content.contains(value)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> canonicalSharedCssBlockers(String css) {
        List<String> blockers = new ArrayList<>();
        for (String token : List.of("--bg", "--panel", "--panel2", "--text", "--muted", "--border",
                "--accent", "--good", "--warn", "--bad", "--shadow", "--sans", "--mono")) {
            requireToken(css, token, "Shared CSS missing canonical variable", blockers);
        }
        for (String token : List.of(".wrap", ".hero", ".card", "details", ".subdetails", "summary",
                ".summary-title", ".sumhint", ".pill-row", ".pill", ".badge", ".diag", ".score-card",
                "table", "code.inline", ".note", ".warn", ".rule", ".executive-context",
                ".graph-legend", ".graph-legend-list", ".score-summary", ".score-table", ".score-calculation-table",
                ".proof-table", ".run-audit-list", ".artifact-links", ".closeout-summary", "@media")) {
            requireToken(css, token, "Shared CSS missing canonical presentation primitive", blockers);
        }
        requireToken(css, ".nt", "Shared CSS missing canonical graph title text class", blockers);
        requireToken(css, ".ns", "Shared CSS missing canonical graph secondary text class", blockers);
        return blockers;
    }
}
