package com.ai.skills.atlas.rules.business;

import com.ai.skills.atlas.rules.AtlasValidationRule;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasText;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasSubgraphBusinessComplexityRule implements AtlasValidationRule {
    private static final Pattern NUMBERED_SUBGRAPH = Pattern.compile(
            "(?is)<details\\b[^>]*class=\"[^\"]*subdetails[^\"]*\"[^>]*>\\s*<summary>\\s*(3\\.\\d+\\s+[^<]+)</summary>(.*?)</details>");
    private static final Pattern NODE_TITLE = Pattern.compile("(?is)<text\\b[^>]*class=\"[^\"]*nt[^\"]*\"[^>]*>(.*?)</text>");
    private static final Pattern TEMPLATE_ROLE_NODE = Pattern.compile(
            "(?i)^(actor|intent|business object|observable result|visible action|source evidence|test method)\\b");

    private static final Set<String> TECHNICAL_TERMS = Set.of(
            "page",
            "selector",
            "component",
            "validation",
            "helper",
            "test",
            "mcp",
            "css",
            "html");

    @Override
    public String id() {
        return "atlas.candidate.subgraphs.business-complexity";
    }

    @Override
    public String expected() {
        return "Each business subgraph is sufficiently rich and auditable: multiple business nodes, transitions, and concrete business objects.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        Matcher matcher = NUMBERED_SUBGRAPH.matcher(context.lowerAtlasText());
        int subgraphs = 0;
        while (matcher.find()) {
            subgraphs++;
            String title = AtlasText.normalizeWhitespace(matcher.group(1));
            String body = matcher.group(2);
            int nodes = AtlasText.count(body, "class=\"node\"");
            int edges = AtlasText.count(body, "class=\"edge");
            Set<String> templateRoleLabels = templateRoleLabels(body);
            Set<String> businessLabels = businessNodeLabels(body);
            if (nodes < 4) {
                result.add(id(), "Business subgraph has too few business nodes: " + title + " -> " + nodes,
                        context.atlasHtml(),
                        "Each subgraph should explain a real mini-flow with at least four meaningful business nodes.");
            }
            if (edges < 3) {
                result.add(id(), "Business subgraph has too few transitions: " + title + " -> " + edges,
                        context.atlasHtml(),
                        "Each subgraph should show how the business object moves, not just isolated labels.");
            }
            if (!templateRoleLabels.isEmpty()) {
                result.add(id(), "Business subgraph still uses template role nodes: " + title + " -> "
                                + templateRoleLabels,
                        context.atlasHtml(),
                        "Replace Actor/Intent/Object/Result template nodes with concrete business objects and business states discovered from source, for example customer account, profile identity, contact address, invoice record or retained profile state.");
            }
            if (businessLabels.size() < 3) {
                result.add(id(), "Business subgraph is not object-rich enough: " + title + " -> " + businessLabels.size(),
                        context.atlasHtml(),
                        "Use several distinct product/business objects or observable business states discovered from the source package; do not rely on technical layer names as graph nodes.");
            }
        }
        if (subgraphs < 4) {
            result.add(id(), "Atlas has too few numbered business subgraphs: " + subgraphs,
                    context.atlasHtml(),
                    "Keep multiple subgraphs that deepen different real application flow slices, independent of the application domain.");
        }
    }

    private static Set<String> businessNodeLabels(String body) {
        Set<String> labels = new HashSet<>();
        Matcher matcher = NODE_TITLE.matcher(body);
        while (matcher.find()) {
            String label = AtlasText.normalizeLabel(AtlasText.stripTags(matcher.group(1)));
            if (label.length() >= 4
                    && !TEMPLATE_ROLE_NODE.matcher(label).find()
                    && TECHNICAL_TERMS.stream().noneMatch(label::contains)) {
                labels.add(label);
            }
        }
        return labels;
    }

    private static Set<String> templateRoleLabels(String body) {
        Set<String> labels = new HashSet<>();
        Matcher matcher = NODE_TITLE.matcher(body);
        while (matcher.find()) {
            String label = AtlasText.normalizeLabel(AtlasText.stripTags(matcher.group(1)));
            if (TEMPLATE_ROLE_NODE.matcher(label).find()) {
                labels.add(label);
            }
        }
        return labels;
    }
}
