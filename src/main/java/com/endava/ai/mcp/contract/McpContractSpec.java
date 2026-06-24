package com.endava.ai.mcp.contract;

import com.endava.ai.mcp.McpViolationSeverity;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.util.Objects;

public final class McpContractSpec {
    private final String id;
    private final McpRuleCategory category;
    private final McpViolationSeverity severity;
    private final String htmlReq;
    private final String artifact;

    private McpContractSpec(Builder builder) {
        this.id = requireText(builder.id, "id");
        this.category = Objects.requireNonNull(builder.category, "category");
        this.severity = Objects.requireNonNull(builder.severity, "severity");
        this.htmlReq = requireText(builder.htmlReq, "htmlReq");
        this.artifact = requireText(builder.artifact, "artifact");
    }

    public static McpContractSpec fromRule(McpValidationRule rule) {
        return fromClass(rule.getClass());
    }

    public static McpContractSpec fromClass(Class<?> ruleClass) {
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

    public String id() {
        return id;
    }

    public McpRuleCategory category() {
        return category;
    }

    public McpViolationSeverity severity() {
        return severity;
    }

    public String htmlReq() {
        return htmlReq;
    }

    public String artifact() {
        return artifact;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing MCP contract field: " + field);
        }
        return value.trim();
    }

    public static final class Builder {
        private final String id;
        private McpRuleCategory category;
        private McpViolationSeverity severity = McpViolationSeverity.BLOCKER;
        private String htmlReq;
        private String artifact;

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

        public McpContractSpec build() {
            return new McpContractSpec(this);
        }
    }
}
