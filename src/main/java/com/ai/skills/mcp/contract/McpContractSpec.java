package com.ai.skills.mcp.contract;

import com.ai.skills.mcp.McpViolationSeverity;
import com.ai.skills.mcp.rules.McpValidationRule;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

@Getter
@Accessors(fluent = true)
public final class McpContractSpec {
    private final String id;
    private final McpRuleCategory category;
    private final McpViolationSeverity severity;
    private final String htmlReq;
    private final String artifact;
    private final String why;
    private final String repairHint;

    private McpContractSpec(Builder builder) {
        this.id = requireText(builder.id, "id");
        this.category = Objects.requireNonNull(builder.category, "category");
        this.severity = Objects.requireNonNull(builder.severity, "severity");
        this.htmlReq = requireText(builder.htmlReq, "htmlReq");
        this.artifact = requireText(builder.artifact, "artifact");
        this.why = defaultText(builder.why, defaultWhy(this.category, this.htmlReq));
        this.repairHint = defaultText(builder.repairHint, defaultRepairHint(this.artifact));
    }

    public static McpContractSpec fromRule(McpValidationRule rule) {
        return fromClass(rule.getClass());
    }

    public static McpContractSpec fromClass(Class<?> ruleClass) {
        McpContractSpec catalogSpec = McpRuleCatalog.specFor(ruleClass).orElse(null);
        if (catalogSpec != null) {
            return catalogSpec;
        }
        return fromAnnotation(ruleClass);
    }

    static McpContractSpec fromAnnotation(Class<?> ruleClass) {
        McpContract contract = ruleClass.getAnnotation(McpContract.class);
        if (contract == null) {
            return null;
        }
        return builder(contract.id())
                .category(contract.category())
                .severity(contract.severity())
                .htmlReq(contract.htmlReq())
                .artifact(contract.artifact())
                .build();
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public McpContractSpec withNarrative(String why, String repairHint) {
        return builder(id)
                .category(category)
                .severity(severity)
                .htmlReq(htmlReq)
                .artifact(artifact)
                .why(why)
                .repairHint(repairHint)
                .build();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing MCP contract field: " + field);
        }
        return value.trim();
    }

    private static String defaultText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private static String defaultWhy(McpRuleCategory category, String htmlReq) {
        return "Protects the " + category.name().toLowerCase().replace('_', ' ')
                + " contract declared by " + htmlReq + ".";
    }

    private static String defaultRepairHint(String artifact) {
        return "Inspect " + artifact
                + ", repair the violated evidence, regenerate affected package artifacts, and rerun MCP.";
    }

    public static final class Builder {
        private final String id;
        private McpRuleCategory category;
        private McpViolationSeverity severity = McpViolationSeverity.BLOCKER;
        private String htmlReq;
        private String artifact;
        private String why;
        private String repairHint;

        private Builder(String id) {
            this.id = id;
        }

        public Builder category(McpRuleCategory category) {
            this.category = category;
            return this;
        }

        public Builder severity(McpViolationSeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder htmlReq(String htmlReq) {
            this.htmlReq = htmlReq;
            return this;
        }

        public Builder artifact(String artifact) {
            this.artifact = artifact;
            return this;
        }

        public Builder why(String why) {
            this.why = why;
            return this;
        }

        public Builder repairHint(String repairHint) {
            this.repairHint = repairHint;
            return this;
        }

        public McpContractSpec build() {
            return new McpContractSpec(this);
        }
    }
}
