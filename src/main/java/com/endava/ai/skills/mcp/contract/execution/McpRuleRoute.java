package com.endava.ai.skills.mcp.contract.execution;

import com.endava.ai.skills.mcp.contract.McpContractSpec;
import com.endava.ai.skills.mcp.contract.McpProfile;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

@Getter
@Accessors(fluent = true)
public final class McpRuleRoute {
    private final McpProfile profile;
    private final Class<? extends McpValidationRule> ruleClass;
    private final McpContractSpec contractSpec;
    private final McpRuleExecutionDescriptor descriptor;

    private McpRuleRoute(McpProfile profile,
                         Class<? extends McpValidationRule> ruleClass,
                         McpContractSpec contractSpec,
                         McpRuleExecutionDescriptor descriptor) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.ruleClass = Objects.requireNonNull(ruleClass, "ruleClass");
        this.contractSpec = Objects.requireNonNull(contractSpec, "contractSpec");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    public static McpRuleRoute of(McpProfile profile, McpValidationRule rule) {
        return new McpRuleRoute(profile, rule.getClass(), rule.contractSpec(), McpRuleExecutionDescriptor.from(rule));
    }

}
