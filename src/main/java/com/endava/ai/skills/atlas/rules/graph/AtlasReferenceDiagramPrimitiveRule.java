package com.endava.ai.skills.atlas.rules.graph;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.contract.yaml.AtlasYamlGraphReportingCatalog;
import com.endava.ai.skills.atlas.contract.yaml.AtlasYamlGraphReportingContract;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasReferenceDiagramPrimitiveRule implements AtlasValidationRule {
    private static final AtlasYamlGraphReportingContract CONTRACT =
            AtlasYamlGraphReportingCatalog.require("atlas.candidate.graph.reference-primitives");
    private static final Pattern LEFT_ANCHORED_NODE_TEXT = Pattern.compile(
            "(?is)<text\\b[^>]*class\\s*=\\s*[\"'][^\"']*\\b(?:nt|ns)\\b[^\"']*[\"'][^>]*\\bx\\s*=\\s*[\"'](?:16|20)[\"']");

    @Override
    public String id() {
        return "atlas.candidate.graph.reference-primitives";
    }

    @Override
    public String expected() {
        return "Candidate atlas graphs reuse the same primitive vocabulary as html-ex reports: graph-panel, diag, node, nt, ns, and edge.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        for (String className : CONTRACT.requiredClasses()) {
            if (!hasClass(html, className)) {
                result.add(id(), "Missing reference graph primitive class: " + className, context.atlasHtml(),
                        CONTRACT.repair());
            }
        }
        if (!visibleNodeShape(context.atlasText())) {
            result.add(id(), "Graph node class is not attached to visible SVG shapes", context.atlasHtml(),
                    CONTRACT.repair());
        }
        if (forbiddenWrapperClass(context.atlasText())) {
            result.add(id(), "Graph uses class=\"node\" on <g> wrappers", context.atlasHtml(),
                    CONTRACT.repair());
        }
        Matcher leftText = LEFT_ANCHORED_NODE_TEXT.matcher(context.atlasText());
        if (leftText.find()) {
            result.add(id(), "Graph node text is positioned like a left label instead of centered html-ex typography",
                    context.atlasHtml(),
                    "Center node labels inside each shape: x should be near half the node width and text-anchor should be middle.");
        }
    }

    private static boolean hasClass(String lowerHtml, String className) {
        String lowerClass = className.toLowerCase();
        return lowerHtml.contains("class=\"" + lowerClass)
                || lowerHtml.contains("class='" + lowerClass);
    }

    private static boolean visibleNodeShape(String html) {
        for (String tag : CONTRACT.visibleNodeShapeTags()) {
            Pattern pattern = Pattern.compile("(?is)<" + Pattern.quote(tag)
                    + "\\b[^>]*class\\s*=\\s*[\"'][^\"']*\\bnode\\b[^\"']*[\"']");
            if (pattern.matcher(html).find()) {
                return true;
            }
        }
        return false;
    }

    private static boolean forbiddenWrapperClass(String html) {
        for (String wrapper : CONTRACT.forbiddenWrapperClassTags()) {
            String[] parts = wrapper.split("\\.", 2);
            if (parts.length != 2) {
                continue;
            }
            Pattern pattern = Pattern.compile("(?is)<" + Pattern.quote(parts[0])
                    + "\\b[^>]*class\\s*=\\s*[\"'][^\"']*\\b" + Pattern.quote(parts[1])
                    + "\\b[^\"']*[\"']");
            if (pattern.matcher(html).find()) {
                return true;
            }
        }
        return false;
    }
}
