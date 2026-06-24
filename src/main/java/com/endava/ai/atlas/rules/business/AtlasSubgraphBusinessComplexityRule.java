package com.endava.ai.atlas.rules.business;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasSubgraphBusinessComplexityRule implements AtlasValidationRule {
    private static final Pattern NUMBERED_SUBGRAPH = Pattern.compile(
            "(?is)<details\\b[^>]*class=\"[^\"]*subdetails[^\"]*\"[^>]*>\\s*<summary>\\s*(3\\.\\d+\\s+[^<]+)</summary>(.*?)</details>");
    private static final List<String> BUSINESS_OBJECT_TOKENS = List.of(
            "invoice",
            "order",
            "payment",
            "account",
            "profile",
            "message",
            "favorite",
            "cart",
            "checkout",
            "purchase",
            "support",
            "security");

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
            String title = matcher.group(1).replaceAll("\\s+", " ").trim();
            String body = matcher.group(2);
            int nodes = count(body, "class=\"node\"");
            int edges = count(body, "class=\"edge");
            int businessTokens = distinctTokenCount(body, BUSINESS_OBJECT_TOKENS);
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
            if (businessTokens < 2) {
                result.add(id(), "Business subgraph is not object-rich enough: " + title,
                        context.atlasHtml(),
                        "Use real business objects in subgraph labels: invoice, order, payment, account, profile, message, favorite, cart, etc.");
            }
        }
        if (subgraphs < 4) {
            result.add(id(), "Atlas has too few numbered business subgraphs: " + subgraphs,
                    context.atlasHtml(),
                    "Keep multiple subgraphs that deepen the main map: commerce, account, support/profile, and diagnostic or persistence lanes.");
        }
    }

    private static int count(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    private static int distinctTokenCount(String text, List<String> tokens) {
        int count = 0;
        for (String token : tokens) {
            if (text.contains(token)) {
                count++;
            }
        }
        return count;
    }
}
