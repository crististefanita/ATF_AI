package com.endava.ai.skills.atlas.rules.graph;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.contract.yaml.AtlasYamlGraphReportingCatalog;
import com.endava.ai.skills.atlas.contract.yaml.AtlasYamlGraphReportingContract;
import com.endava.ai.skills.atlas.report.AtlasReportModel;
import com.endava.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.endava.ai.skills.atlas.rules.AtlasSourceInventory;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasGraphNodeBusinessProofStripRule implements AtlasValidationRule {
    private static final AtlasYamlGraphReportingContract CONTRACT =
            AtlasYamlGraphReportingCatalog.require("atlas.candidate.graph-node.business-proof-strip");
    private static final Pattern METHOD_TOKEN = Pattern.compile("\\b([a-z][A-Za-z0-9_]*)\\(\\)");

    @Override
    public String id() {
        return CONTRACT.id();
    }

    @Override
    public String expected() {
        return "Every major business graph has a compact business-proof strip: node/flow -> test/method -> layer path -> business understanding evidence -> limit.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        AtlasReportModel report = AtlasReportModel.parse(context.atlasText());
        String graphArea = report.businessGraphText();
        if (graphArea.isBlank()) {
            result.add(id(), "Cannot find Business Flow Graph / Business Flow Understanding area",
                    context.atlasHtml(),
                    "Keep graph sections visible in the atlas before validating node-level business proof strips.");
            return;
        }
        String lowerGraphArea = graphArea.toLowerCase(Locale.ROOT);
        if (!AtlasSemanticVocabulary.containsAny(lowerGraphArea, CONTRACT.proofStripLabels().toArray(String[]::new))) {
            result.add(id(), "Graph area has no business-proof strip for graph nodes or subflows",
                    context.atlasHtml(),
                    "Under each major graph/subgraph add a compact proof strip: node/flow -> test/method -> layer path -> business understanding evidence -> limit.");
            return;
        }
        if (!collapsibleProofStripPattern().matcher(graphArea).find()) {
            result.add(id(), "Main graph proof strip is not published as a collapsed Atlas subdetail",
                    context.atlasHtml(),
                    "Publish the main graph proof as <details class=\"subdetails\" data-atlas-role=\""
                            + CONTRACT.proofStripRole() + "\"><summary>"
                            + String.join(" - ", CONTRACT.proofStripLabels()) + "</summary>...");
        }
        for (String concept : CONTRACT.requiredConcepts()) {
            if (!lowerGraphArea.contains(concept)) {
                result.add(id(), "Business-proof strip misses concept: " + concept,
                        context.atlasHtml(),
                        "A proof strip should make the graph auditable through source test, method, layer path, business evidence and comprehension limit.");
            }
        }
        validateSourceBackedMethods(context, result, graphArea);
    }

    private void validateSourceBackedMethods(AtlasContext context,
                                             AtlasValidationResult result,
                                             String graphArea) {
        AtlasSourceInventory source = AtlasSourceInventory.load(context);
        Set<String> methods = extractMethods(graphArea);
        if (methods.size() < CONTRACT.minSourceBackedMethods()) {
            result.add(id(), "Graph proof strips expose too few concrete source methods: " + methods.size(),
                    context.atlasHtml(),
                    "Name enough representative methodName() entries in proof strips so graph nodes can be reopened from source.");
        }
        for (String method : methods) {
            if (!source.containsMethod(method)) {
                result.add(id(), "Graph proof strip method is not found in analyzed source: " + method + "()",
                        context.atlasHtml(),
                        "Every methodName() in graph proof strips should exist in the analyzed test package or traversed UI layers.");
            }
        }
    }

    private static Pattern collapsibleProofStripPattern() {
        String labels = CONTRACT.proofStripLabels().stream()
                .map(Pattern::quote)
                .reduce((left, right) -> left + "|" + right)
                .orElse("proof\\s+strip");
        return Pattern.compile(
                "(?is)<details\\b[^>]*class\\s*=\\s*[\"'][^\"']*\\bsubdetails\\b[^\"']*[\"'][^>]*"
                        + "(?:data-atlas-role\\s*=\\s*[\"']" + Pattern.quote(CONTRACT.proofStripRole())
                        + "[\"'][^>]*)?>\\s*"
                        + "<summary\\b[^>]*>\\s*(?:" + labels + ")",
                Pattern.CASE_INSENSITIVE);
    }

    private static Set<String> extractMethods(String html) {
        Matcher matcher = METHOD_TOKEN.matcher(html);
        Set<String> methods = new LinkedHashSet<>();
        while (matcher.find()) {
            methods.add(matcher.group(1));
        }
        return methods;
    }
}
