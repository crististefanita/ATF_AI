package com.endava.ai.skills.mcp.validation.profile.graph;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

final class McpGraphSvgModel {
    private final String graphSection;

    McpGraphSvgModel(String graphSection) {
        this.graphSection = graphSection;
    }

    List<NodeBox> nodeBoxes() {
        List<NodeBox> nodes = new ArrayList<>();
        Matcher directRect = Pattern.compile("(?is)<rect\\b[^>]*class=[\"'][^\"']*\\bnode\\b[^\"']*[\"'][^>]*>")
                .matcher(graphSection);
        while (directRect.find()) {
            String rectTag = directRect.group(0);
            NodeBox node = new NodeBox(
                    numberAttribute(rectTag, "x"),
                    numberAttribute(rectTag, "y"),
                    numberAttribute(rectTag, "width"),
                    numberAttribute(rectTag, "height"));
            attachLabelsInsideNode(node, graphSection);
            nodes.add(node);
        }
        if (!nodes.isEmpty()) {
            return nodes;
        }

        Pattern nodePattern = Pattern.compile("(?is)<g\\b[^>]*class=[\"'][^\"']*node[^\"']*[\"'][^>]*>(.*?)</g>");
        Matcher nodeMatcher = nodePattern.matcher(graphSection);
        while (nodeMatcher.find()) {
            String nodeHtml = nodeMatcher.group(1);
            Matcher rect = Pattern.compile("(?is)<rect\\b[^>]*x=[\"']([0-9.]+)[\"'][^>]*y=[\"']([0-9.]+)[\"'][^>]*width=[\"']([0-9.]+)[\"'][^>]*height=[\"']([0-9.]+)[\"'][^>]*>").matcher(nodeHtml);
            if (!rect.find()) {
                continue;
            }
            NodeBox node = new NodeBox(
                    Double.parseDouble(rect.group(1)),
                    Double.parseDouble(rect.group(2)),
                    Double.parseDouble(rect.group(3)),
                    Double.parseDouble(rect.group(4)));
            Matcher text = Pattern.compile("(?is)<text\\b[^>]*class=[\"']([^\"']*)[\"'][^>]*x=[\"']([0-9.]+)[\"'][^>]*y=[\"']([0-9.]+)[\"'][^>]*>(.*?)</text>").matcher(nodeHtml);
            while (text.find()) {
                Matcher anchor = Pattern.compile("(?is)text-anchor=[\"']([^\"']+)[\"']").matcher(text.group(0));
                node.labels().add(new Label(text.group(1),
                        Double.parseDouble(text.group(2)),
                        Double.parseDouble(text.group(3)),
                        anchor.find() ? anchor.group(1).trim() : "",
                        text.group(4).replaceAll("(?is)<[^>]+>", "").trim()));
            }
            nodes.add(node);
        }
        return nodes;
    }

    List<EdgePath> edgePaths() {
        List<EdgePath> edges = new ArrayList<>();
        Matcher matcher = Pattern.compile("(?is)<path\\b[^>]*class=[\"'][^\"']*edge[^\"']*[\"'][^>]*d=[\"']([^\"']+)[\"'][^>]*>").matcher(graphSection);
        while (matcher.find()) {
            EdgePath edge = new EdgePath();
            Matcher pointMatcher = Pattern.compile("([0-9.]+)\\s+([0-9.]+)").matcher(matcher.group(1));
            while (pointMatcher.find()) {
                edge.points().add(new Point(Double.parseDouble(pointMatcher.group(1)),
                        Double.parseDouble(pointMatcher.group(2))));
            }
            edges.add(edge);
        }
        return edges;
    }

    private double numberAttribute(String tag, String attribute) {
        Matcher matcher = Pattern.compile("(?is)\\b" + Pattern.quote(attribute) + "=[\"']([0-9.]+)[\"']")
                .matcher(tag);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : 0;
    }

    private void attachLabelsInsideNode(NodeBox node, String html) {
        Matcher text = Pattern.compile("(?is)<text\\b[^>]*class=[\"']([^\"']*)[\"'][^>]*x=[\"']([0-9.]+)[\"'][^>]*y=[\"']([0-9.]+)[\"'][^>]*>(.*?)</text>")
                .matcher(html);
        while (text.find()) {
            double x = Double.parseDouble(text.group(2));
            double y = Double.parseDouble(text.group(3));
            if (x < node.x() || x > node.x() + node.width() || y < node.y() || y > node.y() + node.height()) {
                continue;
            }
            Matcher anchor = Pattern.compile("(?is)text-anchor=[\"']([^\"']+)[\"']").matcher(text.group(0));
            node.labels().add(new Label(text.group(1),
                    x,
                    y,
                    anchor.find() ? anchor.group(1).trim() : "",
                    text.group(4).replaceAll("(?is)<[^>]+>", "").trim()));
        }
    }

    static double estimatedTextWidth(String text, String cssClass) {
        double average = cssClass.contains("nt") ? 7.6 : 6.5;
        return Math.max(22, text.length() * average);
    }

    static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    static final class NodeBox {
        private final double x;
        private final double y;
        private final double width;
        private final double height;
        private final List<Label> labels = new ArrayList<>();

        boolean contains(Point point) {
            return point.x() >= x && point.x() <= x + width && point.y() >= y && point.y() <= y + height;
        }

        boolean onBorder(Point point) {
            return Math.abs(point.x() - x) <= 3
                    || Math.abs(point.x() - (x + width)) <= 3
                    || Math.abs(point.y() - y) <= 3
                    || Math.abs(point.y() - (y + height)) <= 3;
        }
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    static final class Label {
        private final String cssClass;
        private final double x;
        private final double y;
        private final String textAnchor;
        private final String text;
    }

    @Getter
    @Accessors(fluent = true)
    static final class EdgePath {
        private final List<Point> points = new ArrayList<>();
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    static final class Point {
        private final double x;
        private final double y;
    }
}
