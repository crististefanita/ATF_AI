package com.ai.skills.mcp.validation.profile.graph;

import com.ai.skills.mcp.rules.McpRuleSupport;
import com.ai.skills.mcp.rules.candidate.support.CandidateTextSupport;
import com.ai.skills.mcp.validation.profile.McpReportSections;
import com.ai.skills.mcp.validation.support.McpHtmlAuditSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class McpGraphSemanticTruthProfile {
    private static final Pattern NODE_SHAPE = Pattern.compile(
            "(?is)<(?:rect|circle|polygon)\\b([^>]*class\\s*=\\s*[\"'][^\"']*\\bnode\\b[^\"']*[\"'][^>]*)>");
    private static final Pattern TEXT = Pattern.compile("(?is)<text\\b([^>]*)>(.*?)</text>");
    private static final List<String> PROOF_ARTIFACT_TOKENS = List.of(
            "run_", "extent", "testng", "xml", "diff", "mcp", "gate", "report",
            "screenshot", "validation", "ledger", "checksum", "sha256", "artifact",
            "package-state", "run-state", "method");
    private static final Set<String> TECHNICAL_ONLY_TOKENS = Set.of(
            "extent", "testng", "xml", "diff", "mcp", "gate", "report",
            "screenshot", "validation", "ledger", "checksum", "sha256", "artifact", "method");

    private final String html;
    private final String packageState;
    private final String graphSection;
    private final String legend;
    private final String executiveContext;
    private final String scoreOverview;
    private final String proofMapping;
    private final List<GraphNode> nodes;

    private McpGraphSemanticTruthProfile(String html, String packageState) {
        this.html = html == null ? "" : html;
        this.packageState = packageState == null ? "" : packageState;
        this.graphSection = McpHtmlAuditSupport.detailsBody(this.html, McpReportSections.BUSINESS_FLOW_GRAPH);
        this.legend = McpHtmlAuditSupport.graphLegendBody(this.html);
        this.executiveContext = McpHtmlAuditSupport.detailsBody(this.html, McpReportSections.EXECUTIVE_CONTEXT);
        this.scoreOverview = McpHtmlAuditSupport.detailsBody(this.html, McpReportSections.SCORE_OVERVIEW);
        this.proofMapping = McpHtmlAuditSupport.detailsBody(this.html, McpReportSections.PROOF_MAPPING);
        this.nodes = extractNodes(graphSection);
    }

    public static McpGraphSemanticTruthProfile from(String html, String packageState) {
        return new McpGraphSemanticTruthProfile(html, packageState);
    }

    public List<String> primaryOwnedObjectBlockers() {
        List<String> blockers = new ArrayList<>();
        List<GraphNode> primaryOwned = primaryOwnedNodes();
        if (primaryOwned.isEmpty()) {
            blockers.add("Business Flow Graph must mark exactly one primary owned business object with node owned strong semantics");
            return blockers;
        }
        if (primaryOwned.size() > 1) {
            blockers.add("Business Flow Graph marks multiple primary owned objects; found "
                    + labels(primaryOwned) + ". Keep one durable object owned by this package.");
        }

        GraphNode primary = primaryOwned.get(0);
        if (containsAny(primary.normalizedText(), PROOF_ARTIFACT_TOKENS)) {
            blockers.add("Primary owned graph node looks like proof/runtime evidence instead of a business object: "
                    + primary.label());
        }
        if (tokens(primary.label()).stream().allMatch(TECHNICAL_ONLY_TOKENS::contains)) {
            blockers.add("Primary owned graph node has only technical/proof tokens, not durable business language: "
                    + primary.label());
        }

        Set<String> stateOwned = stateOwnedTokens();
        if (!stateOwned.isEmpty() && stateOwned.stream().noneMatch(primary.normalizedText()::contains)) {
            blockers.add("Primary owned graph node does not echo package-state business_object tokens "
                    + stateOwned + "; node is " + primary.label());
        }
        return blockers;
    }

    public List<String> proofArtifactsNotOwnedBlockers() {
        List<String> blockers = new ArrayList<>();
        for (GraphNode node : nodes) {
            if (node.owned() && containsAny(node.normalizedText(), PROOF_ARTIFACT_TOKENS)) {
                blockers.add("Owned graph node must not be a supporting proof/runtime artifact: " + node.label());
            }
        }
        return blockers;
    }

    public List<String> frontierConsistencyBlockers() {
        List<String> blockers = new ArrayList<>();
        List<GraphNode> frontier = frontierNodes();
        if (frontier.isEmpty()) {
            blockers.add("Business Flow Graph must expose at least one capped/blocked frontier node");
            return blockers;
        }
        for (GraphNode node : frontier) {
            if (node.owned() || node.strong()) {
                blockers.add("Frontier graph node must not also be owned/strong: " + node.label());
            }
        }

        String frontierText = normalize(legend + "\n" + executiveContext + "\n" + scoreOverview);
        if (!containsAny(frontierText, List.of("frontier", "next closure", "not owned", "not-owned", "cap", "capped", "blocked"))) {
            blockers.add("Report text around the graph must explain the frontier/cap meaning, not only color the node");
        }

        Set<String> stateFrontier = stateFrontierTokens();
        if (!stateFrontier.isEmpty() && frontier.stream()
                .noneMatch(node -> stateFrontier.stream().anyMatch(node.normalizedText()::contains))) {
            blockers.add("Frontier graph nodes do not echo package-state primary_next_business_object/new_frontier tokens "
                    + stateFrontier + "; nodes are " + labels(frontier));
        }
        return blockers;
    }

    public List<String> proofAndScoreConsistencyBlockers() {
        List<String> blockers = new ArrayList<>();
        List<GraphNode> primaryOwned = primaryOwnedNodes();
        if (!primaryOwned.isEmpty()) {
            GraphNode primary = primaryOwned.get(0);
            Set<String> primaryTokens = tokens(primary.label());
            String proofScore = normalize(proofMapping + "\n" + scoreOverview);
            if (!primaryTokens.isEmpty() && primaryTokens.stream().noneMatch(proofScore::contains)) {
                blockers.add("Primary owned graph node is not traceable in Proof Mapping or Score Overview: "
                        + primary.label() + " tokens=" + primaryTokens);
            }
        }

        List<GraphNode> frontier = frontierNodes();
        if (!frontier.isEmpty()) {
            String reportCapText = normalize(executiveContext + "\n" + scoreOverview + "\n" + legend);
            boolean matched = frontier.stream()
                    .map(node -> tokens(node.label()))
                    .filter(tokens -> !tokens.isEmpty())
                    .anyMatch(tokens -> tokens.stream().anyMatch(reportCapText::contains));
            if (!matched) {
                blockers.add("Frontier graph node is not traceable in Executive Context, Score Overview, or Legend: "
                        + labels(frontier));
            }
        }
        return blockers;
    }

    private List<GraphNode> primaryOwnedNodes() {
        List<GraphNode> strongOwned = nodes.stream()
                .filter(node -> node.owned() && node.strong())
                .collect(Collectors.toList());
        if (!strongOwned.isEmpty()) {
            return strongOwned;
        }
        return nodes.stream()
                .filter(GraphNode::owned)
                .filter(node -> node.normalizedText().contains("primary owned")
                        || node.normalizedText().contains("owned object")
                        || node.normalizedText().contains("owned business"))
                .collect(Collectors.toList());
    }

    private List<GraphNode> frontierNodes() {
        return nodes.stream()
                .filter(node -> node.hasClass("cap") || node.hasClass("blocked"))
                .collect(Collectors.toList());
    }

    private Set<String> stateOwnedTokens() {
        return tokens(firstStateValue("business_object", "owned_business_object", "business_object_owned"));
    }

    private Set<String> stateFrontierTokens() {
        Set<String> result = new LinkedHashSet<>();
        result.addAll(tokens(firstStateValue("primary_next_business_object", "next_business_object")));
        result.addAll(tokens(firstStateValue("new_frontier", "frontier")));
        return result;
    }

    private String firstStateValue(String... keys) {
        for (String key : keys) {
            String value = McpRuleSupport.extractStringValue(packageState, key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static List<GraphNode> extractNodes(String graphSection) {
        List<ShapeMatch> shapes = new ArrayList<>();
        Matcher shape = NODE_SHAPE.matcher(graphSection);
        while (shape.find()) {
            shapes.add(new ShapeMatch(shape.start(), shape.end(), classAttribute(shape.group(1))));
        }

        List<GraphNode> result = new ArrayList<>();
        for (int i = 0; i < shapes.size(); i++) {
            ShapeMatch current = shapes.get(i);
            int end = i + 1 < shapes.size() ? shapes.get(i + 1).start : graphSection.length();
            String textSegment = graphSection.substring(current.end, end);
            result.add(new GraphNode(current.classes, title(textSegment), subtitle(textSegment)));
        }
        return result;
    }

    private static String title(String segment) {
        return textByClass(segment, "nt", 0);
    }

    private static String subtitle(String segment) {
        String secondary = textByClass(segment, "ns", 0);
        return secondary.isBlank() ? textByClass(segment, "", 1) : secondary;
    }

    private static String textByClass(String segment, String cssClass, int fallbackIndex) {
        List<String> all = new ArrayList<>();
        Matcher matcher = TEXT.matcher(segment);
        while (matcher.find()) {
            String text = strip(matcher.group(2));
            all.add(text);
            if (!cssClass.isBlank() && hasClass(matcher.group(1), cssClass)) {
                return text;
            }
        }
        return fallbackIndex < all.size() ? all.get(fallbackIndex) : "";
    }

    private static String classAttribute(String attributes) {
        Matcher matcher = Pattern.compile("(?is)class\\s*=\\s*[\"']([^\"']*)[\"']").matcher(attributes);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static boolean hasClass(String attributes, String cssClass) {
        return Pattern.compile("(?is)(^|\\s)" + Pattern.quote(cssClass) + "(\\s|$)")
                .matcher(classAttribute(attributes)).find();
    }

    private static Set<String> tokens(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return CandidateTextSupport.meaningfulTokens(value).stream()
                .filter(token -> !Set.of("record", "details", "visible", "current").contains(token))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean containsAny(String text, List<String> tokens) {
        for (String token : tokens) {
            if (text.contains(token.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static String labels(List<GraphNode> nodes) {
        return nodes.stream().map(GraphNode::label).collect(Collectors.joining(", "));
    }

    private static String strip(String value) {
        return McpHtmlAuditSupport.stripTags(value)
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String normalize(String value) {
        return value == null ? "" : strip(value).toLowerCase(Locale.ROOT);
    }

    private static final class ShapeMatch {
        private final int start;
        private final int end;
        private final String classes;

        private ShapeMatch(int start, int end, String classes) {
            this.start = start;
            this.end = end;
            this.classes = classes;
        }
    }

    private static final class GraphNode {
        private final String classes;
        private final String title;
        private final String subtitle;

        private GraphNode(String classes, String title, String subtitle) {
            this.classes = classes == null ? "" : classes;
            this.title = title == null ? "" : title;
            this.subtitle = subtitle == null ? "" : subtitle;
        }

        private boolean owned() {
            return hasClass("owned");
        }

        private boolean strong() {
            return hasClass("strong");
        }

        private boolean hasClass(String cssClass) {
            return Arrays.asList(classes.split("\\s+")).contains(cssClass);
        }

        private String label() {
            String combined = (title + " " + subtitle).trim();
            return combined.isBlank() ? "(unlabelled node)" : combined;
        }

        private String normalizedText() {
            return normalize(label());
        }
    }
}
