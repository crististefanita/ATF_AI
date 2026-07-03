package com.endava.ai.skills.atlas.rules.graph.proof;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.endava.ai.skills.atlas.rules.AtlasText;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasMainGraphNodeMaturityProofRule implements AtlasValidationRule {
    private static final Pattern PRIMARY_GRAPH_SECTION = Pattern.compile(
            "(?is)<details\\b[^>]*(?:id=[\"']business-flow-graph[\"']|data-atlas-section=[\"']2\\. business flow graph[\"'])[^>]*>(.*?)</details>\\s*<details\\b[^>]*data-atlas-section=[\"']3\\.");
    private static final Pattern SVG_NODE_TITLE = Pattern.compile("(?is)<text\\b[^>]*class=[\"'][^\"']*\\bnt\\b[^\"']*[\"'][^>]*>(.*?)</text>");
    private static final Pattern SVG_NODE_GROUP = Pattern.compile("(?is)<g\\b([^>]*)>(?:(?!</g>).)*?<text\\b[^>]*class=[\"'][^\"']*\\bnt\\b[^\"']*[\"'][^>]*>(.*?)</text>(?:(?!</g>).)*?</g>");
    private static final Pattern PROOF_STRIP = Pattern.compile("(?is)<details\\b[^>]*data-atlas-role=[\"']business-proof-strip[\"'][^>]*>(.*?)</details>");
    private static final Pattern TABLE_ROW = Pattern.compile("(?is)<tr\\b[^>]*>(.*?)</tr>");

    private static final List<String> NON_BUSINESS_LABELS = List.of(
            "observable result",
            "source evidence",
            "test method");

    @Override
    public String id() {
        return "atlas.candidate.main-graph-node.maturity-proof";
    }

    @Override
    public String expected() {
        return "Primary Business Flow Graph nodes expose their maturity in-node and reconcile with local proof-strip rows that state method, layer, maturity, evidence and limit.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String primary = primaryGraphSection(context.atlasText());
        if (primary.isBlank()) {
            result.add(id(), "Cannot locate primary 2. Business Flow Graph section",
                    context.atlasHtml(),
                    "Keep the primary graph in <details id=\"business-flow-graph\" data-atlas-section=\"2. business flow graph\">.");
            return;
        }

        List<String> graphNodes = graphNodeLabels(primary);
        if (graphNodes.size() < 5) {
            result.add(id(), "Primary graph has too few auditable business nodes: " + graphNodes.size(),
                    context.atlasHtml(),
                    "Publish a substantial primary graph, then add one local proof-strip row per graph node.");
        }

        List<String> proofRows = proofRows(primary);
        if (proofRows.isEmpty()) {
            result.add(id(), "Primary graph is missing a business-proof strip table",
                    context.atlasHtml(),
                    "Add a collapsed data-atlas-role=\"business-proof-strip\" table below the graph legend.");
            return;
        }
        if (proofRows.size() < graphNodes.size()) {
            result.add(id(), "Primary graph proof strip has fewer rows than graph nodes: rows="
                            + proofRows.size() + ", nodes=" + graphNodes.size(),
                    context.atlasHtml(),
                    "Add one proof-strip row for every primary graph node. The row must name the same business object and include method, layer path, maturity, business evidence and limit.");
        }

        for (String node : graphNodes) {
            String matchingRow = findMatchingRow(node, proofRows);
            if (matchingRow.isBlank()) {
                result.add(id(), "Primary graph node is not backed by a local proof-strip row: " + node,
                        context.atlasHtml(),
                        "Add a proof-strip row whose first business-object cell names this graph node and maps it to method, layer path, maturity, business evidence and limit.");
                continue;
            }
            String expectedMaturity = expectedMaturity(matchingRow);
            if (!nodeHasInGraphMaturity(primary, node, expectedMaturity)) {
                result.add(id(), "Primary graph node lacks matching in-graph maturity marker: " + node
                                + (expectedMaturity.isBlank() ? "" : " expected " + expectedMaturity),
                        context.atlasHtml(),
                        "Add data-maturity=\"" + (expectedMaturity.isBlank() ? "owned|partial|diagnostic|surface-only|frontier" : expectedMaturity)
                                + "\" to the same SVG node <g> or visible node shape that renders '" + node
                                + "'. Derive the value from that node's proof-strip maturity; do not rely on prompt-only guidance or distant prose.");
            }
            if (!hasMaturityCue(matchingRow)) {
                result.add(id(), "Primary graph proof row does not state maturity for node: " + node,
                        context.atlasHtml(),
                        "Each graph-node proof row must state owned, partial, diagnostic, surface-only or frontier maturity so the graph does not overclaim ownership.");
            }
            if (!hasLimitCue(matchingRow)) {
                result.add(id(), "Primary graph proof row does not state a limit/boundary for node: " + node,
                        context.atlasHtml(),
                        "Each graph-node proof row must keep a local limit/boundary/cap statement next to its business evidence.");
            }
        }
    }

    private static String primaryGraphSection(String html) {
        Matcher matcher = PRIMARY_GRAPH_SECTION.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return AtlasText.sectionAfter(html, "2. Business Flow Graph", 20_000);
    }

    private static List<String> graphNodeLabels(String section) {
        List<String> labels = new ArrayList<>();
        Matcher matcher = SVG_NODE_TITLE.matcher(section);
        while (matcher.find()) {
            String label = AtlasText.normalizeLabel(AtlasText.stripTags(matcher.group(1)));
            if (label.length() >= 4 && !NON_BUSINESS_LABELS.contains(label)) {
                labels.add(label);
            }
        }
        return labels;
    }

    private static boolean nodeHasInGraphMaturity(String section, String node, String expectedMaturity) {
        String normalized = AtlasText.normalizeLabel(node);
        Matcher matcher = SVG_NODE_GROUP.matcher(section);
        while (matcher.find()) {
            String label = AtlasText.normalizeLabel(AtlasText.stripTags(matcher.group(2)));
            if (!normalized.equals(label)) {
                continue;
            }
            String nodeMarkup = matcher.group(0).toLowerCase(Locale.ROOT);
            String groupAttributes = matcher.group(1).toLowerCase(Locale.ROOT);
            return hasMachineReadableMaturity(groupAttributes, expectedMaturity)
                    || hasMachineReadableMaturity(nodeMarkup, expectedMaturity);
        }
        return false;
    }

    private static boolean hasMachineReadableMaturity(String markup, String expectedMaturity) {
        boolean hasMarker = markup.contains("data-maturity=")
                || markup.contains("data-atlas-maturity=")
                || markup.contains("data-business-maturity=")
                || Pattern.compile("(?is)\\baria-label\\s*=\\s*[\"'][^\"']*\\b(owned|partial|diagnostic|surface-only|surface only|frontier|boundary)\\b")
                .matcher(markup).find();
        if (!hasMarker || expectedMaturity.isBlank()) {
            return hasMarker;
        }
        return markup.contains(expectedMaturity);
    }

    private static String expectedMaturity(String row) {
        String lower = row.toLowerCase(Locale.ROOT);
        if (lower.contains("surface-only") || lower.contains("surface only")) {
            return "surface-only";
        }
        if (lower.contains("diagnostic")) {
            return "diagnostic";
        }
        if (lower.contains("partial") || lower.contains("boundary")) {
            return "partial";
        }
        if (lower.contains("frontier")) {
            return "frontier";
        }
        if (lower.contains("owned")) {
            return "owned";
        }
        return "";
    }

    private static List<String> proofRows(String section) {
        Matcher stripMatcher = PROOF_STRIP.matcher(section);
        if (!stripMatcher.find()) {
            return List.of();
        }
        List<String> rows = new ArrayList<>();
        Matcher rowMatcher = TABLE_ROW.matcher(stripMatcher.group(1));
        while (rowMatcher.find()) {
            String plain = AtlasText.normalizeLabel(AtlasText.plainText(rowMatcher.group(1)));
            if (!plain.isBlank() && !plain.contains("business object method layer")) {
                rows.add(plain);
            }
        }
        return rows;
    }

    private static String findMatchingRow(String node, List<String> rows) {
        String normalized = AtlasText.normalizeLabel(node);
        for (String row : rows) {
            if (row.contains(normalized) || normalized.contains(firstCell(row))) {
                return row;
            }
        }
        String[] nodeTokens = normalized.split("\\s+");
        for (String row : rows) {
            int matched = 0;
            for (String token : nodeTokens) {
                if (token.length() >= 4 && row.contains(token)) {
                    matched++;
                }
            }
            if (matched >= Math.min(2, nodeTokens.length)) {
                return row;
            }
        }
        return "";
    }

    private static String firstCell(String normalizedRow) {
        int methodIndex = normalizedRow.indexOf(" ");
        return methodIndex < 0 ? normalizedRow : normalizedRow.substring(0, methodIndex);
    }

    private static boolean hasMaturityCue(String row) {
        String lower = row.toLowerCase(Locale.ROOT);
        return AtlasSemanticVocabulary.maturityFamilyCoverage(lower) > 0
                || lower.contains("owned")
                || lower.contains("partial")
                || lower.contains("diagnostic")
                || lower.contains("surface")
                || lower.contains("frontier")
                || lower.contains("boundary");
    }

    private static boolean hasLimitCue(String row) {
        String lower = row.toLowerCase(Locale.ROOT);
        return AtlasSemanticVocabulary.containsAny(lower, AtlasSemanticVocabulary.LIMIT_MARKERS)
                || AtlasSemanticVocabulary.containsAny(lower, AtlasSemanticVocabulary.CAP_MARKERS)
                || lower.contains("boundary")
                || lower.contains("frontier");
    }
}
