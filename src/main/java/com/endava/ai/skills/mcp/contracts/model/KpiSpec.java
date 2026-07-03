package com.endava.ai.skills.mcp.contracts.model;

public final class KpiSpec extends ContractSpec {
    private KpiSpec(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends ContractSpec.Builder<Builder> {
        @Override
        public KpiSpec build() {
            return new KpiSpec(this);
        }
    }
}
