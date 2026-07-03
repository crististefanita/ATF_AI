package com.ai.skills.mcp.contracts.catalog;

import com.ai.core.support.contract.yaml.ContractIndex;
import com.ai.skills.mcp.contracts.execution.McpAgenticExecutionStepSpec;
import com.ai.skills.mcp.contracts.policy.McpCloseoutArtifactSpec;
import com.ai.skills.mcp.contracts.policy.McpRuleProfileSpec;
import com.ai.skills.mcp.contracts.policy.McpTestNgProfileSpec;
import com.ai.skills.mcp.contracts.suites.McpSuiteSpec;
import com.ai.skills.mcp.contracts.yaml.ContractSpecLoader;

import java.util.List;
import java.util.stream.Collectors;

final class McpExecutionContractCatalog {
    private static final ContractLazy<List<McpTestNgProfileSpec>> TESTNG_PROFILES = ContractLazy.of(
            McpContractResources.TESTNG_PROFILES,
            () -> ContractSpecLoader.loadEntries(McpContractResources.TESTNG_PROFILES).stream()
                    .map(entry -> McpTestNgProfileSpec.from(entry, McpContractResources.TESTNG_PROFILES))
                    .collect(Collectors.toUnmodifiableList()));
    private static final ContractLazy<List<McpAgenticExecutionStepSpec>> AGENTIC_EXECUTION_FLOW = ContractLazy.of(
            McpContractResources.AGENTIC_EXECUTION_FLOW,
            () -> ContractSpecLoader.loadEntries(McpContractResources.AGENTIC_EXECUTION_FLOW).stream()
                    .map(entry -> McpAgenticExecutionStepSpec.from(
                            entry,
                            McpContractResources.AGENTIC_EXECUTION_FLOW))
                    .collect(Collectors.toUnmodifiableList()));
    private static final ContractLazy<ContractIndex<McpTestNgProfileSpec>> TESTNG_PROFILES_BY_ID = ContractLazy.of(
            McpContractResources.TESTNG_PROFILES + "#by-id",
            () -> ContractIndex.unique(McpContractResources.TESTNG_PROFILES,
                    TESTNG_PROFILES.get(),
                    McpTestNgProfileSpec::id));
    private static final ContractLazy<List<McpRuleProfileSpec>> RULE_PROFILES = ContractLazy.of(
            McpContractResources.RULE_PROFILES,
            () -> ContractSpecLoader.loadEntries(McpContractResources.RULE_PROFILES).stream()
                    .map(entry -> McpRuleProfileSpec.from(entry, McpContractResources.RULE_PROFILES))
                    .collect(Collectors.toUnmodifiableList()));
    private static final ContractLazy<ContractIndex<McpRuleProfileSpec>> RULE_PROFILES_BY_ID = ContractLazy.of(
            McpContractResources.RULE_PROFILES + "#by-id",
            () -> ContractIndex.unique(McpContractResources.RULE_PROFILES,
                    RULE_PROFILES.get(),
                    spec -> spec.id().name()));
    private static final ContractLazy<List<McpCloseoutArtifactSpec>> CLOSEOUT_ARTIFACT_SPECS = ContractLazy.of(
            McpContractResources.PACKAGE_CLOSEOUT_ARTIFACTS,
            () -> ContractSpecLoader.loadEntries(McpContractResources.PACKAGE_CLOSEOUT_ARTIFACTS).stream()
                    .map(entry -> McpCloseoutArtifactSpec.from(entry, McpContractResources.PACKAGE_CLOSEOUT_ARTIFACTS))
                    .collect(Collectors.toUnmodifiableList()));
    private static final ContractLazy<List<McpSuiteSpec>> MCP_SUITES = ContractLazy.of(
            McpContractResources.MCP_SUITES,
            () -> ContractSpecLoader.loadEntries(McpContractResources.MCP_SUITES).stream()
                    .map(entry -> McpSuiteSpec.from(entry, McpContractResources.MCP_SUITES))
                    .collect(Collectors.toUnmodifiableList()));
    private static final ContractLazy<ContractIndex<McpSuiteSpec>> MCP_SUITES_BY_ID = ContractLazy.of(
            McpContractResources.MCP_SUITES + "#by-id",
            () -> ContractIndex.unique(McpContractResources.MCP_SUITES, MCP_SUITES.get(), McpSuiteSpec::id));

    private McpExecutionContractCatalog() {
    }

    static List<McpTestNgProfileSpec> testNgProfiles() {
        return TESTNG_PROFILES.get();
    }

    static List<McpAgenticExecutionStepSpec> agenticExecutionFlow() {
        return AGENTIC_EXECUTION_FLOW.get();
    }

    static McpTestNgProfileSpec requireTestNgProfile(String id) {
        return TESTNG_PROFILES_BY_ID.get().require(id);
    }

    static List<McpRuleProfileSpec> ruleProfiles() {
        return RULE_PROFILES.get();
    }

    static McpRuleProfileSpec requireRuleProfile(com.ai.skills.mcp.contract.McpProfile profile) {
        return RULE_PROFILES_BY_ID.get().require(profile.name());
    }

    static List<McpCloseoutArtifactSpec> closeoutArtifactSpecs() {
        return CLOSEOUT_ARTIFACT_SPECS.get();
    }

    static McpCloseoutArtifactSpec primaryCloseoutArtifactSpec() {
        List<McpCloseoutArtifactSpec> specs = CLOSEOUT_ARTIFACT_SPECS.get();
        if (specs.isEmpty()) {
            throw new IllegalStateException("Missing MCP close-out artifact contract specs");
        }
        return specs.get(0);
    }

    static List<McpSuiteSpec> mcpSuites() {
        return MCP_SUITES.get();
    }

    static McpSuiteSpec requireMcpSuite(String id) {
        return MCP_SUITES_BY_ID.get().require(id);
    }
}
