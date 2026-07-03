package com.endava.ai.core.reporting.graph;

import com.endava.ai.skills.business.model.BusinessGraphConstruction;

import java.util.Objects;

/**
 * Neutral Reporting adapter from shared business semantics to reusable graph layout.
 */
public final class BusinessGraphLayoutAdapter {
    private static final int NODE_TEXT_LIMIT = 36;
    private static final int EDGE_TEXT_LIMIT = 28;
    private static final int MAX_SUPPLEMENTAL_SVG_CUES = 3;
    private static final int SUPPLEMENTAL_CUE_X = 520;
    private static final int SUPPLEMENTAL_CUE_Y = 262;
    private static final int SUPPLEMENTAL_CUE_ROW_GAP = 22;

    public BusinessGraphLayout render(BusinessGraphConstruction graph) {
        Objects.requireNonNull(graph, "graph");
        BusinessGraphLayoutModel model = BusinessGraphLayoutModel.from(graph);
        String svg = svg(model);
        return new BusinessGraphLayout(html(graph, model, svg), svg, model.nodes().size(), model.edges().size());
    }

    private static String html(BusinessGraphConstruction graph, BusinessGraphLayoutModel model, String svg) {
        return "<details open class=\"" + BusinessGraphLayoutTokens.DETAILS_CLASS + "\">"
                + "<summary><span class=\"summary-title\">Business Flow Graph</span>"
                + "<span class=\"sumhint\">business-owned layout</span></summary>"
                + "<div class=\"" + BusinessGraphLayoutTokens.GRAPH_PANEL_CLASS + "\" data-model=\""
                + BusinessGraphLayoutTokens.MODEL + "\">"
                + svg
                + "</div>"
                + legend()
                + supplementalRelationships(model)
                + proofBoundary(graph.proofBoundary())
                + "</details>";
    }

    private static String svg(BusinessGraphLayoutModel model) {
        StringBuilder svg = new StringBuilder("<svg class=\"").append(BusinessGraphLayoutTokens.SVG_CLASS)
                .append("\" viewBox=\"0 0 1040 360\" role=\"img\" aria-labelledby=\"")
                .append(BusinessGraphLayoutTokens.TITLE_ID).append(" ")
                .append(BusinessGraphLayoutTokens.DESCRIPTION_ID).append("\">")
                .append("<title id=\"").append(BusinessGraphLayoutTokens.TITLE_ID)
                .append("\">Business graph layout</title>")
                .append("<desc id=\"").append(BusinessGraphLayoutTokens.DESCRIPTION_ID)
                .append("\">Business-owned graph layout generated from BusinessGraphConstruction.</desc>")
                .append("<defs><marker id=\"business-arrow\" markerWidth=\"10\" markerHeight=\"10\" refX=\"8\"")
                .append(" refY=\"3\" orient=\"auto\"><path d=\"M0,0 L0,6 L9,3 z\"></path></marker></defs>");
        model.lanes().forEach(lane -> svg.append(lane(lane)));
        model.nodes().forEach(node -> svg.append(node(node)));
        model.edges().forEach(edge -> svg.append(edge(edge)));
        svg.append(supplementalRelationshipCues(model));
        return svg.append("</svg>").toString();
    }

    private static String lane(BusinessGraphLayoutModel.Lane lane) {
        return "<rect class=\"" + BusinessGraphLayoutTokens.LANE_CLASS + " " + lane.cssClass()
                + "\" " + BusinessGraphLayoutTokens.ROLE_ATTRIBUTE + "=\"" + lane.role()
                + "\" x=\"" + lane.x() + "\" y=\"" + lane.y() + "\" width=\"" + lane.width()
                + "\" height=\"" + lane.height()
                + "\" rx=\"18\"></rect>";
    }

    private static String node(BusinessGraphLayoutModel.Node node) {
        return "<rect class=\"" + node.cssClass() + "\" " + BusinessGraphLayoutTokens.ROLE_ATTRIBUTE + "=\""
                + node.role()
                + "\" x=\"" + node.x() + "\" y=\"" + node.y()
                + "\" width=\"175\" height=\"72\" rx=\"14\"></rect>"
                + nodeText(BusinessGraphLayoutTokens.TITLE_TEXT_CLASS, node.x() + 87, node.y() + 29, node.title())
                + nodeText(BusinessGraphLayoutTokens.SUBTITLE_TEXT_CLASS, node.x() + 87, node.y() + 52,
                        node.subtitle());
    }

    private static String edge(BusinessGraphLayoutModel.Edge edge) {
        int labelX = (edge.x1() + edge.x2()) / 2;
        int labelY = Math.min(edge.y1(), edge.y2()) - 12;
        return "<path class=\"" + edge.cssClass() + "\" " + BusinessGraphLayoutTokens.ROLE_ATTRIBUTE + "=\""
                + edge.role()
                + "\" d=\"M" + edge.x1() + " " + edge.y1()
                + " L" + edge.x2() + " " + edge.y2() + "\" marker-end=\"url(#business-arrow)\"></path>"
                + edgeText(labelX, labelY, edge.label());
    }

    private static String nodeText(String cssClass, int x, int y, String value) {
        return text(cssClass, x, y, value, NODE_TEXT_LIMIT);
    }

    private static String edgeText(int x, int y, String value) {
        return text(BusinessGraphLayoutTokens.EDGE_LABEL_CLASS, x, y, value, EDGE_TEXT_LIMIT);
    }

    private static String supplementalRelationshipCues(BusinessGraphLayoutModel model) {
        if (model.supplementalRelationshipLabels().isEmpty()) {
            return "";
        }
        StringBuilder cues = new StringBuilder();
        int count = Math.min(model.supplementalRelationshipLabels().size(), MAX_SUPPLEMENTAL_SVG_CUES);
        for (int index = 0; index < count; index++) {
            cues.append(roleText(BusinessGraphLayoutTokens.SUPPLEMENTAL_RELATIONSHIP_TEXT_CLASS,
                    BusinessGraphLayoutTokens.ROLE_SUPPLEMENTAL_RELATIONSHIP,
                    SUPPLEMENTAL_CUE_X,
                    SUPPLEMENTAL_CUE_Y + index * SUPPLEMENTAL_CUE_ROW_GAP,
                    model.supplementalRelationshipLabels().get(index),
                    EDGE_TEXT_LIMIT));
        }
        return cues.toString();
    }

    private static String text(String cssClass, int x, int y, String value, int limit) {
        return "<text class=\"" + cssClass + "\" x=\"" + x + "\" y=\"" + y
                + "\" text-anchor=\"middle\" " + BusinessGraphLayoutTokens.FULL_TEXT_ATTRIBUTE + "=\""
                + escape(value) + "\">" + escape(visibleText(value, limit)) + "</text>";
    }

    private static String roleText(String cssClass, String role, int x, int y, String value, int limit) {
        return "<text class=\"" + cssClass + "\" " + BusinessGraphLayoutTokens.ROLE_ATTRIBUTE + "=\""
                + role + "\" x=\"" + x + "\" y=\"" + y
                + "\" text-anchor=\"middle\" " + BusinessGraphLayoutTokens.FULL_TEXT_ATTRIBUTE + "=\""
                + escape(value) + "\">" + escape(visibleText(value, limit)) + "</text>";
    }

    private static String legend() {
        return "<details class=\"" + BusinessGraphLayoutTokens.LEGEND_CLASS + "\">"
                + "<summary><span class=\"summary-title\">Legend</span></summary>"
                + "<ul>"
                + "<li>Solid owned node = business object directly claimed by the report.</li>"
                + "<li>Frontier node = downstream or capped object that remains explicitly limited.</li>"
                + "<li>Relationship labels describe business meaning, not execution order.</li>"
                + "</ul>"
                + "</details>";
    }

    private static String supplementalRelationships(BusinessGraphLayoutModel model) {
        if (model.supplementalRelationshipLabels().isEmpty()) {
            return "";
        }
        StringBuilder html = new StringBuilder("<details class=\"")
                .append(BusinessGraphLayoutTokens.SUPPLEMENTAL_RELATIONSHIPS_CLASS)
                .append("\"><summary><span class=\"summary-title\">Supplemental Relationships</span></summary><ul>");
        model.supplementalRelationshipLabels().forEach(label ->
                html.append("<li>").append(escape(label)).append("</li>"));
        return html.append("</ul></details>").toString();
    }

    private static String proofBoundary(String proofBoundary) {
        return "<details class=\"" + BusinessGraphLayoutTokens.PROOF_BOUNDARY_CLASS + "\">"
                + "<summary><span class=\"summary-title\">Proof Boundary</span></summary>"
                + "<p>" + escape(proofBoundary) + "</p>"
                + "</details>";
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String visibleText(String value, int limit) {
        if (value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit - 3) + "...";
    }
}
