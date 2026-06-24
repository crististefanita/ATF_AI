package com.endava.ai.atlas.rules.business;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.List;

public final class AtlasBusinessOnlyFlowRule implements AtlasValidationRule {
    private static final List<String> META_FLOW_TOKENS = List.of(
            "atlas test contract",
            "html exists",
            "shared css",
            "atlas tests",
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
        String html = context.lowerAtlasText();
        int index = html.indexOf("business flow understanding");
        if (index < 0) {
            return;
        }
        String section = html.substring(index, Math.min(html.length(), index + 10_000));
        for (String token : META_FLOW_TOKENS) {
            if (section.contains(token)) {
                result.add(id(), "Business Flow Understanding contains meta-flow token: " + token, context.atlasHtml(),
                        "Remove atlas/MCP/reporting contract graphs from Business Flow Understanding; keep only application business flows inferred from source tests and their framework layers.");
            }
        }
    }
}
