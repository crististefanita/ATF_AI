package com.ai.skills.mcp.contracts.model;

public final class RuleSpec extends ContractSpec {
    private RuleSpec(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends ContractSpec.Builder<Builder> {
        @Override
        public RuleSpec build() {
            return new RuleSpec(this);
        }
    }
}
