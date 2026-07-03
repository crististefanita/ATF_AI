package com.ai.skills.mcp.contract.execution;

import com.ai.skills.mcp.contract.McpProfile;
import com.ai.skills.mcp.rules.McpDefaultRules;

import java.util.List;
import java.util.stream.Collectors;

public final class McpRuleRoutingCatalog {
    private static final List<McpProfile> ROUTED_PROFILES = List.of(
            McpProfile.UNIVERSAL,
            McpProfile.UI_PACKAGE,
            McpProfile.HTML_REPORT,
            McpProfile.HTML_EXAMPLE_CANDIDATE,
            McpProfile.SUITE_ATLAS,
            McpProfile.FULL_PACKAGE);

    private McpRuleRoutingCatalog() {
    }

    public static List<McpProfile> routedProfiles() {
        return ROUTED_PROFILES;
    }

    public static List<McpRuleRoute> routes() {
        return ROUTED_PROFILES.stream()
                .flatMap(profile -> McpDefaultRules.rulesFor(profile).stream()
                        .map(rule -> McpRuleRoute.of(profile, rule)))
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<McpRuleRoute> routesFor(McpProfile profile) {
        return routes().stream()
                .filter(route -> route.profile() == profile)
                .collect(Collectors.toUnmodifiableList());
    }
}
