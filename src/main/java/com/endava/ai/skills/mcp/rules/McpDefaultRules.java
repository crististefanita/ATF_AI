package com.endava.ai.skills.mcp.rules;

import com.endava.ai.skills.mcp.contract.McpProfile;

import java.util.List;

/**
 * Public MCP rule facade.
 * <p>
 * Keep profile selection here and executable rule composition in
 * {@link McpRuleRegistry}. That split makes this class read as contract intent
 * instead of a long constructor inventory.
 */
public final class McpDefaultRules {
    private McpDefaultRules() {
    }

    public static List<McpValidationRule> create() {
        return fullContract();
    }

    public static McpRuleSet ruleSet(McpProfile profile) {
        return McpRuleSet.of(profile, rulesFor(profile));
    }

    public static List<McpValidationRule> rulesFor(McpProfile profile) {
        switch (profile) {
            case UNIVERSAL:
                return universalContract();
            case UI_PACKAGE:
                return uiContract();
            case HTML_REPORT:
                return htmlReportContract();
            case HTML_EXAMPLE_CANDIDATE:
                return htmlExampleCandidateContract();
            case FULL_PACKAGE:
                return fullContract();
            case SUITE_ATLAS:
                return suiteAtlasContract();
            default:
                throw new IllegalArgumentException("Unsupported MCP profile: " + profile);
        }
    }

    public static List<McpValidationRule> fullContract() {
        return McpRuleRegistry.fullPackageRules();
    }

    public static List<McpValidationRule> universalContract() {
        return McpRuleRegistry.universalRules();
    }

    public static List<McpValidationRule> uiContract() {
        return McpRuleRegistry.uiPackageRules();
    }

    public static List<McpValidationRule> htmlReportContract() {
        return McpRuleRegistry.htmlReportRules();
    }

    public static List<McpValidationRule> htmlExampleCandidateContract() {
        return McpRuleRegistry.htmlExampleCandidateRules();
    }

    public static List<McpValidationRule> suiteAtlasContract() {
        return McpRuleRegistry.suiteAtlasRules();
    }
}
