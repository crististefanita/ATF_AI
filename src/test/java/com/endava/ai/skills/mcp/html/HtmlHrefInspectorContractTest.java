package com.endava.ai.skills.mcp.html;

import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class HtmlHrefInspectorContractTest {
    private static final String RELATIVE_MCP_EXTENT_REPORT = "../" + McpArtifactPaths.MCP_EXTENT_REPORT;

    @Test
    public void inspectorNormalizesExactAndArtifactHrefs() {
        HtmlHrefInspector inspector = HtmlHrefInspector.in(String.join(System.lineSeparator(),
                "<a href=\"" + RELATIVE_MCP_EXTENT_REPORT + "\">mcp</a>",
                "<a href=\"" + McpArtifactPaths.PACKAGE_STATE.replace('/', '\\') + "\">state</a>",
                "<a href=\"langgraph-business-understanding.html\">report</a>"
        ));

        Assert.assertTrue(inspector.hasHrefTo(RELATIVE_MCP_EXTENT_REPORT));
        Assert.assertTrue(inspector.hasArtifactHref(McpArtifactPaths.MCP_EXTENT_REPORT));
        Assert.assertTrue(inspector.hasArtifactHref(McpArtifactPaths.PACKAGE_STATE));
        Assert.assertTrue(inspector.hasArtifactHref(McpArtifactPaths.FINAL_REPORT));
        Assert.assertTrue(inspector.hasAnyArtifactHref(List.of("missing.html", McpArtifactPaths.PACKAGE_STATE)));
    }

    @Test
    public void inspectorTreatsNullOrEmptyExpectedListsAsMissing() {
        HtmlHrefInspector inspector = HtmlHrefInspector.in("<a href=\"actual.html\">actual</a>");

        Assert.assertFalse(inspector.hasAnyHrefTo(null));
        Assert.assertFalse(inspector.hasAnyHrefTo(List.of()));
        Assert.assertFalse(inspector.hasAnyArtifactHref(null));
        Assert.assertFalse(inspector.hasAnyArtifactHref(List.of()));
    }
}
