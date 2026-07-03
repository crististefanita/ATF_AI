package com.ai.skills.atlas.rules.graph.legend;

import com.ai.skills.atlas.rules.AtlasValidationRule;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasSupportingGraphsLegendRule implements AtlasValidationRule {
    private static final Pattern NUMBERED_SUBGRAPH_SUMMARY = Pattern.compile(
            "(?is)<summary>\\s*(3\\.\\d+\\s+[^<]+)</summary>");

    @Override
    public String id() {
        return "atlas.candidate.supporting-graphs.legends";
    }

    @Override
    public String expected() {
        return "Only real application flow graphs get graph legends; transversal analysis remains evidence tables.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String section = AtlasText.sectionBetween(context.lowerAtlasText(), "business flow understanding", "business evidence tables");
        Matcher matcher = NUMBERED_SUBGRAPH_SUMMARY.matcher(section);
        int graphCount = 0;
        while (matcher.find()) {
            String graph = AtlasText.normalizeWhitespace(matcher.group(1));
            String body = section.substring(matcher.end(), nextSubgraphStart(section, matcher.end()));
            if (!body.contains("<svg")) {
                continue;
            }
            graphCount++;
            if (!body.contains("legend")) {
                result.add(id(), "Business flow graph has no collapsed legend: " + graph, context.atlasHtml(),
                        "Add a short Legend subdetail explaining how this graph deepens the main business map.");
            }
            if (!body.contains("business flow graph")) {
                result.add(id(), "Business flow graph does not explicitly relate back to the main Business Flow Graph: " + graph, context.atlasHtml(),
                        "State how the flow deepens or slices the primary Business Flow Graph.");
            }
        }
        if (graphCount < 3) {
            result.add(id(), "Too few numbered business-flow subgraphs have SVG diagrams: " + graphCount,
                    context.atlasHtml(),
                        "Each application flow should contain a compact SVG diagram.");
        }
    }

    private static int nextSubgraphStart(String section, int fromIndex) {
        Matcher next = NUMBERED_SUBGRAPH_SUMMARY.matcher(section);
        if (next.find(fromIndex)) {
            return next.start();
        }
        return section.length();
    }

}
