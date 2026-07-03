package com.endava.ai.skills.atlas.rules.graph.style;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasText;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
    private static final Pattern NODE_GROUP_PATTERN = Pattern.compile(
            "(?is)<g\\b([^>]*)>(?:(?!</g>).)*?<text\\b[^>]*class=[\"'][^\"']*\\bnt\\b[^\"']*[\"'][^>]*>(.*?)</text>(?:(?!</g>).)*?</g>");
    private static final Pattern TRANSFORM_PATTERN = Pattern.compile(
            "(?i)translate\\s*\\(\\s*(-?\\d+(?:\\.\\d+)?)\\s+(-?\\d+(?:\\.\\d+)?)\\s*\\)");
    private static final Pattern DIRECT_NODE_PATTERN = Pattern.compile(
            "(?is)<rect\\b(?=[^>]*class=[\"'][^\"']*\\bnode\\b[^\"']*[\"'])([^>]*)>");
    private static final Pattern DIRECT_NODE_TITLE_PATTERN = Pattern.compile(
            "(?is)<rect\\b(?=[^>]*class=[\"'][^\"']*\\bnode\\b[^\"']*[\"'])([^>]*)>.*?<text\\b[^>]*class=[\"'][^\"']*\\bnt\\b[^\"']*[\"'][^>]*>(.*?)</text>");
    private static final Pattern Y_ATTRIBUTE_PATTERN = Pattern.compile(
            "(?i)\\by\\s*=\\s*[\"'](-?\\d+(?:\\.\\d+)?)");
    private static final Pattern EDGE_PATH_PATTERN = Pattern.compile(
            "(?is)<path\\b(?=[^>]*class=[\"'][^\"']*\\bedge\\b[^\"']*[\"'])([^>]*)>");
    private static final Pattern EDGE_LINE_PATTERN = Pattern.compile(
            "(?is)<line\\b(?=[^>]*class=[\"'][^\"']*\\bedge\\b[^\"']*[\"'])([^>]*)>");
    private static final Pattern MARKER_END_PATTERN = Pattern.compile(
            "(?is)<path\\b(?=[^>]*class=[\"'][^\"']*\\bedge\\b[^\"']*[\"'])(?=[^>]*\\bmarker-end\\s*=)[^>]*>");
    private static final Pattern CURVED_EDGE_PATTERN = Pattern.compile(
            "(?is)<path\\b(?=[^>]*class=[\"'][^\"']*\\bedge\\b[^\"']*[\"'])(?=[^>]*\\bd\\s*=\\s*[\"'][^\"']*[CQ][^\"']*[\"'])[^>]*>");
    private static final Pattern DASHED_EDGE_PATTERN = Pattern.compile(
            "(?is)<(?:path|line)\\b(?=[^>]*class=[\"'][^\"']*\\bedge\\b[^\"']*\\bdashed\\b[^\"']*[\"'])[^>]*>");
    private static final Pattern CAPPED_FRONTIER_NODE_PATTERN = Pattern.compile(
            "(?is)<(?:rect|g)\\b(?=[^>]*(?:class=[\"'][^\"']*\\b(?:cap|frontier)\\b[^\"']*[\"']|data-maturity=[\"'](?:frontier|not-claimed)[\"']))[^>]*>");
    private static final Pattern PRIMARY_OBJECT_PATTERN = Pattern.compile(
            "(?is)(?:class=[\"'][^\"']*\\b(?:strong|primary)\\b[^\"']*[\"']|data-primary=[\"']true[\"']|primary owned object)");
    private static final double MIN_WIDTH_RATIO_TO_MAIN_GRAPH = 0.90;
    private static final double MAX_INLINE_FONT_SIZE = 15.0;
    private static final int MIN_MAIN_GRAPH_EDGE_LABELS = 3;
    private static final int MIN_MAIN_GRAPH_NODE_BANDS = 3;
    private static final int READABLE_MAIN_GRAPH_NODE_THRESHOLD = 5;
    private static final int RICH_MAIN_GRAPH_NODE_THRESHOLD = 6;
    private static final double MIN_RICH_MAIN_GRAPH_HEIGHT = 500.0;
    private static final double MIN_SUPPORTING_VERTICAL_FILL_RATIO = 0.50;
    private static final double MIN_SPARSE_SUPPORTING_HEIGHT = 200.0;

    @Override
    public String id() {
        return "atlas.candidate.graphs.visual-scale";
    }

    @Override
    public String expected() {
        return "All atlas graphs keep a stable visual scale, and the primary Business Flow Graph is visually readable: multi-band layout, labeled relationships and visible maturity cues.";
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
        validateMainGraphReadability(context, result, mainGraph);
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
            validateSupportingGraphReadability(context, result, graph);
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

    private void validateMainGraphReadability(AtlasContext context, AtlasValidationResult result, GraphSvg mainGraph) {
        List<NodeBox> nodes = parseNodeBoxes(mainGraph.body);
        if (nodes.size() >= RICH_MAIN_GRAPH_NODE_THRESHOLD
                && mainGraph.viewBoxHeight < MIN_RICH_MAIN_GRAPH_HEIGHT) {
            result.add(id(), "Primary Business Flow Graph is too compressed for an ATF1-style business map: height="
                            + trim(mainGraph.viewBoxHeight) + ", expected>=" + trim(MIN_RICH_MAIN_GRAPH_HEIGHT),
                    context.atlasHtml(),
                    "Use a taller primary graph canvas like the ATF1 calibrated report: enough vertical room for top, middle and closure/frontier bands instead of a tight two-row map.");
        }
        if (nodes.size() >= RICH_MAIN_GRAPH_NODE_THRESHOLD && distinctYBands(nodes) < MIN_MAIN_GRAPH_NODE_BANDS) {
            result.add(id(), "Primary Business Flow Graph is too linear: nodes=" + nodes.size()
                            + ", y-bands=" + distinctYBands(nodes),
                    context.atlasHtml(),
                    "Use an ATF1-style multi-band business map for the primary graph: upstream objects on the first band, owned interaction/state flow in the middle band and closure/frontier state on a lower band.");
        }
        int pathEdges = countMatches(EDGE_PATH_PATTERN, mainGraph.body);
        int lineEdges = countMatches(EDGE_LINE_PATTERN, mainGraph.body);
        if (nodes.size() >= RICH_MAIN_GRAPH_NODE_THRESHOLD && pathEdges == 0 && lineEdges > 0) {
            result.add(id(), "Primary Business Flow Graph is line-only: line edges=" + lineEdges
                            + ", path edges=" + pathEdges,
                    context.atlasHtml(),
                    "Draw main relationships with SVG path edges, including at least one curved or soft edge. Straight line-only maps collapse into a flat grid and do not match the calibrated ATF1 business graph.");
        }
        if (nodes.size() >= RICH_MAIN_GRAPH_NODE_THRESHOLD
                && countMatches(CURVED_EDGE_PATTERN, mainGraph.body) == 0) {
            result.add(id(), "Primary Business Flow Graph has no curved/soft relationship path",
                    context.atlasHtml(),
                    "Add a curved or soft path for the cross-band business dependency, for example the primary owned object opening the intake/state lane. This keeps the graph readable when it spans multiple bands.");
        }
        if (nodes.size() >= RICH_MAIN_GRAPH_NODE_THRESHOLD
                && !PRIMARY_OBJECT_PATTERN.matcher(mainGraph.body).find()) {
            result.add(id(), "Primary Business Flow Graph does not visually identify the primary owned object",
                    context.atlasHtml(),
                    "Mark exactly one central owned business object as primary, using the existing strong/primary styling or a visible 'primary owned object' cue.");
        }
        if (nodes.size() >= RICH_MAIN_GRAPH_NODE_THRESHOLD
                && (countMatches(CAPPED_FRONTIER_NODE_PATTERN, mainGraph.body) == 0
                || countMatches(DASHED_EDGE_PATTERN, mainGraph.body) == 0)) {
            result.add(id(), "Primary Business Flow Graph lacks a capped frontier and dashed frontier relationship",
                    context.atlasHtml(),
                    "When the report contains partial, surface-only or not-yet-owned boundaries, render them like the ATF1 calibrated graph: a capped/frontier node and a dashed relationship from the last owned/confirmed state.");
        }
        int edgeLabels = AtlasGraphRelationshipLabels.count(mainGraph.body);
        if (nodes.size() >= READABLE_MAIN_GRAPH_NODE_THRESHOLD && edgeLabels < MIN_MAIN_GRAPH_EDGE_LABELS) {
            result.add(id(), "Primary Business Flow Graph has too few visible relationship labels: "
                            + edgeLabels + ", expected>=" + MIN_MAIN_GRAPH_EDGE_LABELS,
                    context.atlasHtml(),
                    "Label the main graph arrows with short business verbs such as authenticates, retains, exposes, opens or limits so relationships are visible in the diagram, not only in prose.");
        }
        int semanticEdgeLabels = AtlasGraphRelationshipLabels.semantic(mainGraph.body).size();
        if (nodes.size() >= READABLE_MAIN_GRAPH_NODE_THRESHOLD && semanticEdgeLabels < MIN_MAIN_GRAPH_EDGE_LABELS) {
            result.add(id(), "Primary Business Flow Graph relationship labels are too terse: semantic labels="
                            + semanticEdgeLabels + ", expected>=" + MIN_MAIN_GRAPH_EDGE_LABELS,
                    context.atlasHtml(),
                    "Use compact but specific relationship labels with a business object or state cue, for example opens account, retains profile, exposes contact, limits invoice or reopens evidence.");
        }
        nodes.stream()
                .filter(node -> !node.maturity.isBlank())
                .filter(node -> !visibleTextContainsMaturity(node.visibleText, node.maturity))
                .forEach(node -> result.add(id(), "Primary graph node has machine-readable maturity but no visible maturity cue: "
                                + node.label + " -> " + node.maturity,
                        context.atlasHtml(),
                        "Render a compact visible maturity cue inside or next to the node, matching data-maturity=\""
                                + node.maturity + "\". Keep the node visually neutral; the cue may be a small text line or badge."));
    }

    private void validateSupportingGraphReadability(AtlasContext context,
                                                    AtlasValidationResult result,
                                                    GraphSvg graph) {
        List<NodeBox> nodes = parseNodeBoxes(graph.body);
        int pathEdges = countMatches(EDGE_PATH_PATTERN, graph.body);
        int lineEdges = countMatches(EDGE_LINE_PATTERN, graph.body);
        int relationshipLabels = AtlasGraphRelationshipLabels.count(graph.body);
        int edgeMarkers = countMatches(MARKER_END_PATTERN, graph.body);
        if (nodes.size() >= 4 && lineEdges > 0 && pathEdges == 0) {
            result.add(id(), "Supporting Business Flow Understanding graph is line-only: "
                            + graph.label + " line edges=" + lineEdges + ", path edges=" + pathEdges,
                    context.atlasHtml(),
                    "Use the same SVG relationship grammar as the primary graph for 3.x subgraphs: replace <line class=\"edge\"> connectors with <path class=\"edge\"> connectors while keeping the compact four-node subgraph shape.");
        }
        if (nodes.size() >= 4
                && graph.viewBoxHeight >= MIN_SPARSE_SUPPORTING_HEIGHT
                && verticalSpread(nodes) < graph.viewBoxHeight * MIN_SUPPORTING_VERTICAL_FILL_RATIO) {
            result.add(id(), "Supporting Business Flow Understanding graph leaves too much vertical dead space: "
                            + graph.label + " node-spread=" + trim(verticalSpread(nodes))
                            + ", height=" + trim(graph.viewBoxHeight),
                    context.atlasHtml(),
                    "Keep 3.x subgraphs compact at report scale: reduce the supporting SVG viewBox height or rebalance node positions so the four-node mini-flow uses the panel instead of leaving a large empty lower half.");
        }
        if (nodes.size() >= 4 && pathEdges > 0 && edgeMarkers < pathEdges) {
            result.add(id(), "Supporting Business Flow Understanding graph edges are missing arrowheads: "
                            + graph.label + " marker-end edges=" + edgeMarkers + ", path edges=" + pathEdges,
                    context.atlasHtml(),
                    "Render supporting subgraph relationships like the calibrated ATF1 graph: every <path class=\"edge\"> relationship should carry marker-end so the direction is visible.");
        }
        if (nodes.size() >= 4 && pathEdges > 0 && relationshipLabels < Math.min(3, pathEdges)) {
            result.add(id(), "Supporting Business Flow Understanding graph has too few visible relationship labels: "
                            + graph.label + " labels=" + relationshipLabels + ", path edges=" + pathEdges,
                    context.atlasHtml(),
                    "Add short business relationship labels on the supporting graph edges, not only subtitles inside nodes. The reader should see why one business object/state leads to the next.");
        }
    }

    private static List<NodeBox> parseNodeBoxes(String svgBody) {
        Matcher matcher = NODE_GROUP_PATTERN.matcher(svgBody);
        List<NodeBox> nodes = new ArrayList<>();
        while (matcher.find()) {
            String attributes = matcher.group(1);
            Matcher transform = TRANSFORM_PATTERN.matcher(attributes);
            double y = 0;
            if (transform.find()) {
                y = Double.parseDouble(transform.group(2));
            }
            String markup = matcher.group(0);
            nodes.add(new NodeBox(
                    AtlasText.normalizeLabel(AtlasText.stripTags(matcher.group(2))),
                    y,
                    maturity(attributes),
                    AtlasText.normalizeLabel(AtlasText.stripTags(markup))));
        }
        if (nodes.isEmpty()) {
            Matcher directMatcher = DIRECT_NODE_TITLE_PATTERN.matcher(svgBody);
            while (directMatcher.find()) {
                nodes.add(new NodeBox(
                        AtlasText.normalizeLabel(AtlasText.stripTags(directMatcher.group(2))),
                        yAttribute(directMatcher.group(1)),
                        maturity(directMatcher.group(1)),
                        AtlasText.normalizeLabel(AtlasText.stripTags(directMatcher.group(0)))));
            }
        }
        if (nodes.isEmpty()) {
            Matcher rectMatcher = DIRECT_NODE_PATTERN.matcher(svgBody);
            while (rectMatcher.find()) {
                nodes.add(new NodeBox("", yAttribute(rectMatcher.group(1)), maturity(rectMatcher.group(1)), ""));
            }
        }
        return nodes;
    }

    private static int distinctYBands(List<NodeBox> nodes) {
        Set<Integer> bands = new HashSet<>();
        nodes.forEach(node -> bands.add((int) Math.round(node.y / 120.0)));
        return bands.size();
    }

    private static double verticalSpread(List<NodeBox> nodes) {
        double minimum = nodes.stream().mapToDouble(node -> node.y).min().orElse(0);
        double maximum = nodes.stream().mapToDouble(node -> node.y).max().orElse(0);
        return maximum - minimum;
    }

    private static int countMatches(Pattern pattern, String body) {
        int count = 0;
        Matcher matcher = pattern.matcher(body);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static String maturity(String attributes) {
        Matcher matcher = Pattern.compile("(?i)data-maturity\\s*=\\s*[\"']([^\"']+)[\"']").matcher(attributes);
        return matcher.find() ? matcher.group(1).toLowerCase(Locale.ROOT) : "";
    }

    private static double yAttribute(String attributes) {
        Matcher matcher = Y_ATTRIBUTE_PATTERN.matcher(attributes);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : 0;
    }

    private static boolean visibleTextContainsMaturity(String visibleText, String maturity) {
        String normalized = maturity.replace("-", " ");
        return visibleText.contains(maturity) || visibleText.contains(normalized);
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
            return matcher.group(1).toLowerCase(Locale.ROOT);
        }
        return "unnamed graph";
    }

    private static String trim(double value) {
        if (value == Math.rint(value)) {
            return Long.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class GraphSvg {
        private final String label;
        private final double viewBoxWidth;
        private final double viewBoxHeight;
        private final String body;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class NodeBox {
        private final String label;
        private final double y;
        private final String maturity;
        private final String visibleText;
    }
}
