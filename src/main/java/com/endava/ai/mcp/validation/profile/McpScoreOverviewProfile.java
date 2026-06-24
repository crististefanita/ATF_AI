package com.endava.ai.mcp.validation.profile;

import java.util.ArrayList;
import java.util.List;

public final class McpScoreOverviewProfile {
    private static final List<AnyTokenExpectation> EXPECTATIONS = List.of(
            exact(McpReportSections.OVERALL, "Score Overview missing Overall"),
            any("Score Overview missing Business Substance axis", "Business Substance", "business substance"),
            any("Score Overview missing Business Graph / Graph Realism axis", "Business Graph", "Graph Realism", "business graph"),
            any("Score Overview missing Historical Reopenability axis", "Historical Reopenability", "history", "reopen"),
            any("Score Overview missing Proof Chain axis", "Proof Chain", "proof chain", "Proof"),
            any("Score Overview missing artifact/contract axis", "Contract", "artifact", "Artefact", "Artifact"),
            exact("<table", "Score Overview must publish a canonical scoring table"),
            any("Score Overview table missing Axis/Axa column", "Axis", "Axa"),
            any("Score Overview table missing Score/Nota column", "Score", "Nota"),
            any("Score Overview table missing Weight/Pondere column", "Weight", "Pondere"),
            any("Score Overview table missing cold why-this-score column", "Why it sits here", "De ce sta aici", "Why"),
            any("Score Overview missing Business Substance weight", "40%", "0.40"),
            any("Score Overview missing Business Graph weight", "20%", "0.20"),
            any("Score Overview missing historical/proof weights", "15%", "0.15"),
            any("Score Overview missing contract/artifact weight", "10%", "0.10"),
            any("Score Overview missing explicit calculation/cap explanation", "=", "weighted", "calculation", "calcul"),
            any("Score Overview missing business cap reason", "cap", "capped", "plafon"));

    private McpScoreOverviewProfile() {
    }

    public static List<String> canonicalBlockers(String scoreSection) {
        List<String> blockers = new ArrayList<>();
        for (AnyTokenExpectation expectation : EXPECTATIONS) {
            expectation.validate(scoreSection, blockers);
        }
        return blockers;
    }

    public static String axesSummary() {
        return "Overall, Business Substance, Business Graph, Historical Reopenability, Proof Chain, Contract/Artifact";
    }

    public static String tableSummary() {
        return "Axis | Score | Weight | Why it sits here, plus explicit calculation and cap reason";
    }

    private static AnyTokenExpectation exact(String token, String message) {
        return new AnyTokenExpectation(message, List.of(token));
    }

    private static AnyTokenExpectation any(String message, String... tokens) {
        return new AnyTokenExpectation(message, List.of(tokens));
    }

    private static final class AnyTokenExpectation {
        private final String message;
        private final List<String> tokens;

        private AnyTokenExpectation(String message, List<String> tokens) {
            this.message = message;
            this.tokens = tokens;
        }

        private void validate(String content, List<String> blockers) {
            for (String token : tokens) {
                if (content.contains(token)) {
                    return;
                }
            }
            blockers.add(message + " [" + String.join(" | ", tokens) + "]");
        }
    }
}
