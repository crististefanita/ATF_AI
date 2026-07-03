package com.ai.skills.atlas.rules.business;

import com.ai.skills.atlas.rules.AtlasValidationRule;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;

public final class AtlasObservedGapsSecondaryRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.comprehension-limits.secondary";
    }

    @Override
    public String expected() {
        return "Comprehension limits may exist, but they remain secondary; the atlas is a business comprehension map, not a next-run planning report.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        if (html.contains("next frontier graph")) {
            result.add(id(), "Atlas promotes Next Frontier as a central graph", context.atlasHtml(),
                    "Rename or demote this to Comprehension Limits. The atlas should explain what existing tests prove before suggesting future work.");
        }
        if (html.contains("observed gaps graph")) {
            result.add(id(), "Atlas still uses Observed Gaps Graph vocabulary", context.atlasHtml(),
                    "Use Comprehension Limits for limits of understanding; do not make gap/planning language the visual center.");
        }
        if (html.contains("mandatory next") || html.contains("must run next") || html.contains("next run instruction")) {
            result.add(id(), "Atlas uses planning language instead of analysis-only language", context.atlasHtml(),
                    "Comprehension limits are allowed, but the atlas must not prescribe a mandatory next run.");
        }
        int limitsIndex = firstGraphTitleIndex(html, "comprehension limits");
        int businessFlowIndex = html.indexOf("business flow graph");
        if (limitsIndex >= 0 && businessFlowIndex >= 0 && limitsIndex < businessFlowIndex) {
            result.add(id(), "Comprehension Limits graph appears before the main Business Flow Graph", context.atlasHtml(),
                    "Keep limitation graphs below the comprehension map. Collapsed Executive Context may mention limits, but the first visual center should be the business already understood from source tests.");
        }
    }

    private static int firstGraphTitleIndex(String html, String title) {
        int titleIndex = html.indexOf(title);
        if (titleIndex < 0) {
            return -1;
        }
        int graphPanelIndex = html.indexOf("graph-panel", Math.max(0, titleIndex - 300));
        if (graphPanelIndex >= 0 && graphPanelIndex < titleIndex + 300) {
            return titleIndex;
        }
        return -1;
    }
}
