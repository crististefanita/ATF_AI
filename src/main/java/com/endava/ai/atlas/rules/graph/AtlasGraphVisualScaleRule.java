package com.endava.ai.atlas.rules.graph;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasGraphVisualScaleRule implements AtlasValidationRule {
    private static final Pattern SVG_PATTERN = Pattern.compile(
            "(?is)<svg\\b([^>]*)>(.*?)</svg>");
    private static final Pattern VIEWBOX_PATTERN = Pattern.compile(
            "(?i)viewbox\\s*=\\s*\"\\s*0\\s+0\\s+(\\d+(?:\\.\\d+)?)\\s+(\\d+(?:\\.\\d+)?)\\s*\"");
    private static final Pattern ARIA_LABEL_PATTERN = Pattern.compile(
            "(?i)aria-label\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern INLINE_FONT_SIZE_PATTERN = Pattern.compile(
            "(?i)font-size\\s*[:=]\\s*['\"]?\\s*(\\d+(?:\\.\\d+)?)");
    private static final double MIN_WIDTH_RATIO_TO_MAIN_GRAPH = 0.90;
    private static final double MAX_INLINE_FONT_SIZE = 15.0;

    @Override
    public String id() {
        return "atlas.candidate.graphs.visual-scale";
    }

    @Override
    public String expected() {
        return "All atlas graphs keep a stable visual scale: supporting graph viewBoxes are close to the main Business Flow Graph and do not override font sizes.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        List<GraphSvg> graphs = parseGraphs(context.lowerAtlasText());
        GraphSvg mainGraph = graphs.stream()
                .filter(graph -> graph.label.contains("business flow graph"))
                .findFirst()
                .orElse(null);
        if (mainGraph == null) {
            result.add(id(), "Missing main Business Flow Graph SVG for visual-scale baseline",
                    context.atlasHtml(),
                    "Keep the primary Business Flow Graph as the scale reference for all atlas diagrams.");
            return;
        }
        double minimumWidth = mainGraph.viewBoxWidth * MIN_WIDTH_RATIO_TO_MAIN_GRAPH;
        for (GraphSvg graph : graphs) {
            if (graph == mainGraph) {
                continue;
            }
            if (graph.viewBoxWidth < minimumWidth) {
                result.add(id(), "Graph viewBox is too narrow and will inflate text/nodes: "
                                + graph.label + " width=" + trim(graph.viewBoxWidth)
                                + ", expected>=" + trim(minimumWidth),
                        context.atlasHtml(),
                        "Use the same visual scale as the main Business Flow Graph. Widen supporting SVG viewBox values instead of letting full-width rendering enlarge fonts and node borders.");
            }
            Matcher fontMatcher = INLINE_FONT_SIZE_PATTERN.matcher(graph.body);
            while (fontMatcher.find()) {
                double fontSize = Double.parseDouble(fontMatcher.group(1));
                if (fontSize > MAX_INLINE_FONT_SIZE) {
                    result.add(id(), "Graph overrides text with too-large inline font size: "
                                    + graph.label + " font-size=" + trim(fontSize),
                            context.atlasHtml(),
                            "Keep graph text sizing in shared-report-reference.css and avoid local oversized SVG font overrides.");
                }
            }
        }
    }

    private static List<GraphSvg> parseGraphs(String html) {
        Matcher matcher = SVG_PATTERN.matcher(html);
        List<GraphSvg> graphs = new ArrayList<>();
        while (matcher.find()) {
            String attributes = matcher.group(1);
            String body = matcher.group(2);
            Matcher viewBox = VIEWBOX_PATTERN.matcher(attributes);
            if (!viewBox.find()) {
                continue;
            }
            graphs.add(new GraphSvg(
                    label(attributes),
                    Double.parseDouble(viewBox.group(1)),
                    Double.parseDouble(viewBox.group(2)),
                    body));
        }
        return graphs;
    }

    private static String label(String attributes) {
        Matcher matcher = ARIA_LABEL_PATTERN.matcher(attributes);
        if (matcher.find()) {
            return matcher.group(1).toLowerCase(java.util.Locale.ROOT);
        }
        return "unnamed graph";
    }

    private static String trim(double value) {
        if (value == Math.rint(value)) {
            return Long.toString(Math.round(value));
        }
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static final class GraphSvg {
        private final String label;
        private final double viewBoxWidth;
        @SuppressWarnings("unused")
        private final double viewBoxHeight;
        private final String body;

        private GraphSvg(String label, double viewBoxWidth, double viewBoxHeight, String body) {
            this.label = label;
            this.viewBoxWidth = viewBoxWidth;
            this.viewBoxHeight = viewBoxHeight;
            this.body = body;
        }
    }
}
