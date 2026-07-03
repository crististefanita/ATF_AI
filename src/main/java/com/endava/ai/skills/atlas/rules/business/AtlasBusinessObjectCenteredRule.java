package com.endava.ai.skills.atlas.rules.business;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasBusinessObjectCenteredRule implements AtlasValidationRule {
    private static final Pattern NODE_TEXT_PATTERN = Pattern.compile("(?is)<text\\b[^>]*class=\"[^\"]*nt[^\"]*\"[^>]*>(.*?)</text>");
    private static final Pattern BUSINESS_OBJECT_CELL_PATTERN = Pattern.compile("(?is)<tr>\\s*<td>.*?</td>\\s*<td>(.*?)</td>");

    private static final List<String> TECHNICAL_NODE_TOKENS = List.of(
            "page",
            " selector",
            "css",
            "html",
            "mcp",
            "test contract",
            "component",
            "validation",
            "helper");

    private static final List<String> TECHNICAL_OBJECT_TOKENS = List.of(
            "page",
            "selector",
            "component",
            "validation",
            "helper",
            "test support",
            "test contract",
            "mcp");

    @Override
    public String id() {
        return "atlas.candidate.business-object-centered";
    }

    @Override
    public String expected() {
        return "Atlas graph nodes are organized around real business objects, while technical layers stay as evidence, not primary business nodes.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String graphSection = AtlasText.sectionAfter(context.lowerAtlasText(), "business flow graph", 16_000);
        Set<String> businessNodes = nodeTitleLabels(graphSection);
        String nodeText = String.join(" ", businessNodes);
        if (businessNodes.size() < 5) {
            result.add(id(), "Business graph has too few distinct business node labels: " + businessNodes.size(),
                    context.atlasHtml(),
                    "Keep the map generic but substantial: extract several distinct product/business objects or visible business states from the analyzed source package.");
        }
        for (String token : TECHNICAL_NODE_TOKENS) {
            if (nodeText.contains(token)) {
                result.add(id(), "Business graph appears to promote technical mechanics as node text: " + token.trim(),
                        context.atlasHtml(),
                        "Keep page objects, selectors, CSS, HTML, MCP and test-contract language outside business graph nodes; use them only as evidence in trace tables.");
            }
        }
        List<String> businessObjects = businessObjectCells(context.atlasText());
        if (distinctBusinessLabels(businessObjects).size() < 5) {
            result.add(id(), "Source-to-Business trace has too few distinct business objects",
                    context.atlasHtml(),
                    "The Business object column should expose a diverse set of product objects or observable business states discovered from the source, without relying on domain-specific hardcoded names.");
        }
        for (String object : businessObjects) {
            String normalized = AtlasText.normalizeLabel(object);
            for (String token : TECHNICAL_OBJECT_TOKENS) {
                if (normalized.contains(token)) {
                    result.add(id(), "Source-to-Business object cell is technical, not business: " + object,
                            context.atlasHtml(),
                            "The Business object column must name a product object or observable business state; keep pages, actions, selectors, components, validation and helpers in evidence columns only.");
                }
            }
        }
    }

    private static Set<String> nodeTitleLabels(String graphSection) {
        Matcher matcher = NODE_TEXT_PATTERN.matcher(graphSection);
        Set<String> labels = new TreeSet<>();
        while (matcher.find()) {
            String label = AtlasText.normalizeLabel(AtlasText.stripTags(matcher.group(1)));
            if (label.length() >= 4 && !isTechnicalLabel(label)) {
                labels.add(label);
            }
        }
        return labels;
    }

    private static Set<String> distinctBusinessLabels(List<String> rawLabels) {
        Set<String> labels = new TreeSet<>();
        for (String rawLabel : rawLabels) {
            String label = AtlasText.normalizeLabel(rawLabel);
            if (label.length() >= 4 && !isTechnicalLabel(label)) {
                labels.add(label);
            }
        }
        return labels;
    }

    private static boolean isTechnicalLabel(String label) {
        for (String token : TECHNICAL_OBJECT_TOKENS) {
            if (label.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> businessObjectCells(String html) {
        ArrayList<String> objects = new ArrayList<>();
        String traceSection = sourceTraceSection(html);
        if (traceSection.isBlank()) {
            return objects;
        }
        Matcher matcher = BUSINESS_OBJECT_CELL_PATTERN.matcher(traceSection);
        while (matcher.find()) {
            String cell = AtlasText.plainText(matcher.group(1));
            if (!cell.isBlank()) {
                objects.add(cell);
            }
        }
        return objects;
    }

    private static String sourceTraceSection(String html) {
        String lower = html.toLowerCase(Locale.ROOT);
        int start = AtlasText.firstExistingIndex(lower,
                "data-atlas-section=\"5. source-to-business trace\"",
                "5. source-to-business trace",
                "trace sursa-catre-business");
        if (start < 0) {
            return "";
        }
        int end = AtlasText.firstExistingIndex(lower.substring(start), "<summary>5.1", "6. atlas test audit", "6. audit");
        if (end < 0) {
            return html.substring(start);
        }
        return html.substring(start, start + end);
    }

}
