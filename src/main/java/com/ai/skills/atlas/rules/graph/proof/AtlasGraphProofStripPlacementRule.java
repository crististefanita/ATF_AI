package com.ai.skills.atlas.rules.graph.proof;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.contract.AtlasRuleIds;
import com.ai.skills.atlas.rules.AtlasText;
import com.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasGraphProofStripPlacementRule implements AtlasValidationRule {
    private static final Pattern GRAPH_PANEL = Pattern.compile("(?is)<div\\b[^>]*class=[\"'][^\"']*\\bgraph-panel\\b[^\"']*[\"'][^>]*>.*?</svg>\\s*</div>");
    private static final Pattern DETAILS_WITH_SUMMARY = Pattern.compile("(?is)<details\\b([^>]*)>\\s*<summary\\b[^>]*>(.*?)</summary>");

    @Override
    public String id() {
        return AtlasRuleIds.Candidate.GRAPH_PROOF_STRIP_PLACEMENT;
    }

    @Override
    public String expected() {
        return "Every Atlas graph panel is followed locally by a collapsed Legend and then a collapsed Banda de dovada noduri proof strip inside the same graph section.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String graphArea = graphArea(context.atlasText());
        if (graphArea.isBlank()) {
            result.add(id(), "Cannot find Atlas graph area", context.atlasHtml(),
                    "Keep Business Flow Graph and Business Flow Understanding before validating graph proof-strip placement.");
            return;
        }

        Matcher matcher = GRAPH_PANEL.matcher(graphArea);
        int graphs = 0;
        while (matcher.find()) {
            graphs++;
            String tail = tailAfterGraph(graphArea, matcher.end());
            validateGraphTail(context, result, graphs, tail);
        }
        if (graphs == 0) {
            result.add(id(), "Atlas graph area has no graph-panel blocks", context.atlasHtml(),
                    "Each business graph should render through a graph-panel followed by Legend and proof strip subdetails.");
        }
    }

    private static void validateGraphTail(AtlasContext context,
                                          AtlasValidationResult result,
                                          int graphIndex,
                                          String tail) {
        DetailsMatch legend = firstDetails(tail, "legend");
        DetailsMatch proof = firstDetails(tail, "banda de dovada noduri", "proof strip");
        if (legend.missing()) {
            result.add(AtlasRuleIds.Candidate.GRAPH_PROOF_STRIP_PLACEMENT,
                    "Graph panel " + graphIndex + " is not followed by a collapsed Legend subdetail",
                    context.atlasHtml(),
                    "Place <details class=\"subdetails\"><summary>Legend</summary> immediately after each graph SVG.");
        } else if (legend.open()) {
            result.add(AtlasRuleIds.Candidate.GRAPH_PROOF_STRIP_PLACEMENT,
                    "Graph panel " + graphIndex + " Legend is open by default",
                    context.atlasHtml(),
                    "All legends must be collapsed; only 2. Business Flow Graph itself may be open.");
        }
        if (proof.missing()) {
            result.add(AtlasRuleIds.Candidate.GRAPH_PROOF_STRIP_PLACEMENT,
                    "Graph panel " + graphIndex + " is missing Banda de dovada noduri below its Legend",
                    context.atlasHtml(),
                    "Place the proof strip directly under the Legend for the same graph/subgraph section.");
        } else if (proof.open()) {
            result.add(AtlasRuleIds.Candidate.GRAPH_PROOF_STRIP_PLACEMENT,
                    "Graph panel " + graphIndex + " proof strip is open by default",
                    context.atlasHtml(),
                    "All proof strips must be collapsed by default.");
        }
        if (legend.present() && proof.present() && legend.index() > proof.index()) {
            result.add(AtlasRuleIds.Candidate.GRAPH_PROOF_STRIP_PLACEMENT,
                    "Graph panel " + graphIndex + " publishes proof strip before Legend",
                    context.atlasHtml(),
                    "Use the canonical local order: graph SVG, collapsed Legend, collapsed Banda de dovada noduri.");
        }
    }

    private static String graphArea(String html) {
        String lower = html.toLowerCase(Locale.ROOT);
        int start = lower.indexOf("business flow graph");
        if (start < 0) {
            return "";
        }
        int end = lower.indexOf("business evidence tables", start);
        if (end < 0) {
            return html.substring(start);
        }
        return html.substring(start, end);
    }

    private static String tailAfterGraph(String graphArea, int graphEnd) {
        int nextGraph = graphArea.indexOf("<div class=\"graph-panel\"", graphEnd);
        int nextSection = graphArea.indexOf("business evidence tables", graphEnd);
        int end = graphArea.length();
        if (nextGraph >= 0) {
            end = Math.min(end, nextGraph);
        }
        if (nextSection >= 0) {
            end = Math.min(end, nextSection);
        }
        return graphArea.substring(graphEnd, end);
    }

    private static DetailsMatch firstDetails(String html, String... summaryTokens) {
        Matcher matcher = DETAILS_WITH_SUMMARY.matcher(html);
        while (matcher.find()) {
            String summary = AtlasText.plainText(matcher.group(2)).toLowerCase(Locale.ROOT);
            for (String token : summaryTokens) {
                if (summary.contains(token)) {
                    String attributes = matcher.group(1);
                    boolean open = Pattern.compile("(?i)(^|\\s)open(\\s|=|$)").matcher(attributes).find();
                    return new DetailsMatch(matcher.start(), true, open);
                }
            }
        }
        return new DetailsMatch(-1, false, false);
    }

    private static final class DetailsMatch {
        private final int index;
        private final boolean present;
        private final boolean open;

        private DetailsMatch(int index, boolean present, boolean open) {
            this.index = index;
            this.present = present;
            this.open = open;
        }

        private int index() {
            return index;
        }

        private boolean present() {
            return present;
        }

        private boolean missing() {
            return !present;
        }

        private boolean open() {
            return open;
        }
    }
}
