package com.endava.ai.atlas.rules.graph;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.List;

public final class AtlasSupportingGraphsLegendRule implements AtlasValidationRule {
    private static final List<String> BUSINESS_FLOW_GRAPHS = List.of(
            "checkout to invoice lifecycle",
            "support message lifecycle",
            "profile account memory",
            "invoice boundary diagnostic");

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
        String html = context.lowerAtlasText();
        for (String graph : BUSINESS_FLOW_GRAPHS) {
            int index = html.indexOf(graph);
            if (index < 0) {
                result.add(id(), "Missing business flow graph: " + graph, context.atlasHtml(),
                        "Keep application business flows as graphs under Business Flow Understanding.");
                continue;
            }
            String sectionTail = html.substring(index, Math.min(html.length(), index + 4_000));
            if (!sectionTail.contains("<svg")) {
                result.add(id(), "Business flow graph has no SVG: " + graph, context.atlasHtml(),
                        "Each application flow should contain a compact SVG diagram.");
            }
            if (!sectionTail.contains("legend")) {
                result.add(id(), "Business flow graph has no collapsed legend: " + graph, context.atlasHtml(),
                        "Add a short Legend subdetail explaining how this graph deepens the main business map.");
            }
            if (!sectionTail.contains("business flow graph")) {
                result.add(id(), "Business flow graph does not explicitly relate back to the main Business Flow Graph: " + graph, context.atlasHtml(),
                        "State how the flow deepens or slices the primary Business Flow Graph.");
            }
        }
    }
}
