package com.ai.skills.mcp.validation.report.link;

import com.ai.skills.mcp.context.McpArtifactPaths;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class McpReportLinkProbeContractTest {
    private static final String RELATIVE_MCP_EXTENT_REPORT = "../" + McpArtifactPaths.MCP_EXTENT_REPORT;

    @Test
    public void probeMatchesExactHrefAndNormalizedArtifactHref() {
        McpReportLinkProbe probe = McpReportLinkProbe.in(
                "<a href=\"" + RELATIVE_MCP_EXTENT_REPORT + "\">MCP Extent</a>\n"
                        + "<a href=\"" + McpArtifactPaths.PACKAGE_STATE.replace('/', '\\') + "\">state</a>\n");

        Assert.assertTrue(probe.hasHrefTo(RELATIVE_MCP_EXTENT_REPORT));
        Assert.assertTrue(probe.hasArtifactHref(McpArtifactPaths.MCP_EXTENT_REPORT));
        Assert.assertTrue(probe.hasArtifactHref(McpArtifactPaths.PACKAGE_STATE));
    }

    @Test
    public void requireMethodsAppendActionableBlockers() {
        McpReportLinkProbe probe = McpReportLinkProbe.in("<a href=\"actual.html\">actual</a>");
        List<String> blockers = new ArrayList<>();

        probe.requireHrefTo("missing.html", "missing exact", blockers);
        probe.requireAnyHrefTo(List.of("a.html", "b.html"), "missing any", blockers);
        probe.requireArtifactHref(McpArtifactPaths.MCP_EXTENT_REPORT, "missing artifact", blockers);
        probe.requireAnyArtifactHref(List.of("one.html", "two.html"), "missing artifact group", blockers);

        Assert.assertEquals(blockers, List.of(
                "missing exact: missing.html",
                "missing any: a.html or b.html",
                "missing artifact: " + McpArtifactPaths.MCP_EXTENT_REPORT,
                "missing artifact group: one.html or two.html"));
    }
}
