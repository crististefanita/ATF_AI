package com.ai.skills.atlas.rules.graph;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.ai.skills.atlas.rules.AtlasSourceInventory;
import com.ai.skills.atlas.rules.AtlasText;
import com.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasSubgraphBusinessUnderstandingStripRule implements AtlasValidationRule {
    private static final Pattern SUBGRAPH_START = Pattern.compile(
            "(?is)<details\\b[^>]*class=\"[^\"]*subdetails[^\"]*\"[^>]*>\\s*<summary>\\s*(3\\.\\d+\\s+[^<]+)</summary>");
    private static final Pattern METHOD_TOKEN = Pattern.compile("\\b([a-z][A-Za-z0-9_]*)\\(\\)");

    @Override
    public String id() {
        return "atlas.candidate.subgraph.business-understanding-strips";
    }

    @Override
    public String expected() {
        return "Each 3.x business subgraph has its own compact understanding strip: node/flow -> test/method -> layer path -> proven business understanding -> limit.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String section = AtlasText.sectionBetween(context.atlasText(), "Business Flow Understanding", "Business Evidence Tables");
        if (section.isBlank()) {
            section = AtlasText.sectionBetween(context.lowerAtlasText(), "business flow understanding", "business evidence tables");
        }
        if (section.isBlank()) {
            result.add(id(), "Cannot find Business Flow Understanding section", context.atlasHtml(),
                    "Keep business subgraphs under a dedicated Business Flow Understanding section.");
            return;
        }
        AtlasSourceInventory source = AtlasSourceInventory.load(context);
        List<SubgraphBlock> blocks = subgraphBlocks(section);
        Set<String> stripSignatures = new LinkedHashSet<>();
        for (SubgraphBlock block : blocks) {
            String title = block.title();
            String body = block.body();
            String lowerBody = body.toLowerCase(Locale.ROOT);
            if (!AtlasSemanticVocabulary.containsAny(lowerBody, "proof strip", "banda dovada", "banda de dovada", "banda intelegere", "dovada nod")) {
                result.add(id(), "Business subgraph has no local understanding strip: " + title,
                        context.atlasHtml(),
                        "Add 2-3 compact rows under this subgraph: node/flow, test/method, layer path, proven business understanding and limit.");
                continue;
            }
            int rows = Math.max(0, AtlasText.count(body, "<tr>") - 1);
            if (rows < 2) {
                result.add(id(), "Business subgraph understanding strip has too few rows: " + title + " -> " + rows,
                        context.atlasHtml(),
                        "Each subgraph strip should prove at least two important nodes/flows, not just label the graph.");
            }
            for (String concept : new String[]{"test", "method", "layer", "business", "limit"}) {
                if (!lowerBody.contains(concept)) {
                    result.add(id(), "Business subgraph understanding strip misses concept " + concept + ": " + title,
                            context.atlasHtml(),
                            "The strip must connect business understanding to source test, method, layer path and comprehension limit.");
                }
            }
            Set<String> methods = extractMethods(body);
            if (methods.isEmpty()) {
                result.add(id(), "Business subgraph understanding strip has no source method token: " + title,
                        context.atlasHtml(),
                        "Use at least one methodName() token in each local proof strip so the subgraph is source-reopenable.");
            }
            for (String method : methods) {
                if (!source.containsMethod(method)) {
                    result.add(id(), "Subgraph proof strip method is not found in source: " + method + "()",
                            context.atlasHtml(),
                            "Each methodName() in a local proof strip should exist in the analyzed source package or UI layers.");
                }
            }
            if (!methods.isEmpty()) {
                stripSignatures.add(String.join("|", methods));
            }
        }
        int subgraphs = blocks.size();
        if (subgraphs < 4) {
            result.add(id(), "Too few business subgraphs for strip validation: " + subgraphs,
                    context.atlasHtml(),
                    "Keep several 3.x business subgraphs and give each one its own local understanding strip.");
        }
        if (subgraphs >= 4 && stripSignatures.size() < Math.min(4, subgraphs)) {
            result.add(id(), "Business subgraph understanding strips are not distinct enough: "
                            + stripSignatures.size() + " distinct strips for " + subgraphs + " subgraphs",
                    context.atlasHtml(),
                    "Each local proof strip should prove a different business slice, not reuse the same method/layer evidence everywhere.");
        }
    }

    private static List<SubgraphBlock> subgraphBlocks(String section) {
        Matcher matcher = SUBGRAPH_START.matcher(section);
        List<SubgraphBlock> starts = new ArrayList<>();
        while (matcher.find()) {
            starts.add(new SubgraphBlock(
                    AtlasText.normalizeWhitespace(matcher.group(1)),
                    matcher.start(),
                    matcher.end(),
                    ""));
        }
        List<SubgraphBlock> blocks = new ArrayList<>();
        for (int i = 0; i < starts.size(); i++) {
            SubgraphBlock current = starts.get(i);
            int bodyEnd = i + 1 < starts.size() ? starts.get(i + 1).start() : section.length();
            blocks.add(new SubgraphBlock(current.title(), current.start(), current.end(),
                    section.substring(current.end(), bodyEnd)));
        }
        return blocks;
    }

    private static Set<String> extractMethods(String html) {
        Matcher matcher = METHOD_TOKEN.matcher(html);
        Set<String> methods = new LinkedHashSet<>();
        while (matcher.find()) {
            methods.add(matcher.group(1));
        }
        return methods;
    }

    private static final class SubgraphBlock {
        private final String title;
        private final int start;
        private final int end;
        private final String body;

        private SubgraphBlock(String title, int start, int end, String body) {
            this.title = title;
            this.start = start;
            this.end = end;
            this.body = body;
        }

        private String title() {
            return title;
        }

        private int start() {
            return start;
        }

        private int end() {
            return end;
        }

        private String body() {
            return body;
        }
    }
}
