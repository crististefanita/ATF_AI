package com.endava.ai.core.reporting.graph;

import com.endava.ai.skills.business.model.BusinessGraphConstruction;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Internal layout model for the reusable business graph primitive.
 */
@Value
@Accessors(fluent = true)
final class BusinessGraphLayoutModel {
    List<Lane> lanes;
    List<Node> nodes;
    List<Edge> edges;
    List<String> supplementalRelationshipLabels;

    static BusinessGraphLayoutModel from(BusinessGraphConstruction graph) {
        List<String> labels = graph.relationshipLabels();
        return new BusinessGraphLayoutModel(
                List.of(
                        new Lane(BusinessGraphLayoutTokens.ANCHOR_LANE_CLASS,
                                BusinessGraphLayoutTokens.ROLE_ANCHOR, 35, 42, 400, 110),
                        new Lane(BusinessGraphLayoutTokens.OWNED_LANE_CLASS,
                                BusinessGraphLayoutTokens.ROLE_OWNED, 430, 164, 390, 110),
                        new Lane(BusinessGraphLayoutTokens.FRONTIER_LANE_CLASS,
                                BusinessGraphLayoutTokens.ROLE_FRONTIER, 810, 228, 205, 110)),
                List.of(
                        new Node(BusinessGraphLayoutTokens.ANCHOR_NODE_CLASS,
                                BusinessGraphLayoutTokens.ROLE_ANCHOR, 55, 70,
                                graph.businessAnchor().name(), graph.businessAnchor().observableResult()),
                        new Node(BusinessGraphLayoutTokens.INTENT_NODE_CLASS,
                                BusinessGraphLayoutTokens.ROLE_INTENT, 245, 70,
                                "Actor intent", graph.actorIntent()),
                        new Node(BusinessGraphLayoutTokens.OWNED_NODE_CLASS,
                                BusinessGraphLayoutTokens.ROLE_OWNED, 440, 185,
                                graph.primaryOwnedObject().name(), graph.primaryOwnedObject().observableResult()),
                        new Node(BusinessGraphLayoutTokens.OUTCOME_NODE_CLASS,
                                BusinessGraphLayoutTokens.ROLE_OUTCOME, 635, 185,
                                "Observable result", graph.primaryOwnedObject().observableResult()),
                        new Node(BusinessGraphLayoutTokens.FRONTIER_NODE_CLASS,
                                BusinessGraphLayoutTokens.ROLE_FRONTIER, 825, 250,
                                graph.frontierObject().name(), graph.frontierObject().observableResult())),
                List.of(
                        new Edge(BusinessGraphLayoutTokens.EDGE_CLASS,
                                BusinessGraphLayoutTokens.ROLE_RELATIONSHIP, 230, 106, 245, 106,
                                label(labels, 0)),
                        new Edge(BusinessGraphLayoutTokens.EDGE_CLASS,
                                BusinessGraphLayoutTokens.ROLE_RELATIONSHIP, 420, 106, 440, 221,
                                label(labels, 1)),
                        new Edge(BusinessGraphLayoutTokens.EDGE_CLASS,
                                BusinessGraphLayoutTokens.ROLE_RELATIONSHIP, 615, 221, 635, 221,
                                label(labels, 2)),
                        new Edge(BusinessGraphLayoutTokens.FRONTIER_EDGE_CLASS,
                                BusinessGraphLayoutTokens.ROLE_FRONTIER_RELATIONSHIP, 810, 221, 825, 286,
                                label(labels, 3))),
                supplementalLabels(labels));
    }

    private static List<String> supplementalLabels(List<String> labels) {
        if (labels.size() <= 4) {
            return List.of();
        }
        return List.copyOf(labels.subList(4, labels.size()));
    }

    private static String label(List<String> labels, int index) {
        if (index < labels.size()) {
            return labels.get(index);
        }
        switch (index) {
            case 0:
                return "anchors";
            case 1:
                return "owns";
            case 2:
                return "proves";
            default:
                return "limits";
        }
    }

    @Value
    @Accessors(fluent = true)
    static class Lane {
        String cssClass;
        String role;
        int x;
        int y;
        int width;
        int height;
    }

    @Value
    @Accessors(fluent = true)
    static class Node {
        String cssClass;
        String role;
        int x;
        int y;
        String title;
        String subtitle;
    }

    @Value
    @Accessors(fluent = true)
    static class Edge {
        String cssClass;
        String role;
        int x1;
        int y1;
        int x2;
        int y2;
        String label;
    }
}
