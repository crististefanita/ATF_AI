package com.ai.atf.core.reporting.graph;

import com.ai.core.reporting.graph.BusinessGraphLayout;
import com.ai.core.reporting.graph.BusinessGraphLayoutTokens;
import org.testng.Assert;

/**
 * Focused assertions for the neutral business graph layout contract.
 */
final class BusinessGraphLayoutAssertions {
    private BusinessGraphLayoutAssertions() {
    }

    static void assertPrimitiveShape(BusinessGraphLayout layout) {
        Assert.assertEquals(layout.nodeCount(), 5);
        Assert.assertEquals(layout.edgeCount(), 4);
        Assert.assertEquals(count(layout.svg(), "<rect class=\"" + BusinessGraphLayoutTokens.LANE_CLASS), 3);
        Assert.assertEquals(count(layout.svg(), "<rect class=\"" + BusinessGraphLayoutTokens.NODE_CLASS), 5);
        Assert.assertEquals(count(layout.svg(), "<path class=\"" + BusinessGraphLayoutTokens.EDGE_CLASS), 4);
    }

    static void assertLayoutClasses(BusinessGraphLayout layout) {
        assertHtmlContains(layout, "data-model=\"" + BusinessGraphLayoutTokens.MODEL + "\"");
        assertHtmlContains(layout, BusinessGraphLayoutTokens.GRAPH_PANEL_CLASS);
        assertHtmlContains(layout, BusinessGraphLayoutTokens.OWNED_NODE_CLASS);
        assertHtmlContains(layout, BusinessGraphLayoutTokens.OUTCOME_NODE_CLASS);
        assertHtmlContains(layout, BusinessGraphLayoutTokens.FRONTIER_NODE_CLASS);
        assertHtmlContains(layout, BusinessGraphLayoutTokens.ANCHOR_LANE_CLASS);
        assertHtmlContains(layout, BusinessGraphLayoutTokens.OWNED_LANE_CLASS);
        assertHtmlContains(layout, BusinessGraphLayoutTokens.FRONTIER_LANE_CLASS);
        assertHtmlContains(layout, BusinessGraphLayoutTokens.FRONTIER_EDGE_CLASS);
        assertHtmlContains(layout, BusinessGraphLayoutTokens.LEGEND_CLASS);
        assertHtmlContains(layout, BusinessGraphLayoutTokens.PROOF_BOUNDARY_CLASS);
    }

    static void assertSupplementalRelationships(BusinessGraphLayout layout, String... labels) {
        assertHtmlContains(layout, BusinessGraphLayoutTokens.SUPPLEMENTAL_RELATIONSHIPS_CLASS);
        for (String label : labels) {
            assertHtmlContains(layout, "<li>" + label + "</li>");
        }
    }

    static void assertSupplementalRelationshipCues(BusinessGraphLayout layout, String... labels) {
        assertSvgContains(layout, BusinessGraphLayoutTokens.SUPPLEMENTAL_RELATIONSHIP_TEXT_CLASS);
        assertSvgContains(layout, role(BusinessGraphLayoutTokens.ROLE_SUPPLEMENTAL_RELATIONSHIP));
        for (String label : labels) {
            assertVisibleText(layout, label);
        }
    }

    static void assertAccessibleMetadata(BusinessGraphLayout layout) {
        assertSvgContains(layout, "aria-labelledby=\"" + BusinessGraphLayoutTokens.TITLE_ID
                + " " + BusinessGraphLayoutTokens.DESCRIPTION_ID + "\"");
        assertSvgContains(layout, "<title id=\"" + BusinessGraphLayoutTokens.TITLE_ID + "\">");
        assertSvgContains(layout, "<desc id=\"" + BusinessGraphLayoutTokens.DESCRIPTION_ID + "\">");
    }

    static void assertGraphRoles(BusinessGraphLayout layout) {
        assertSvgContains(layout, role(BusinessGraphLayoutTokens.ROLE_ANCHOR));
        assertSvgContains(layout, role(BusinessGraphLayoutTokens.ROLE_INTENT));
        assertSvgContains(layout, role(BusinessGraphLayoutTokens.ROLE_OWNED));
        assertSvgContains(layout, role(BusinessGraphLayoutTokens.ROLE_OUTCOME));
        assertSvgContains(layout, role(BusinessGraphLayoutTokens.ROLE_FRONTIER));
        assertSvgContains(layout, role(BusinessGraphLayoutTokens.ROLE_RELATIONSHIP));
        assertSvgContains(layout, role(BusinessGraphLayoutTokens.ROLE_FRONTIER_RELATIONSHIP));
    }

    static void assertSinglePrimaryOwnedNode(BusinessGraphLayout layout) {
        Assert.assertEquals(count(layout.svg(), BusinessGraphLayoutTokens.OWNED_NODE_CLASS), 1,
                "Only the primary business object may use the owned node class.");
        assertSvgContains(layout, "<rect class=\"" + BusinessGraphLayoutTokens.OWNED_NODE_CLASS + "\" "
                + role(BusinessGraphLayoutTokens.ROLE_OWNED));
        Assert.assertEquals(count(layout.svg(), BusinessGraphLayoutTokens.OUTCOME_NODE_CLASS), 1,
                "Observable result must stay distinct from the primary owned object.");
        Assert.assertEquals(count(layout.svg(), BusinessGraphLayoutTokens.FRONTIER_NODE_CLASS), 1,
                "Frontier/cap node must stay distinct from owned outcome path.");
    }

    static void assertOutcomeAndFrontierAreNotOwned(BusinessGraphLayout layout) {
        assertSvgNotContains(layout, BusinessGraphLayoutTokens.OUTCOME_NODE_CLASS + "\" "
                + role(BusinessGraphLayoutTokens.ROLE_OWNED));
        assertSvgNotContains(layout, BusinessGraphLayoutTokens.FRONTIER_NODE_CLASS + "\" "
                + role(BusinessGraphLayoutTokens.ROLE_OWNED));
    }

    static void assertFullText(BusinessGraphLayout layout, String value) {
        assertSvgContains(layout, BusinessGraphLayoutTokens.FULL_TEXT_ATTRIBUTE + "=\"" + value + "\"");
    }

    static void assertVisibleText(BusinessGraphLayout layout, String value) {
        assertSvgContains(layout, ">" + value + "</text>");
    }

    static void assertNotVisibleText(BusinessGraphLayout layout, String value) {
        assertSvgNotContains(layout, ">" + value + "</text>");
    }

    static void assertMcpAlignedNodeDensity(BusinessGraphLayout layout) {
        Assert.assertTrue(layout.nodeCount() >= 5,
                "Reporting graph primitive must stay aligned with MCP final-report density gate.");
        int minY = minAttribute(layout.svg(), "y", "<rect class=\"" + BusinessGraphLayoutTokens.NODE_CLASS);
        int maxY = maxAttributeSum(layout.svg(), "y", "height",
                "<rect class=\"" + BusinessGraphLayoutTokens.NODE_CLASS);
        int viewBoxHeight = viewBoxHeight(layout.svg());
        double usedRatio = (double) (maxY - minY) / viewBoxHeight;
        Assert.assertTrue(usedRatio >= 0.42,
                "Business graph node band must use at least 42% of canvas height, actual=" + usedRatio);
    }

    static void assertHtmlContains(BusinessGraphLayout layout, String token) {
        Assert.assertTrue(layout.html().contains(token), "Expected HTML to contain: " + token);
    }

    static void assertHtmlNotContains(BusinessGraphLayout layout, String token) {
        Assert.assertFalse(layout.html().contains(token), "Expected HTML not to contain: " + token);
    }

    static void assertSvgContains(BusinessGraphLayout layout, String token) {
        Assert.assertTrue(layout.svg().contains(token), "Expected SVG to contain: " + token);
    }

    static void assertSvgNotContains(BusinessGraphLayout layout, String token) {
        Assert.assertFalse(layout.svg().contains(token), "Expected SVG not to contain: " + token);
    }

    private static String role(String value) {
        return BusinessGraphLayoutTokens.ROLE_ATTRIBUTE + "=\"" + value + "\"";
    }

    private static int count(String source, String token) {
        int matches = 0;
        int index = 0;
        while ((index = source.indexOf(token, index)) >= 0) {
            matches++;
            index += token.length();
        }
        return matches;
    }

    private static int viewBoxHeight(String svg) {
        String marker = "viewBox=\"0 0 ";
        int start = svg.indexOf(marker);
        Assert.assertTrue(start >= 0, "Expected SVG viewBox.");
        int end = svg.indexOf('"', start + marker.length());
        String[] numbers = svg.substring(start + marker.length(), end).split(" ");
        return Integer.parseInt(numbers[1]);
    }

    private static int minAttribute(String source, String attribute, String elementStart) {
        int min = Integer.MAX_VALUE;
        int index = 0;
        while ((index = source.indexOf(elementStart, index)) >= 0) {
            min = Math.min(min, attribute(source, attribute, index));
            index += elementStart.length();
        }
        return min;
    }

    private static int maxAttributeSum(String source, String first, String second, String elementStart) {
        int max = 0;
        int index = 0;
        while ((index = source.indexOf(elementStart, index)) >= 0) {
            max = Math.max(max, attribute(source, first, index) + attribute(source, second, index));
            index += elementStart.length();
        }
        return max;
    }

    private static int attribute(String source, String attribute, int start) {
        String marker = attribute + "=\"";
        int markerStart = source.indexOf(marker, start);
        Assert.assertTrue(markerStart >= 0, "Expected attribute " + attribute + " after index " + start);
        int valueStart = markerStart + marker.length();
        int valueEnd = source.indexOf('"', valueStart);
        return Integer.parseInt(source.substring(valueStart, valueEnd));
    }
}
