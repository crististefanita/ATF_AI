package com.ai.skills.atlas.rules.graph;

import com.ai.skills.atlas.rules.AtlasValidationRule;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasBusinessSubgraphsCollapsedRule implements AtlasValidationRule {
    private static final Pattern BUSINESS_FLOW_UNDERSTANDING_DETAILS = Pattern.compile(
            "(?is)<details\\b([^>]*)>\\s*<summary>\\s*(?:<[^>]+>)*\\s*(?:\\d+\\.\\s*)?business flow understanding");

    @Override
    public String id() {
        return "atlas.candidate.business-subgraphs.collapsed";
    }

    @Override
    public String expected() {
        return "Business Flow Understanding is available, but collapsed by default so the primary Business Flow Graph remains the only large open visual surface.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        Matcher matcher = BUSINESS_FLOW_UNDERSTANDING_DETAILS.matcher(context.lowerAtlasText());
        if (!matcher.find()) {
            result.add(id(), "Missing Business Flow Understanding section", context.atlasHtml(),
                    "Keep Business Flow Understanding as a secondary section after the primary Business Flow Graph.");
            return;
        }
        if (matcher.group(1).contains("open")) {
            result.add(id(), "Business Flow Understanding is open by default", context.atlasHtml(),
                    "Close Business Flow Understanding by default; keep the primary Business Flow Graph as the main open visual anchor.");
        }
    }
}
