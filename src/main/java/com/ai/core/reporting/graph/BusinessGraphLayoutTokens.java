package com.ai.core.reporting.graph;

/**
 * Shared CSS/data tokens for the neutral business-graph layout produced by Reporting.
 */
public final class BusinessGraphLayoutTokens {
    public static final String MODEL = "BusinessGraphConstruction";
    public static final String DETAILS_CLASS = "business-graph-layout";
    public static final String GRAPH_PANEL_CLASS = "graph-panel business-graph-panel";
    public static final String SVG_CLASS = "diag business-graph-diagram";
    public static final String LANE_CLASS = "business-graph-lane";
    public static final String ANCHOR_LANE_CLASS = "business-graph-lane-anchor";
    public static final String OWNED_LANE_CLASS = "business-graph-lane-owned";
    public static final String FRONTIER_LANE_CLASS = "business-graph-lane-frontier";
    public static final String NODE_CLASS = "node";
    public static final String ANCHOR_NODE_CLASS = "node business-anchor-node";
    public static final String INTENT_NODE_CLASS = "node business-intent-node";
    public static final String OWNED_NODE_CLASS = "node business-owned-node";
    public static final String OUTCOME_NODE_CLASS = "node business-outcome-node";
    public static final String FRONTIER_NODE_CLASS = "node cap business-frontier-node";
    public static final String TITLE_TEXT_CLASS = "nt";
    public static final String SUBTITLE_TEXT_CLASS = "ns";
    public static final String EDGE_CLASS = "edge";
    public static final String FRONTIER_EDGE_CLASS = "edge edge-dashed business-frontier-edge";
    public static final String EDGE_LABEL_CLASS = "edge-label";
    public static final String SUPPLEMENTAL_RELATIONSHIP_TEXT_CLASS = "edge-label business-supplemental-relationship";
    public static final String LEGEND_CLASS = "subdetails business-graph-legend";
    public static final String PROOF_BOUNDARY_CLASS = "subdetails business-graph-proof-boundary";
    public static final String SUPPLEMENTAL_RELATIONSHIPS_CLASS =
            "subdetails business-graph-supplemental-relationships";
    public static final String FULL_TEXT_ATTRIBUTE = "data-full-text";
    public static final String ROLE_ATTRIBUTE = "data-graph-role";
    public static final String ROLE_ANCHOR = "anchor";
    public static final String ROLE_INTENT = "intent";
    public static final String ROLE_OWNED = "owned";
    public static final String ROLE_OUTCOME = "outcome";
    public static final String ROLE_FRONTIER = "frontier";
    public static final String ROLE_RELATIONSHIP = "relationship";
    public static final String ROLE_FRONTIER_RELATIONSHIP = "frontier-relationship";
    public static final String ROLE_SUPPLEMENTAL_RELATIONSHIP = "supplemental-relationship";
    public static final String TITLE_ID = "business-graph-layout-title";
    public static final String DESCRIPTION_ID = "business-graph-layout-description";

    private BusinessGraphLayoutTokens() {
    }
}
