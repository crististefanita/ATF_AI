package com.ai.skills.mcp.validation.profile;

import java.util.List;

public final class McpReportSections {
    public static final String EXECUTIVE_CONTEXT   = "Executive Context";
    public static final String BUSINESS_FLOW_GRAPH = "Business Flow Graph";
    public static final String SCORE_OVERVIEW      = "Score Overview";
    public static final String PROOF_MAPPING       = "Proof Mapping";
    public static final String RUN_AUDIT           = "Run Audit";
    public static final String ARTIFACT_INDEX      = "Artifact Index";
    public static final String CLOSE_OUT           = "Close-Out";
    public static final String OVERALL             = "Overall";

    public static final List<String> MAIN = List.of(
            BUSINESS_FLOW_GRAPH,
            SCORE_OVERVIEW,
            PROOF_MAPPING,
            RUN_AUDIT,
            ARTIFACT_INDEX,
            CLOSE_OUT);

    public static final List<String> COLLAPSED_MAIN = List.of(
            SCORE_OVERVIEW,
            PROOF_MAPPING,
            RUN_AUDIT,
            ARTIFACT_INDEX,
            CLOSE_OUT);

    public static final List<String> TEMPLATE_ORDER = List.of(
            EXECUTIVE_CONTEXT,
            BUSINESS_FLOW_GRAPH,
            SCORE_OVERVIEW,
            PROOF_MAPPING,
            RUN_AUDIT,
            ARTIFACT_INDEX,
            CLOSE_OUT);

    private McpReportSections() {
    }
}
