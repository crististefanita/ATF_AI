package com.ai.skills.mcp.rules.candidate.business;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.html.HtmlSectionFinder;
import com.ai.skills.mcp.rules.McpRuleSupport;
import com.ai.skills.mcp.rules.McpValidationRule;
import com.ai.skills.mcp.rules.candidate.support.CandidateTextSupport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@McpContract(id = "candidate.business-graph.object-specificity",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "business-flow-graph-rubric.html",
        artifact = "analysis/langgraph-business-understanding.html")
public final class CandidateBusinessGraphObjectSpecificityRule implements McpValidationRule {
    private static final HtmlSectionFinder SECTIONS = new HtmlSectionFinder();

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        String state = context.files().read(packageState, result);
        Set<String> ownedTokens = CandidateTextSupport.meaningfulTokens(
                McpRuleSupport.extractStringValue(state, "business_object"));
        Set<String> nextTokens = CandidateTextSupport.meaningfulTokens(
                McpRuleSupport.extractStringValue(state, "primary_next_business_object"));
        String graph = CandidateTextSupport.normalize(SECTIONS.detailsBlockBody(
                context.files().readIfRegular(context.paths().report(), result), "Business Flow Graph"));

        if (!ownedTokens.isEmpty() && ownedTokens.stream().noneMatch(graph::contains)) {
            result.add(McpPackageViolation.blocker("candidate.business-graph.owned-object-missing",
                    "Business Flow Graph must name the owned business object tokens: " + ownedTokens,
                    context.paths().report()));
        }
        if (!nextTokens.isEmpty() && nextTokens.stream().noneMatch(graph::contains)) {
            result.add(McpPackageViolation.blocker("candidate.business-graph.next-closure-missing",
                    "Business Flow Graph must name the primary next closure object tokens: " + nextTokens,
                    context.paths().report()));
        }
    }
}
