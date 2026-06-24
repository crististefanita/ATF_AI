package com.endava.ai.mcp.validation.profile.graph;

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
}
