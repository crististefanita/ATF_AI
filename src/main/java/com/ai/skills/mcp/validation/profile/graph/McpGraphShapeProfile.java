package com.ai.skills.mcp.validation.profile.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class McpGraphShapeProfile {
    private final String graphSection;
    private final McpGraphCss graphCss;
    private final McpGraphSvgModel svgModel;

    public McpGraphShapeProfile(String graphSection, String css) {
        this.graphSection = graphSection;
        this.graphCss = new McpGraphCss(css);
        this.svgModel = new McpGraphSvgModel(graphSection);
    }

    public List<String> labelAlignmentBlockers() {
        List<String> blockers = new ArrayList<>();
        boolean cssTitleAnchorsMiddle = graphCss.classHasPropertyValue(".nt", "text-anchor", "middle");
        boolean cssSubtitleAnchorsMiddle = graphCss.classHasPropertyValue(".ns", "text-anchor", "middle");
        if (!cssTitleAnchorsMiddle || !cssSubtitleAnchorsMiddle) {
            blockers.add("Business Flow Graph .nt and .ns labels must use text-anchor: middle; SVG text x/y values are center points, not left edges");
        }

        for (McpGraphSvgModel.NodeBox node : svgModel.nodeBoxes()) {
            for (McpGraphSvgModel.Label label : node.labels()) {
                boolean middleAnchored = "middle".equalsIgnoreCase(label.textAnchor())
                        || (label.cssClass().contains("nt") && cssTitleAnchorsMiddle)
                        || (label.cssClass().contains("ns") && cssSubtitleAnchorsMiddle);
                double estimatedWidth = McpGraphSvgModel.estimatedTextWidth(label.text(), label.cssClass());
                double labelLeft = middleAnchored ? label.x() - estimatedWidth / 2 : label.x();
                double labelRight = middleAnchored ? label.x() + estimatedWidth / 2 : label.x() + estimatedWidth;
                double safeLeft = node.x() + 12;
                double safeRight = node.x() + node.width() - 12;
                if (labelLeft < safeLeft || labelRight > safeRight) {
                    blockers.add("Business Flow Graph label does not fit inside node: '" + label.text()
                            + "' spans approximately x=" + McpGraphSvgModel.round(labelLeft) + ".." + McpGraphSvgModel.round(labelRight)
                            + " while node safe range is x=" + McpGraphSvgModel.round(safeLeft) + ".." + McpGraphSvgModel.round(safeRight)
                            + ". Center it with text-anchor: middle or widen/reposition the node.");
                }
            }
        }
        return blockers;
    }

    public List<String> textClearanceBlockers() {
        List<String> blockers = new ArrayList<>();
        for (McpGraphSvgModel.NodeBox node : svgModel.nodeBoxes()) {
            for (McpGraphSvgModel.Label label : node.labels()) {
                double labelHalfWidth = McpGraphSvgModel.estimatedTextWidth(label.text(), label.cssClass()) / 2;
                double labelLeft = label.x() - labelHalfWidth;
                double labelRight = label.x() + labelHalfWidth;
                for (McpGraphSvgModel.EdgePath edge : svgModel.edgePaths()) {
                    for (McpGraphSvgModel.Point point : edge.points()) {
                        if (node.contains(point)
                                && !node.onBorder(point)
                                && Math.abs(point.y() - label.y()) <= 8
                                && point.x() >= labelLeft - 8
                                && point.x() <= labelRight + 8) {
                            blockers.add("Business Flow Graph edge crosses node text: label '" + label.text()
                                    + "' in node at x=" + node.x() + ", y=" + node.y()
                                    + ". Move the edge to the node border or route it below/above the label band.");
                        }
                    }
                }
            }
        }
        return blockers;
    }

    public List<String> visibleNodeShapeBlockers() {
        List<String> blockers = new ArrayList<>();
        if (!Pattern.compile("(?is)<(rect|circle|path)\\b[^>]*class=[\"'][^\"']*\\bnode\\b[^\"']*[\"']")
                .matcher(graphSection).find()) {
            blockers.add("Business Flow Graph node classes must be attached to visible SVG shapes, not only to wrapper <g> elements");
        }

        Matcher wrapperNode = Pattern.compile("(?is)<g\\b[^>]*class=[\"'][^\"']*\\bnode\\b[^\"']*[\"'][^>]*>(.*?)</g>")
                .matcher(graphSection);
        while (wrapperNode.find()) {
            String nodeBody = wrapperNode.group(1);
            if (Pattern.compile("(?is)<rect\\b(?![^>]*class=)[^>]*>").matcher(nodeBody).find()) {
                blockers.add("Business Flow Graph wraps an unstyled <rect> in <g class=\"node\">; put node/owned/cap classes on the <rect> itself");
                break;
            }
        }
        return blockers;
    }
}
