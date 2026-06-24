package com.endava.ai.mcp.rules;

import com.endava.ai.mcp.contract.McpProfile;

import java.util.List;
import java.util.Objects;

public final class McpRuleSet {
    private final McpProfile profile;
    private final List<McpValidationRule> rules;

    private McpRuleSet(McpProfile profile, List<McpValidationRule> rules) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.rules = List.copyOf(rules);
    }

    public static McpRuleSet of(McpProfile profile, List<McpValidationRule> rules) {
        return new McpRuleSet(profile, rules);
    }

    public McpProfile profile() {
        return profile;
    }

    public List<McpValidationRule> rules() {
        return rules;
    }
}
