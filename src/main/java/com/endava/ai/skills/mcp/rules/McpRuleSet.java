package com.endava.ai.skills.mcp.rules;

import com.endava.ai.skills.mcp.contract.McpProfile;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
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
}
