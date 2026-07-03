package com.ai.skills.mcp.rules;

import org.testng.Assert;
import org.testng.annotations.Test;
import com.ai.skills.mcp.contract.McpProfile;
import com.ai.skills.mcp.contract.execution.McpRuleExecutionDescriptor;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.contracts.policy.McpRuleProfileSpec;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class McpExecutableRuleCatalogContractTest {
    @Test
    public void executableRuleCatalogCreatesEveryRegisteredRuleWithMatchingId() {
        List<String> ids = McpExecutableRuleCatalog.ruleIds();

        Assert.assertFalse(ids.isEmpty(), "Executable MCP rule catalog must not be empty.");
        Assert.assertEquals(Set.copyOf(ids).size(), ids.size(), "Executable MCP rule ids must be unique.");

        for (String id : ids) {
            McpValidationRule rule = McpExecutableRuleCatalog.createVerified(id);
            Assert.assertEquals(rule.contractSpec().id(), id,
                    "Factory must create the rule declared by its catalog id.");
            Assert.assertFalse(rule.contractSpec().repairHint().isBlank(),
                    "Every executable MCP rule must expose repair guidance: " + id);
        }
    }

    @Test
    public void executableRulesExposeTypedAgentFacingDescriptors() {
        for (String id : McpExecutableRuleCatalog.ruleIds()) {
            McpValidationRule rule = McpExecutableRuleCatalog.createVerified(id);

            Assert.assertEquals(rule.contractSpec().id(), id,
                    "Descriptor source rule must match executable catalog id.");
            Assert.assertNotNull(McpRuleExecutionDescriptor.from(rule).family(),
                    "Every executable MCP rule must expose an agent-facing family: " + id);
            Assert.assertNotNull(McpRuleExecutionDescriptor.from(rule).cost(),
                    "Every executable MCP rule must expose an execution cost: " + id);
            Assert.assertFalse(McpRuleExecutionDescriptor.from(rule).repairRoute().isBlank(),
                    "Every executable MCP rule must expose a repair route: " + id);
            Assert.assertFalse(McpRuleExecutionDescriptor.from(rule).artifactFamilyName().isBlank(),
                    "Every executable MCP rule must expose an artifact-family mapping or UNMAPPED marker: " + id);
        }
    }

    @Test
    public void registryProfilesDoNotDuplicateRuleIds() {
        assertUnique("fullPackageRules", McpRuleRegistry::fullPackageRules);
        assertUnique("suiteAtlasRules", McpRuleRegistry::suiteAtlasRules);
        assertUnique("universalRules", McpRuleRegistry::universalRules);
        assertUnique("uiPackageRules", McpRuleRegistry::uiPackageRules);
        assertUnique("htmlReportRules", McpRuleRegistry::htmlReportRules);
        assertUnique("htmlExampleCandidateRules", McpRuleRegistry::htmlExampleCandidateRules);
    }

    @Test
    public void fullPackageRegistryCarriesCloseoutAndKpiRules() {
        Set<String> ids = ruleIds(McpRuleRegistry.fullPackageRules());

        Assert.assertTrue(ids.contains(McpExecutableRuleCatalog.PACKAGE_CLOSEOUT),
                "Full-package profile must include final MCP package close-out.");
        Assert.assertTrue(ids.contains(McpExecutableRuleCatalog.KPI_CLOSEOUT),
                "Full-package profile must include KPI close-out artifact checks.");
        Assert.assertTrue(ids.contains(McpExecutableRuleCatalog.LOCAL_LINKS),
                "Full-package profile must keep final report links auditable.");
    }

    @Test
    public void yamlRuleProfilesCoverEveryMcpProfileAndReferenceExecutableRules() {
        Set<McpProfile> yamlProfiles = ContractCatalog.ruleProfiles().stream()
                .map(McpRuleProfileSpec::id)
                .collect(Collectors.toUnmodifiableSet());

        Assert.assertEquals(yamlProfiles, EnumSet.allOf(McpProfile.class),
                "skills/mcp/contracts/rule-profiles.yml must describe every executable MCP profile.");

        Set<String> executableIds = Set.copyOf(McpExecutableRuleCatalog.ruleIds());
        for (McpRuleProfileSpec profile : ContractCatalog.ruleProfiles()) {
            Assert.assertFalse(profile.role().isBlank(), "Missing profile role for " + profile.id());
            Assert.assertFalse(McpRuleRegistry.ruleIds(profile.id()).isEmpty(),
                    "Profile must resolve to at least one executable rule: " + profile.id());
            for (String ruleId : profile.ruleIds()) {
                Assert.assertTrue(executableIds.contains(ruleId),
                        "YAML profile " + profile.id() + " references missing executable MCP rule: " + ruleId);
            }
        }
    }

    private static void assertUnique(String label, Supplier<List<McpValidationRule>> rules) {
        List<String> ids = rules.get().stream()
                .map(rule -> rule.contractSpec().id())
                .collect(Collectors.toUnmodifiableList());

        Assert.assertEquals(Set.copyOf(ids).size(), ids.size(),
                label + " must not include duplicate executable rule ids.");
    }

    private static Set<String> ruleIds(List<McpValidationRule> rules) {
        return rules.stream()
                .map(rule -> rule.contractSpec().id())
                .collect(Collectors.toUnmodifiableSet());
    }
}
