package com.endava.ai.skills.mcp.validation.profile.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class McpGraphEdgeProfile {
    private final String graphSection;
    private final McpGraphCss graphCss;

    public McpGraphEdgeProfile(String graphSection, String css) {
        this.graphSection = graphSection;
        this.graphCss = new McpGraphCss(css);
    }

    public List<String> variantBlockers() {
        List<String> blockers = new ArrayList<>();
        Matcher edge = Pattern.compile("(?is)<path\\b[^>]*class=[\"']([^\"']*\\bedge\\b[^\"']*)[\"']")
                .matcher(graphSection);
        while (edge.find()) {
            for (String cssClass : edge.group(1).trim().split("\\s+")) {
                if ("edge".equals(cssClass)) {
                    continue;
                }
                String normalized = cssClass.toLowerCase(Locale.ROOT);
                String edgeSelector = ".diag.edge." + normalized;
                String diagramSelector = ".diag." + normalized;
                if (!graphCss.containsSelector(edgeSelector) && !graphCss.containsSelector(diagramSelector)) {
                    blockers.add("Business Flow Graph uses edge variant '" + cssClass
                            + "' but shared CSS does not define a matching .diag .edge." + cssClass + " style");
                }
            }
        }
        return blockers;
    }

    public List<String> frontierCueBlockers() {
        List<String> blockers = new ArrayList<>();
        boolean hasFrontierNode = Pattern.compile("(?is)<(rect|circle|polygon)\\b[^>]*class=[\"'][^\"']*\\b(cap|blocked|capped)\\b[^\"']*[\"']")
                .matcher(graphSection).find()
                || graphSection.toLowerCase(Locale.ROOT).contains("next object")
                || graphSection.toLowerCase(Locale.ROOT).contains("frontier");
        if (!hasFrontierNode) {
            return blockers;
        }

        boolean styledFrontierEdge = Pattern.compile("(?is)<path\\b[^>]*class=[\"'][^\"']*\\bedge\\b[^\"']*\\b(dashed|soft|frontier|cap)\\b[^\"']*[\"']")
                .matcher(graphSection).find()
                || Pattern.compile("(?is)<path\\b[^>]*stroke-dasharray=").matcher(graphSection).find();
        if (!styledFrontierEdge) {
            blockers.add("Business Flow Graph frontier/next-object edge must use a CSS-backed visual cue such as edge dashed/soft/frontier/cap or stroke-dasharray");
        }

        Matcher inlineEdge = Pattern.compile("(?is)<path\\b(?=[^>]*class=[\"'][^\"']*\\bedge\\b[^\"']*[\"'])(?=[^>]*\\bstroke=[\"']).*?>")
                .matcher(graphSection);
        while (inlineEdge.find()) {
            String tag = inlineEdge.group(0);
            boolean hasVariant = Pattern.compile("(?is)class=[\"'][^\"']*\\b(dashed|soft|frontier|cap)\\b[^\"']*[\"']")
                    .matcher(tag).find();
            if (!hasVariant) {
                blockers.add("Business Flow Graph edge uses inline stroke without a semantic edge variant; keep visual meaning in shared CSS classes");
                break;
            }
        }
        return blockers;
    }
}
