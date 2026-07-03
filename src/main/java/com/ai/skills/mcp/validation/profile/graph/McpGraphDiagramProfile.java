package com.ai.skills.mcp.validation.profile.graph;

import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class McpGraphDiagramProfile {
    private static final double MINIMUM_NODE_CANVAS_COVERAGE = 0.42;

    private final String graphSection;

    public List<String> blockers(McpPackageAuditValidation audit) {
        List<String> blockers = new ArrayList<>();
        audit.requireAnyToken(graphSection, List.of("<svg", "class=\"diag\"", "class='diag'"),
                "Business Flow Graph must contain a real diagram/SVG container", blockers);
        audit.requireAnyToken(graphSection, List.of("role=\"img\"", "aria-label"),
                "Business Flow Graph SVG must expose an accessible diagram label", blockers);
        audit.requireAnyToken(graphSection, List.of("<rect", "<path", "<circle"),
                "Business Flow Graph diagram must contain visual nodes/edges", blockers);
        if (audit.stripTags(graphSection).contains("->") && !graphSection.contains("<svg")) {
            blockers.add("Business Flow Graph is still a text arrow chain instead of a rendered graph");
        }
        return blockers;
    }

    public List<String> densityBlockers() {
        List<String> blockers = new ArrayList<>();
        Matcher viewBox = Pattern.compile("(?is)<svg\\b[^>]*viewBox=[\"'][^\"']*?([0-9.]+)\\s+([0-9.]+)[\"']")
                .matcher(graphSection);
        if (!viewBox.find()) {
            blockers.add("Business Flow Graph SVG must declare viewBox width/height so canvas density can be audited");
            return blockers;
        }

        double canvasHeight = Double.parseDouble(viewBox.group(2));
        Bounds bounds = nodeBounds();
        if (bounds.count() < 5) {
            blockers.add("Business Flow Graph must show at least five business nodes so the diagram is not a thin decorative chain");
        }
        if (canvasHeight <= 0 || bounds.count() == 0) {
            blockers.add("Business Flow Graph has no measurable node geometry for visual density checks");
            return blockers;
        }

        double usedHeightRatio = bounds.height() / canvasHeight;
        if (usedHeightRatio < MINIMUM_NODE_CANVAS_COVERAGE) {
            blockers.add("Business Flow Graph canvas is too sparse: node band uses "
                    + roundedPercent(usedHeightRatio)
                    + " of viewBox height; reduce empty graph-panel space or add meaningful layered business context");
        }
        return blockers;
    }

    public List<String> semanticNodeBlockers() {
        List<String> blockers = new ArrayList<>();
        if (!Pattern.compile("(?is)<(rect|circle|polygon)\\b[^>]*class=[\"'][^\"']*\\bowned\\b[^\"']*[\"']")
                .matcher(graphSection).find()) {
            blockers.add("Business Flow Graph must visually mark owned/countable business nodes with the CSS-backed owned class");
        }
        if (!Pattern.compile("(?is)<(rect|circle|polygon)\\b[^>]*class=[\"'][^\"']*\\b(cap|blocked)\\b[^\"']*[\"']")
                .matcher(graphSection).find()) {
            blockers.add("Business Flow Graph must visually mark the not-owned frontier with CSS-backed cap or blocked class");
        }
        if (Pattern.compile("(?is)<(rect|circle|polygon)\\b[^>]*class=[\"'][^\"']*\\bcapped\\b[^\"']*[\"']")
                .matcher(graphSection).find()) {
            blockers.add("Business Flow Graph uses class 'capped', but shared CSS styles 'cap'/'blocked'; use CSS-backed semantic classes so frontier nodes are visible");
        }
        return blockers;
    }

    private Bounds nodeBounds() {
        Bounds bounds = new Bounds();
        Matcher rect = Pattern.compile("(?is)<rect\\b[^>]*class=[\"'][^\"']*\\bnode\\b[^\"']*[\"'][^>]*>")
                .matcher(graphSection);
        while (rect.find()) {
            String tag = rect.group(0);
            double x = numberAttribute(tag, "x");
            double y = numberAttribute(tag, "y");
            double width = numberAttribute(tag, "width");
            double height = numberAttribute(tag, "height");
            if (width > 0 && height > 0) {
                bounds.include(x, y, x + width, y + height);
            }
        }
        return bounds;
    }

    private static double numberAttribute(String tag, String attribute) {
        Matcher matcher = Pattern.compile("(?is)\\b" + Pattern.quote(attribute) + "=[\"']([0-9.]+)[\"']")
                .matcher(tag);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : 0;
    }

    private static int roundedPercent(double ratio) {
        return (int) Math.round(ratio * 100);
    }

    private static final class Bounds {
        private double minY = Double.MAX_VALUE;
        private double maxY = Double.MIN_VALUE;
        private int count;

        void include(double minX, double minY, double maxX, double maxY) {
            if (maxX <= minX || maxY <= minY) {
                return;
            }
            this.minY = Math.min(this.minY, minY);
            this.maxY = Math.max(this.maxY, maxY);
            count++;
        }

        double height() {
            return count == 0 ? 0 : maxY - minY;
        }

        int count() {
            return count;
        }
    }
}
