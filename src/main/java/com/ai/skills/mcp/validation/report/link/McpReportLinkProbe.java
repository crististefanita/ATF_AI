package com.ai.skills.mcp.validation.report.link;

import com.ai.skills.mcp.html.HtmlHrefInspector;

import java.util.List;

public final class McpReportLinkProbe {
    private final HtmlHrefInspector hrefs;

    private McpReportLinkProbe(String html) {
        this.hrefs = HtmlHrefInspector.in(html);
    }

    public static McpReportLinkProbe in(String html) {
        return new McpReportLinkProbe(html);
    }

    public boolean hasHrefTo(String expectedHref) {
        return hrefs.hasHrefTo(expectedHref);
    }

    public boolean hasAnyHrefTo(List<String> expectedHrefs) {
        return hrefs.hasAnyHrefTo(expectedHrefs);
    }

    public boolean hasArtifactHref(String artifact) {
        return hrefs.hasArtifactHref(artifact);
    }

    public boolean hasAnyArtifactHref(List<String> artifacts) {
        return hrefs.hasAnyArtifactHref(artifacts);
    }

    public void requireHrefTo(String expectedHref, String message, List<String> blockers) {
        if (!hasHrefTo(expectedHref)) {
            blockers.add(message + ": " + expectedHref);
        }
    }

    public void requireAnyHrefTo(List<String> expectedHrefs, String message, List<String> blockers) {
        if (!hasAnyHrefTo(expectedHrefs)) {
            blockers.add(message + ": " + String.join(" or ", expectedHrefs));
        }
    }

    public void requireArtifactHref(String artifact, String message, List<String> blockers) {
        if (!hasArtifactHref(artifact)) {
            blockers.add(message + ": " + artifact);
        }
    }

    public void requireAnyArtifactHref(List<String> artifacts, String message, List<String> blockers) {
        if (!hasAnyArtifactHref(artifacts)) {
            blockers.add(message + ": " + String.join(" or ", artifacts));
        }
    }
}
