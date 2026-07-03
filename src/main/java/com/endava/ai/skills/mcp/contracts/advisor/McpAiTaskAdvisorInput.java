package com.endava.ai.skills.mcp.contracts.advisor;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * Small run-shape snapshot used to decide which advisor entries are relevant
 * for the next agentic iteration.
 */
@Getter
@Accessors(fluent = true)
public final class McpAiTaskAdvisorInput {
    private final String taskScope;
    private final String modelTier;
    private final String acceptanceCriteria;
    private final String stopCondition;
    private final String valueRationale;

    private McpAiTaskAdvisorInput(Builder builder) {
        this.taskScope = normalize(builder.taskScope);
        this.modelTier = normalize(builder.modelTier);
        this.acceptanceCriteria = normalize(builder.acceptanceCriteria);
        this.stopCondition = normalize(builder.stopCondition);
        this.valueRationale = normalize(builder.valueRationale);
    }

    public static Builder builder() {
        return new Builder();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public static final class Builder {
        private String taskScope = "";
        private String modelTier = "";
        private String acceptanceCriteria = "";
        private String stopCondition = "";
        private String valueRationale = "";

        private Builder() {
        }

        public Builder taskScope(String taskScope) {
            this.taskScope = Objects.toString(taskScope, "");
            return this;
        }

        public Builder modelTier(String modelTier) {
            this.modelTier = Objects.toString(modelTier, "");
            return this;
        }

        public Builder acceptanceCriteria(String acceptanceCriteria) {
            this.acceptanceCriteria = Objects.toString(acceptanceCriteria, "");
            return this;
        }

        public Builder stopCondition(String stopCondition) {
            this.stopCondition = Objects.toString(stopCondition, "");
            return this;
        }

        public Builder valueRationale(String valueRationale) {
            this.valueRationale = Objects.toString(valueRationale, "");
            return this;
        }

        public McpAiTaskAdvisorInput build() {
            return new McpAiTaskAdvisorInput(this);
        }
    }
}
