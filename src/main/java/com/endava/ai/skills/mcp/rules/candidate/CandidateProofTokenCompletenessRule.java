package com.endava.ai.skills.mcp.rules.candidate;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.html.HtmlDetailsParser;
import com.endava.ai.skills.mcp.html.HtmlSectionFinder;
import com.endava.ai.skills.mcp.rules.McpRuleSupport;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import com.endava.ai.skills.mcp.rules.candidate.support.CandidateTextSupport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@McpContract(id = "candidate.proof-token.completeness",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "reporting.html",
        artifact = "analysis/langgraph-business-understanding.html")
public final class CandidateProofTokenCompletenessRule implements McpValidationRule {
    private static final HtmlSectionFinder SECTIONS = new HtmlSectionFinder();
    private static final HtmlDetailsParser DETAILS = new HtmlDetailsParser();

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        String state = context.files().read(packageState, result);
        Set<String> tokens = CandidateTextSupport.meaningfulTokens(
                McpRuleSupport.extractStringValue(state, "business_object"));
        if (tokens.isEmpty()) {
            return;
        }

        String proofMapping = SECTIONS.detailsBlockBody(
                context.files().readIfRegular(context.paths().report(), result), "Proof Mapping");
        boolean completeRow = DETAILS.tableRowsOrListItems(proofMapping).stream()
                .map(SECTIONS::stripTags)
                .map(CandidateTextSupport::normalize)
                .anyMatch(row -> tokens.stream().allMatch(row::contains)
                        && CandidateTextSupport.containsAny(row, "thin-no-index.diff", "root-source-thin-no-index.diff")
                        && CandidateTextSupport.containsAny(row, "run-state.json", "package-state.json")
                        && CandidateTextSupport.containsAny(row, "extent", "testng-results.xml", "command-result.md")
                        && CandidateTextSupport.containsAny(row, "verdict", "owned", "partial", "blocked", "capped"));

        if (!completeRow) {
            result.add(McpPackageViolation.blocker("candidate.business-object.proof-token-incomplete",
                    "html-ex candidate Proof Mapping must carry all major business_object tokens plus diff/state/execution/verdict: "
                            + tokens,
                    context.paths().report()));
        }
    }
}
