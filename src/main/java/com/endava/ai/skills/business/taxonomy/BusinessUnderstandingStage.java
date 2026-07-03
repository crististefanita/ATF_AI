package com.endava.ai.skills.business.taxonomy;

/**
 * Canonical semantic path shared by MCP and Atlas.
 */
public enum BusinessUnderstandingStage {
    NORMALIZE_INPUT(10, "Normalize evidence input"),
    MAP_SUPPORT_LAYERS(20, "Map support and application layers"),
    IDENTIFY_BUSINESS_OBJECTS(30, "Identify durable business objects"),
    TRACE_BUSINESS_CLAIMS(40, "Trace business claims to source or artifact evidence"),
    MAP_PROOF_CHAIN(50, "Map claim to proof chain"),
    SCORE_UNDERSTANDING_LIMITS(60, "Score business understanding and limits"),
    PUBLISH_SEMANTIC_SUMMARY(70, "Publish semantic summary for the owning skill");

    private final int order;
    private final String description;

    BusinessUnderstandingStage(int order, String description) {
        this.order = order;
        this.description = description;
    }

    public int order() {
        return order;
    }

    public String description() {
        return description;
    }
}
