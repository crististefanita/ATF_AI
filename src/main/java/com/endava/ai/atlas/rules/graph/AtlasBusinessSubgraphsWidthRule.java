package com.endava.ai.atlas.rules.graph;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

public final class AtlasBusinessSubgraphsWidthRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.business-subgraphs.full-width";
    }

    @Override
    public String expected() {
        return "Business Flow Understanding uses full-width real application flow graphs, while support analysis stays in tables.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        int index = html.indexOf("business flow understanding");
        if (index < 0) {
            result.add(id(), "Missing Business Flow Understanding section", context.atlasHtml(),
                    "Add Business Flow Understanding after the primary Business Flow Graph.");
            return;
        }
        int nextSection = html.indexOf("business evidence tables", index + "business flow understanding".length());
        String section = html.substring(index, nextSection > index ? nextSection : Math.min(html.length(), index + 12_000));
        if (section.contains("diagram-grid")) {
            result.add(id(), "Business Flow Understanding graphs are rendered inside diagram-grid", context.atlasHtml(),
                    "Render each application flow as a full-width collapsed panel aligned with the Business Flow Graph width.");
        }
        if (!section.contains("checkout to invoice lifecycle")
                || !section.contains("support message lifecycle")
                || !section.contains("profile account memory")) {
            result.add(id(), "Business Flow Understanding does not expose the expected application flow slices", context.atlasHtml(),
                    "Keep multiple business-flow graphs, but make each one a full-width subdetail with its own legend.");
        }
        int svgCount = count(section, "<svg");
        if (svgCount < 3) {
            result.add(id(), "Business Flow Understanding has too few SVG flow graphs: " + svgCount, context.atlasHtml(),
                    "The section should contain several full-width graphs that explain real application flows only.");
        }
        if (section.contains("coverage matrix")
                || section.contains("repetition ledger")
                || section.contains("observed gaps graph")) {
            result.add(id(), "Business Flow Understanding contains non-flow analysis vocabulary", context.atlasHtml(),
                    "Keep coverage, repetition, gaps and layer trace as evidence tables, not as business-flow graphs.");
        }
    }

    private static int count(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }
}
