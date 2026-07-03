package com.endava.ai.skills.mcp.html;

import com.endava.ai.skills.mcp.context.McpArtifactPaths;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Normalized href lookup for generated MCP HTML reports.
 */
public final class HtmlHrefInspector {
    private final List<String> hrefs;

    private HtmlHrefInspector(String html) {
        this.hrefs = new HtmlLinkExtractor().hrefs(html).stream()
                .map(HtmlHrefInspector::normalize)
                .collect(Collectors.toUnmodifiableList());
    }

    public static HtmlHrefInspector in(String html) {
        return new HtmlHrefInspector(html);
    }

    public boolean hasHrefTo(String expectedHref) {
        return hrefs.contains(normalize(expectedHref));
    }

    public boolean hasAnyHrefTo(List<String> expectedHrefs) {
        if (expectedHrefs == null || expectedHrefs.isEmpty()) {
            return false;
        }
        return expectedHrefs.stream().anyMatch(this::hasHrefTo);
    }

    public boolean hasArtifactHref(String artifact) {
        if (hasHrefTo("../" + artifact) || hasHrefTo(artifact)) {
            return true;
        }
        if (artifact.startsWith(McpArtifactPaths.ANALYSIS_ROOT + "/")) {
            return hasHrefTo(artifact.substring((McpArtifactPaths.ANALYSIS_ROOT + "/").length()));
        }
        return false;
    }

    public boolean hasAnyArtifactHref(List<String> artifacts) {
        if (artifacts == null || artifacts.isEmpty()) {
            return false;
        }
        return artifacts.stream().anyMatch(this::hasArtifactHref);
    }

    private static String normalize(String href) {
        return href == null ? "" : href.trim().replace('\\', '/');
    }
}
