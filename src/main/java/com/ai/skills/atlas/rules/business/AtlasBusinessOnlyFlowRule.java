package com.ai.skills.atlas.rules.business;

import com.ai.skills.atlas.rules.AtlasValidationRule;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasText;

import java.util.List;

public final class AtlasBusinessOnlyFlowRule implements AtlasValidationRule {
    private static final List<String> META_FLOW_TOKENS = List.of(
            "atlas test contract",
            "atlas tests",
            "ui agentic tests",
            "testng",
            "mcp",
            "reporting",
            "html exists",
            "shared css",
            "output shape",
            "contract ownership");

    @Override
    public String id() {
        return "atlas.candidate.business-flows.application-only";
    }

    @Override
    public String expected() {
        return "Business flow graphs describe only application business behavior discovered from the test package and supporting layers, not atlas/MCP/reporting meta-contracts.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        inspectBusinessArea(context, result, "Business Flow Graph", "Business Flow Understanding");
        inspectBusinessArea(context, result, "Business Flow Understanding", "Business Evidence Tables");
    }

    private void inspectBusinessArea(AtlasContext context,
                                     AtlasValidationResult result,
                                     String start,
                                     String end) {
        String section = AtlasText.sectionBetween(context.lowerAtlasText(),
                start.toLowerCase(), end.toLowerCase());
        if (section.isBlank()) {
            return;
        }
        for (String token : META_FLOW_TOKENS) {
            if (section.contains(token)) {
                result.add(id(), start + " contains meta-flow token: " + token, context.atlasHtml(),
                        "Remove atlas/MCP/reporting/test-suite vocabulary from business graph areas; keep only application business flows inferred from source tests and their framework layers.");
            }
        }
    }
}
