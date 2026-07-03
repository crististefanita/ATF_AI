package com.ai.skills.mcp.rules;

import com.ai.skills.mcp.contract.McpProfile;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.contracts.policy.McpRuleProfileSpec;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves YAML-declared MCP rule profiles into executable validators.
 */
final class McpRuleRegistry {
    private McpRuleRegistry() {
    }

    static List<McpValidationRule> fullPackageRules() {
        return rules(McpProfile.FULL_PACKAGE);
    }

    static List<McpValidationRule> suiteAtlasRules() {
        return rules(McpProfile.SUITE_ATLAS);
    }

    static List<McpValidationRule> universalRules() {
        return rules(McpProfile.UNIVERSAL);
    }

    static List<McpValidationRule> uiPackageRules() {
        return rules(McpProfile.UI_PACKAGE);
    }

    static List<McpValidationRule> htmlReportRules() {
        return rules(McpProfile.HTML_REPORT);
    }

    static List<McpValidationRule> htmlExampleCandidateRules() {
        return rules(McpProfile.HTML_EXAMPLE_CANDIDATE);
    }

    static List<String> ruleIds(McpProfile profile) {
        Set<String> resolved = new LinkedHashSet<>();
        resolve(profile, resolved, EnumSet.noneOf(McpProfile.class));
        return List.copyOf(resolved);
    }

    private static List<McpValidationRule> rules(McpProfile profile) {
        return McpExecutableRuleCatalog.createAll(ruleIds(profile));
    }

    private static void resolve(McpProfile profile, Set<String> resolved, Set<McpProfile> visiting) {
        if (!visiting.add(profile)) {
            throw new IllegalStateException("Cyclic MCP rule profile include detected: " + visiting + " -> " + profile);
        }
        McpRuleProfileSpec spec = ContractCatalog.requireRuleProfile(profile);
        for (McpProfile included : spec.includeProfiles()) {
            resolve(included, resolved, visiting);
        }
        resolved.addAll(spec.ruleIds());
        visiting.remove(profile);
    }
}
