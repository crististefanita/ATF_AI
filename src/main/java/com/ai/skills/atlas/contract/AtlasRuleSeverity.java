package com.ai.skills.atlas.contract;

public enum AtlasRuleSeverity {
    BLOCKER(0),
    MAJOR(10),
    MINOR(20),
    INFO(30);

    private final int rank;

    AtlasRuleSeverity(int rank) {
        this.rank = rank;
    }

    public boolean atLeastAsSevereAs(AtlasRuleSeverity other) {
        return rank <= other.rank;
    }

    public boolean stricterThan(AtlasRuleSeverity other) {
        return rank < other.rank;
    }
}
