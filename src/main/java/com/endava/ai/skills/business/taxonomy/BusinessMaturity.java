package com.endava.ai.skills.business.taxonomy;

/**
 * Shared maturity vocabulary used by Atlas and MCP when judging business proof depth.
 */
public enum BusinessMaturity {
    OWNED("owned", "Durable or reopenable business proof exists."),
    PARTIAL("partial", "A meaningful slice is proven, but the full lifecycle is not closed."),
    DIAGNOSTIC("diagnostic", "The evidence helps investigation but does not own the business outcome."),
    SURFACE_ONLY("surface-only", "The visible surface is reached without deeper lifecycle proof."),
    BLOCKED("blocked", "The current evidence prevents accepting the business claim.");

    private final String label;
    private final String meaning;

    BusinessMaturity(String label, String meaning) {
        this.label = label;
        this.meaning = meaning;
    }

    public String label() {
        return label;
    }

    public String meaning() {
        return meaning;
    }

    public boolean strongOwnership() {
        return this == OWNED;
    }
}
