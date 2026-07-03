package com.ai.skills.atlas.rules.graph;

import com.ai.skills.atlas.rules.AtlasValidationRule;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasBusinessSubgraphsWidthRule implements AtlasValidationRule {
    private static final Pattern NUMBERED_SUBGRAPH = Pattern.compile(
            "(?is)<details\\b[^>]*class=\"[^\"]*subdetails[^\"]*\"[^>]*>\\s*<summary>\\s*3\\.\\d+\\s+[^<]+</summary>(.*?)</details>");

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
        int svgCount = AtlasText.count(section, "<svg");
        if (svgCount < 3) {
            result.add(id(), "Business Flow Understanding has too few SVG flow graphs: " + svgCount, context.atlasHtml(),
                    "The section should contain several full-width graphs that explain real application flows only.");
        }
        int numberedGraphs = 0;
        Matcher matcher = NUMBERED_SUBGRAPH.matcher(section);
        while (matcher.find()) {
            String subgraph = matcher.group(1);
            if (subgraph.contains("<svg")) {
                numberedGraphs++;
            }
        }
        if (numberedGraphs < 3) {
            result.add(id(), "Business Flow Understanding has too few numbered SVG subgraphs: " + numberedGraphs,
                    context.atlasHtml(),
                    "Use generic 3.x business-flow slices with SVG diagrams; do not hardcode domain-specific flow names in the contract.");
        }
        if (section.contains("coverage matrix")
                || section.contains("repetition ledger")
                || section.contains("observed gaps graph")) {
            result.add(id(), "Business Flow Understanding contains non-flow analysis vocabulary", context.atlasHtml(),
                    "Keep coverage, repetition, gaps and layer trace as evidence tables, not as business-flow graphs.");
        }
    }

}
