package com.endava.ai.skills.mcp.rules.report;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpRuleSupport;
import com.endava.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@McpContract(id = "report.score-overview.canonical",
        category = McpRuleCategory.SCORE,
        htmlReq = "score-overview-reference.html",
        artifact = "analysis/langgraph-business-understanding.html")
public final class ScoreOverviewReportRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path report = context.paths().report();
        if (!Files.isRegularFile(report)) {
            return;
        }
        String html = context.files().read(report, result);
        McpRuleSupport.requireText(html, report, result, "report.score", "Missing Score Overview", "Score Overview");
        McpRuleSupport.requireText(html, report, result, "report.overall", "Missing Overall score", "Overall");
        requireScoreOverviewMatchesPackageState(context, html, report, result);
        requireScoreCapConsistency(context, html, report, result);
    }

    private void requireScoreOverviewMatchesPackageState(McpPackageContext context,
                                                         String html,
                                                         Path report,
                                                         McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        Integer stateOverall = McpRuleSupport.extractIntValue(context.files().read(packageState, result), "overall");
        String scoreOverview = ReportRuleSupport.SECTIONS.detailsBlockBody(html, "Score Overview");
        Integer htmlOverall = ReportRuleSupport.SCORES.firstScoreValue(scoreOverview, "Overall");
        if (stateOverall != null && htmlOverall != null && !stateOverall.equals(htmlOverall)) {
            result.add(McpPackageViolation.blocker("report.score.overall-drift",
                    "Score Overview Overall must match review/package-state.json: state="
                            + stateOverall + ", report=" + htmlOverall,
                    report));
        }
    }

    private void requireScoreCapConsistency(McpPackageContext context,
                                            String html,
                                            Path report,
                                            McpPackageValidationResult result) {
        String scoreOverview = ReportRuleSupport.SECTIONS.detailsBlockBody(html, "Score Overview");
        Integer overall = ReportRuleSupport.SCORES.firstScoreValue(scoreOverview, "Overall");
        List<Integer> axisScores = new ArrayList<>();
        for (String axis : List.of("Business Substance", "Business Graph", "Historical Reopenability",
                "Proof Chain", "Contract", "Artifact")) {
            Integer score = ReportRuleSupport.SCORES.firstScoreValue(scoreOverview, axis);
            if (score != null) {
                axisScores.add(score);
            }
        }
        if (overall != null && ReportRuleSupport.hasStructuralBlockers(result)
                && overall > context.runtime().expectedDeltaQualityAsOverallCap()) {
            result.add(McpPackageViolation.blocker("score.cap.blocker-exceeded",
                    "Overall must be capped by RUN_EXPECTED_DELTA_QUALITY when structural blockers exist: overall="
                            + overall + ", cap=" + context.runtime().expectedDeltaQualityAsOverallCap(),
                    report));
        }
        if (overall == null || axisScores.size() < 3) {
            return;
        }
        int weakestAxis = axisScores.stream().min(Integer::compareTo).orElse(overall);
        if (overall > weakestAxis) {
            result.add(McpPackageViolation.blocker("score.cap.exceeded",
                    "Overall must not exceed the weakest published score axis: overall="
                            + overall + ", weakest_axis=" + weakestAxis,
                    report));
        }
    }
}
