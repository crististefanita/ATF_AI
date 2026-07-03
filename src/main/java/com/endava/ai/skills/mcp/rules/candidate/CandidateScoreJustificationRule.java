package com.endava.ai.skills.mcp.rules.candidate;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.html.HtmlDetailsParser;
import com.endava.ai.skills.mcp.html.HtmlSectionFinder;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import com.endava.ai.skills.mcp.rules.candidate.support.CandidateTextSupport;

import java.util.HashSet;
import java.util.Set;

@McpContract(id = "candidate.score.justification",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "score-overview-reference.html",
        artifact = "analysis/langgraph-business-understanding.html")
public final class CandidateScoreJustificationRule implements McpValidationRule {
    private static final HtmlSectionFinder SECTIONS = new HtmlSectionFinder();
    private static final HtmlDetailsParser DETAILS = new HtmlDetailsParser();

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        String scoreOverview = SECTIONS.detailsBlockBody(
                context.files().readIfRegular(context.paths().report(), result), "Score Overview");
        Set<String> justifications = new HashSet<>();
        int scoredRows = 0;
        int weakRows = 0;

        for (String rowHtml : DETAILS.tableRowsOrListItems(scoreOverview)) {
            String row = SECTIONS.stripTags(rowHtml).trim().replaceAll("\\s+", " ");
            if (!row.matches(".*\\b\\d{2,3}\\b.*") || row.toLowerCase(java.util.Locale.ROOT).contains("score")) {
                continue;
            }
            scoredRows++;
            String normalized = CandidateTextSupport.normalize(row);
            boolean artifactBacked = CandidateTextSupport.containsAny(normalized,
                    "diff", "state", "extent", "xml", "mcp", "proof", "graph", "cap", "artifact", "link");
            boolean generic = normalized.contains("good") || normalized.contains("solid") || normalized.contains("strong");
            if (row.length() < 45 || !artifactBacked || generic && !normalized.contains("because")) {
                weakRows++;
            }
            justifications.add(normalized.replaceAll("\\b\\d{2,3}\\b", "").trim());
        }

        if (scoredRows == 0 || weakRows > 0 || justifications.size() < Math.min(3, scoredRows)) {
            result.add(McpPackageViolation.blocker("candidate.score.justification-generic",
                    "html-ex candidate score rows must have distinct artifact-backed justifications; scoredRows="
                            + scoredRows + ", weakRows=" + weakRows + ", distinct=" + justifications.size(),
                    context.paths().report()));
        }
    }
}
