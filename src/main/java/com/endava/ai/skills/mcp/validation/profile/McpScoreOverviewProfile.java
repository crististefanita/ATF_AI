package com.endava.ai.skills.mcp.validation.profile;

import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.policy.McpReportTemplateSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class McpScoreOverviewProfile {
    private McpScoreOverviewProfile() {
    }

    public static List<String> canonicalBlockers(String scoreSection) {
        List<String> blockers = new ArrayList<>();
        for (AnyTokenExpectation expectation : expectations()) {
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

    private static List<AnyTokenExpectation> expectations() {
        return ContractCatalog.reportTemplateContracts().stream()
                .filter(entry -> "score-overview-any".equals(entry.kind()))
                .map(McpScoreOverviewProfile::expectation)
                .collect(Collectors.toUnmodifiableList());
    }

    private static AnyTokenExpectation expectation(McpReportTemplateSpec spec) {
        return new AnyTokenExpectation(spec.message(), spec.tokens());
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
