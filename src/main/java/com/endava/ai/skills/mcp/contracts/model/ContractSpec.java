package com.endava.ai.skills.mcp.contracts.model;

import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Declarative MCP contract metadata loaded from resources.
 * Java rules remain executable truth; this model explains contract intent, repair target and score impact.
 */
@Getter
@Accessors(fluent = true)
public class ContractSpec {
    private final String id;
    private final String profile;
    private final String category;
    private final String severity;
    private final String sourceArtifact;
    private final String formula;
    private final RepairSpec repair;
    private final String why;
    private final String qualityMeaning;
    private final String agentAction;
    private final String repairHint;
    private final String repairCommand;
    private final String capImpact;
    private final String publishabilityImpact;
    private final boolean metadataOnly;
    private final String validator;

    protected ContractSpec(Builder<?> builder) {
        this.id = required(builder.id, "id");
        this.profile = required(builder.profile, "profile");
        this.category = required(builder.category, "category");
        this.severity = required(builder.severity, "severity");
        this.sourceArtifact = required(builder.sourceArtifact, "sourceArtifact");
        this.formula = required(builder.formula, "formula");
        this.repair = Objects.requireNonNull(builder.repair, "repair");
        this.why = required(builder.why, "why");
        this.qualityMeaning = required(builder.qualityMeaning, "qualityMeaning");
        this.agentAction = required(builder.agentAction, "agentAction");
        this.repairHint = required(builder.repairHint, "repairHint");
        this.repairCommand = required(builder.repairCommand, "repairCommand");
        this.capImpact = required(builder.capImpact, "capImpact");
        this.publishabilityImpact = required(builder.publishabilityImpact, "publishabilityImpact");
        this.metadataOnly = builder.metadataOnly;
        this.validator = optional(builder.validator);
    }

    protected static String required(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing MCP YAML contract field: " + field);
        }
        return value.trim();
    }

    protected static String optional(String value) {
        return value == null ? "" : value.trim();
    }

    public static class Builder<T extends Builder<T>> {
        private String id;
        private String profile;
        private String category;
        private String severity;
        private String sourceArtifact;
        private String formula;
        private RepairSpec repair;
        private String why;
        private String qualityMeaning;
        private String agentAction;
        private String repairHint;
        private String repairCommand;
        private String capImpact;
        private String publishabilityImpact;
        private boolean metadataOnly;
        private String validator;

        protected T self() {
            @SuppressWarnings("unchecked")
            T typed = (T) this;
            return typed;
        }

        public T id(String id) {
            this.id = id;
            return self();
        }

        public T profile(String profile) {
            this.profile = profile;
            return self();
        }

        public T category(String category) {
            this.category = category;
            return self();
        }

        public T severity(String severity) {
            this.severity = severity;
            return self();
        }

        public T sourceArtifact(String sourceArtifact) {
            this.sourceArtifact = sourceArtifact;
            return self();
        }

        public T formula(String formula) {
            this.formula = formula;
            return self();
        }

        public T repair(RepairSpec repair) {
            this.repair = repair;
            return self();
        }

        public T why(String why) {
            this.why = why;
            return self();
        }

        public T qualityMeaning(String qualityMeaning) {
            this.qualityMeaning = qualityMeaning;
            return self();
        }

        public T agentAction(String agentAction) {
            this.agentAction = agentAction;
            return self();
        }

        public T repairHint(String repairHint) {
            this.repairHint = repairHint;
            return self();
        }

        public T repairCommand(String repairCommand) {
            this.repairCommand = repairCommand;
            return self();
        }

        public T capImpact(String capImpact) {
            this.capImpact = capImpact;
            return self();
        }

        public T publishabilityImpact(String publishabilityImpact) {
            this.publishabilityImpact = publishabilityImpact;
            return self();
        }

        public T metadataOnly(boolean metadataOnly) {
            this.metadataOnly = metadataOnly;
            return self();
        }

        public T validator(String validator) {
            this.validator = validator;
            return self();
        }

        public ContractSpec build() {
            return new ContractSpec(this);
        }
    }

    @Getter
    @Accessors(fluent = true)
    public static final class RepairSpec {
        private final String observedArtifact;
        private final String primaryArtifact;
        private final String supportingArtifact;
        private final String route;

        private RepairSpec(String observedArtifact,
                           String primaryArtifact,
                           String supportingArtifact,
                           String route) {
            this.observedArtifact = required(observedArtifact, "repair.observedArtifact");
            this.primaryArtifact = required(primaryArtifact, "repair.primaryArtifact");
            this.supportingArtifact = required(supportingArtifact, "repair.supportingArtifact");
            this.route = required(route, "repair.route");
        }

        public static RepairSpec of(String observedArtifact,
                                    String primaryArtifact,
                                    String supportingArtifact,
                                    String route) {
            return new RepairSpec(observedArtifact, primaryArtifact, supportingArtifact, route);
        }

    }
}
