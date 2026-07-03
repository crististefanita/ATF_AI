package com.endava.ai.skills.atlas.rules.graph.proof;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.contract.yaml.AtlasYamlGraphReportingCatalog;
import com.endava.ai.skills.atlas.contract.yaml.AtlasYamlGraphReportingContract;
import com.endava.ai.skills.atlas.rules.AtlasText;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasSubgraphSemanticDensityRule implements AtlasValidationRule {
    private static final AtlasYamlGraphReportingContract CONTRACT =
            AtlasYamlGraphReportingCatalog.require("atlas.candidate.subgraph.semantic-density");
    private static final Pattern SUBGRAPH_DETAILS = Pattern.compile(
            "(?is)<details\\b[^>]*class=\"[^\"]*subdetails[^\"]*\"[^>]*>\\s*<summary>\\s*(3\\.\\d+\\s+[^<]+)</summary>(.*?)</details>");
    private static final Pattern NODE_TITLE = Pattern.compile(
            "(?is)<text\\b[^>]*class=[\"'][^\"']*\\bnt\\b[^\"']*[\"'][^>]*>(.*?)</text>");

    @Override
    public String id() {
        return CONTRACT.id();
    }

    @Override
    public String expected() {
        return "Atlas 3.x business subgraphs may share visual grammar, but each must expose enough domain-specific node labels to avoid template clones.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String section = AtlasText.sectionBetween(context.atlasText(), "Business Flow Understanding", "Business Evidence Tables");
        if (section.isBlank()) {
            result.add(id(), "Cannot find Business Flow Understanding section", context.atlasHtml(),
                    "Keep 3.x subgraphs under Business Flow Understanding so semantic-density can be audited.");
            return;
        }

        Matcher matcher = SUBGRAPH_DETAILS.matcher(section);
        int subgraphs = 0;
        Set<String> distinctSignatures = new LinkedHashSet<>();
        while (matcher.find()) {
            subgraphs++;
            String title = AtlasText.normalizeWhitespace(matcher.group(1));
            Set<String> domainLabels = domainLabels(matcher.group(2));
            if (domainLabels.size() < CONTRACT.minDomainNodeLabelsPerSubgraph()) {
                result.add(id(), "Subgraph is too template-like: " + title
                                + " has only " + domainLabels.size() + " domain node labels " + domainLabels,
                        context.atlasHtml(),
                        "Keep the shared Actor/Intent/Object/Result grammar, but add at least "
                                + CONTRACT.minDomainNodeLabelsPerSubgraph()
                                + " source/domain-specific labels inside each subgraph SVG.");
            }
            if (!domainLabels.isEmpty()) {
                distinctSignatures.add(String.join("|", domainLabels));
            }
        }
        if (subgraphs == 0) {
            result.add(id(), "No numbered 3.x business subgraphs found", context.atlasHtml(),
                    "Publish several source-specific 3.x subgraphs before claiming Atlas business understanding.");
            return;
        }
        int requiredDistinct = Math.min(CONTRACT.minDistinctSubgraphSignatures(), subgraphs);
        if (distinctSignatures.size() < requiredDistinct) {
            result.add(id(), "Business subgraph semantic signatures are too repetitive: "
                            + distinctSignatures.size() + " distinct signatures for " + subgraphs + " subgraphs",
                    context.atlasHtml(),
                    "Avoid visually identical template clones by giving each subgraph distinct domain-bearing node labels.");
        }
    }

    private static Set<String> domainLabels(String html) {
        Matcher matcher = NODE_TITLE.matcher(html);
        Set<String> labels = new LinkedHashSet<>();
        while (matcher.find()) {
            String label = AtlasText.normalizeLabel(AtlasText.stripTags(matcher.group(1)));
            String domain = domainPart(label);
            if (!domain.isBlank()) {
                labels.add(domain);
            }
        }
        return labels;
    }

    private static String domainPart(String label) {
        String normalized = label.toLowerCase(Locale.ROOT).trim();
        for (String generic : CONTRACT.genericNodeLabels()) {
            String token = generic.toLowerCase(Locale.ROOT).trim();
            if (normalized.equals(token)) {
                return "";
            }
            if (normalized.startsWith(token + " ")) {
                if (token.equals("actor") || token.equals("observable result")) {
                    return "";
                }
                return normalized.substring(token.length()).trim();
            }
        }
        return normalized;
    }
}
