package com.ai.skills.atlas.contract;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

@Getter
@Accessors(fluent = true)
public final class AtlasRuleSpec {
    private final String id;
    private final AtlasContractProfile profile;
    private final AtlasRuleCategory category;
    private final AtlasRuleSeverity severity;
    private final String owner;
    private final String artifact;
    private final String why;
    private final String repair;

    private AtlasRuleSpec(String id,
                          AtlasContractProfile profile,
                          AtlasRuleCategory category,
                          AtlasRuleSeverity severity,
                          String owner,
                          String artifact,
                          String why,
                          String repair) {
        this.id = Objects.requireNonNull(id, "id");
        this.profile = Objects.requireNonNull(profile, "profile");
        this.category = Objects.requireNonNull(category, "category");
        this.severity = Objects.requireNonNull(severity, "severity");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.artifact = Objects.requireNonNull(artifact, "artifact");
        this.why = Objects.requireNonNull(why, "why");
        this.repair = Objects.requireNonNull(repair, "repair");
    }

    public static AtlasRuleSpec of(String id,
                                   AtlasContractProfile profile,
                                   AtlasRuleCategory category,
                                   AtlasRuleSeverity severity,
                                   String why,
                                   String repair) {
        return new AtlasRuleSpec(id,
                profile,
                category,
                severity,
                AtlasContractArtifacts.HTML_OWNER,
                AtlasContractArtifacts.ATLAS_HTML,
                why,
                repair);
    }

}
