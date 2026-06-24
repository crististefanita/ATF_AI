package com.endava.ai.atlas.rules.business;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasBusinessObjectCenteredRule implements AtlasValidationRule {
    private static final Pattern NODE_TEXT_PATTERN = Pattern.compile("(?is)<text\\b[^>]*class=\"[^\"]*nt[^\"]*\"[^>]*>(.*?)</text>");
    private static final Pattern TAG_PATTERN = Pattern.compile("(?is)<[^>]+>");

    private static final List<String> REQUIRED_OBJECTS = List.of(
            "invoice",
            "payment",
            "account",
            "profile",
            "message",
            "cart");

    private static final List<String> TECHNICAL_NODE_TOKENS = List.of(
            "page",
            " selector",
            "css",
            "html",
            "mcp",
            "test contract");

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
        String graphSection = sectionAfter(context.lowerAtlasText(), "business flow graph", 16_000);
        for (String object : REQUIRED_OBJECTS) {
            if (!graphSection.contains(object)) {
                result.add(id(), "Business map missing central business object token: " + object,
                        context.atlasHtml(),
                        "Organize the main map and subgraphs around real product objects such as invoice, payment, account, profile, message, cart, order or favorite.");
            }
        }
        String nodeText = nodeTitleText(graphSection);
        for (String token : TECHNICAL_NODE_TOKENS) {
            if (nodeText.contains(token)) {
                result.add(id(), "Business graph appears to promote technical mechanics as node text: " + token.trim(),
                        context.atlasHtml(),
                        "Keep page objects, selectors, CSS, HTML, MCP and test-contract language outside business graph nodes; use them only as evidence in trace tables.");
            }
        }
    }

    private static String sectionAfter(String html, String marker, int maxChars) {
        int index = html.indexOf(marker);
        if (index < 0) {
            return "";
        }
        return html.substring(index, Math.min(html.length(), index + maxChars));
    }

    private static String nodeTitleText(String graphSection) {
        Matcher matcher = NODE_TEXT_PATTERN.matcher(graphSection);
        StringBuilder text = new StringBuilder();
        while (matcher.find()) {
            text.append(TAG_PATTERN.matcher(matcher.group(1)).replaceAll(" ")).append(' ');
        }
        return text.toString().replaceAll("\\s+", " ").trim();
    }
}
