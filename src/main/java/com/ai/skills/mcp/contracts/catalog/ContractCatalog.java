package com.ai.skills.mcp.contracts.catalog;

import com.ai.core.support.contract.yaml.ContractResource;
import com.ai.skills.mcp.contracts.candidate.McpHtmlExCorpusSpec;
import com.ai.skills.mcp.contracts.advisor.McpAiTaskAdvisorSpec;
import com.ai.skills.mcp.contracts.checks.McpCheckSpec;
import com.ai.skills.mcp.contracts.policy.McpClaimActionContractSpec;
import com.ai.skills.mcp.contracts.execution.McpCloseoutEvidence;
import com.ai.skills.mcp.contracts.execution.McpAgenticExecutionStepSpec;
import com.ai.skills.mcp.contracts.governance.McpGovernanceMapEntry;
import com.ai.skills.mcp.contracts.model.KpiSpec;
import com.ai.skills.mcp.contracts.policy.McpCloseoutArtifactSpec;
import com.ai.skills.mcp.contracts.provenance.McpExtentProvenanceSpec;
import com.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import com.ai.skills.mcp.contracts.policy.McpPackagePolicySpec;
import com.ai.skills.mcp.contracts.policy.McpProtectedRootSpec;
import com.ai.skills.mcp.contracts.policy.McpReportHyperlinkSpec;
import com.ai.skills.mcp.contracts.policy.McpReportTemplateSpec;
import com.ai.skills.mcp.contracts.policy.McpRootCauseGroupSpec;
import com.ai.skills.mcp.contracts.policy.McpRuleProfileSpec;
import com.ai.skills.mcp.contracts.policy.McpScoreCapSpec;
import com.ai.skills.mcp.contracts.policy.McpScoreTruthSyncSpec;
import com.ai.skills.mcp.contracts.policy.McpSizeBudgetSpec;
import com.ai.skills.mcp.contracts.policy.McpSourceEvolutionLedgerSpec;
import com.ai.skills.mcp.contracts.policy.McpTestNgProfileSpec;
import com.ai.skills.mcp.contracts.suites.McpSuiteSpec;

import java.util.List;

/**
 * Stable facade for MCP YAML contract catalogs.
 *
 * <p>Keep callers on this class. Domain-specific loading lives in smaller
 * package-private catalogs so YAML growth does not turn this facade into a
 * second rules engine.</p>
 */
public final class ContractCatalog {
    public static final String SOURCE_EVOLUTION_KPI_RESOURCE = McpContractResources.SOURCE_EVOLUTION_KPI;
    public static final String TESTNG_PROFILES_RESOURCE = McpContractResources.TESTNG_PROFILES;
    public static final String AGENTIC_EXECUTION_FLOW_RESOURCE = McpContractResources.AGENTIC_EXECUTION_FLOW;
    public static final String RULE_PROFILES_RESOURCE = McpContractResources.RULE_PROFILES;
    public static final String PACKAGE_CLOSEOUT_ARTIFACTS_RESOURCE = McpContractResources.PACKAGE_CLOSEOUT_ARTIFACTS;
    public static final String REPORT_HYPERLINKS_RESOURCE = McpContractResources.REPORT_HYPERLINKS;
    public static final String SCORE_CAPS_RESOURCE = McpContractResources.SCORE_CAPS;
    public static final String SCORE_TRUTH_SYNC_RESOURCE = McpContractResources.SCORE_TRUTH_SYNC;
    public static final String AI_TASK_ADVISOR_RESOURCE = McpContractResources.AI_TASK_ADVISOR;
    public static final String ATLAS_QUALITY_BRIDGE_RESOURCE = McpContractResources.ATLAS_QUALITY_BRIDGE;
    public static final String PROTECTED_ROOTS_RESOURCE = McpContractResources.PROTECTED_ROOTS;
    public static final String SOURCE_EVOLUTION_LEDGER_RESOURCE = McpContractResources.SOURCE_EVOLUTION_LEDGER;
    public static final String PACKAGE_POLICY_RESOURCE = McpContractResources.PACKAGE_POLICY;
    public static final String PACKAGE_SHAPE_RESOURCE = McpContractResources.PACKAGE_SHAPE;
    public static final String REPORT_TEMPLATE_RESOURCE = McpContractResources.REPORT_TEMPLATE;
    public static final String EXTENT_PROVENANCE_RESOURCE = McpContractResources.EXTENT_PROVENANCE;
    public static final String HTML_EX_CORPUS_RESOURCE = McpContractResources.HTML_EX_CORPUS;
    public static final String[] MCP_CHECKS_RESOURCES = McpContractResources.MCP_CHECKS;
    public static final String SIZE_BUDGETS_RESOURCE = McpContractResources.SIZE_BUDGETS;
    public static final String MCP_SUITES_RESOURCE = McpContractResources.MCP_SUITES;
    public static final String CLAIM_ACTION_CONTRACTS_RESOURCE = McpContractResources.CLAIM_ACTION_CONTRACTS;
    public static final String ROOT_CAUSE_GROUPS_RESOURCE = McpContractResources.ROOT_CAUSE_GROUPS;

    private ContractCatalog() {
    }

    public static List<KpiSpec> sourceEvolutionKpis() {
        return McpKpiContractCatalog.sourceEvolutionKpis();
    }

    public static List<KpiSpec> sourceEvolutionKpisBySeverity(String severity) {
        return McpKpiContractCatalog.sourceEvolutionKpisBySeverity(severity);
    }

    public static KpiSpec requireSourceEvolutionKpi(String id) {
        return McpKpiContractCatalog.requireSourceEvolutionKpi(id);
    }

    public static List<McpTestNgProfileSpec> testNgProfiles() {
        return McpExecutionContractCatalog.testNgProfiles();
    }

    public static McpTestNgProfileSpec requireTestNgProfile(String id) {
        return McpExecutionContractCatalog.requireTestNgProfile(id);
    }

    public static List<McpAgenticExecutionStepSpec> agenticExecutionFlow() {
        return McpExecutionContractCatalog.agenticExecutionFlow();
    }

    public static McpTestNgProfileSpec requireTestNgProfile(McpExecutionProfile profile) {
        return requireTestNgProfile(profile.id());
    }

    public static List<McpRuleProfileSpec> ruleProfiles() {
        return McpExecutionContractCatalog.ruleProfiles();
    }

    public static McpRuleProfileSpec requireRuleProfile(com.ai.skills.mcp.contract.McpProfile profile) {
        return McpExecutionContractCatalog.requireRuleProfile(profile);
    }

    public static List<McpCloseoutArtifactSpec> closeoutArtifactSpecs() {
        return McpExecutionContractCatalog.closeoutArtifactSpecs();
    }

    public static McpCloseoutArtifactSpec primaryCloseoutArtifactSpec() {
        return McpExecutionContractCatalog.primaryCloseoutArtifactSpec();
    }

    public static McpCloseoutEvidence closeoutEvidence() {
        return McpCloseoutEvidence.from(primaryCloseoutArtifactSpec());
    }

    public static List<McpReportHyperlinkSpec> reportHyperlinkContracts() {
        return McpPolicyContractCatalog.reportHyperlinkContracts();
    }

    public static List<McpScoreCapSpec> scoreCapContracts() {
        return McpPolicyContractCatalog.scoreCapContracts();
    }

    public static List<McpScoreTruthSyncSpec> scoreTruthSyncContracts() {
        return McpPolicyContractCatalog.scoreTruthSyncContracts();
    }

    public static List<McpAiTaskAdvisorSpec> aiTaskAdvisorContracts() {
        return McpPolicyContractCatalog.aiTaskAdvisorContracts();
    }

    public static List<McpGovernanceMapEntry.AtlasQualityBridgeSpec> atlasQualityBridgeContracts() {
        return ContractResource.of(
                ATLAS_QUALITY_BRIDGE_RESOURCE,
                entry -> McpGovernanceMapEntry.AtlasQualityBridgeSpec.from(
                        entry,
                        ATLAS_QUALITY_BRIDGE_RESOURCE)).load();
    }

    public static List<McpProtectedRootSpec> protectedRootContracts() {
        return McpPolicyContractCatalog.protectedRootContracts();
    }

    public static List<McpSourceEvolutionLedgerSpec> sourceEvolutionLedgerContracts() {
        return McpPolicyContractCatalog.sourceEvolutionLedgerContracts();
    }

    public static List<McpPackagePolicySpec> packagePolicyContracts() {
        return McpPolicyContractCatalog.packagePolicyContracts();
    }

    public static List<String> packagePolicyTokens(String kind) {
        return McpPolicyContractCatalog.packagePolicyTokens(kind);
    }

    public static List<McpReportTemplateSpec> reportTemplateContracts() {
        return McpPolicyContractCatalog.reportTemplateContracts();
    }

    public static List<String> reportTemplateTokens(String kind) {
        return McpPolicyContractCatalog.reportTemplateTokens(kind);
    }

    public static List<McpExtentProvenanceSpec> extentProvenanceContracts() {
        return McpPolicyContractCatalog.extentProvenanceContracts();
    }

    public static List<String> extentProvenanceTokens(String kind) {
        return McpPolicyContractCatalog.extentProvenanceTokens(kind);
    }

    public static int extentProvenanceThreshold(String kind, int fallback) {
        return McpPolicyContractCatalog.extentProvenanceThreshold(kind, fallback);
    }

    public static List<McpHtmlExCorpusSpec> htmlExCorpusContracts() {
        return McpPolicyContractCatalog.htmlExCorpusContracts();
    }

    public static List<McpCheckSpec> mcpChecks() {
        return McpCheckContractCatalog.mcpChecks();
    }

    public static McpCheckSpec requireMcpCheck(String id) {
        return McpCheckContractCatalog.requireMcpCheck(id);
    }

    public static List<McpSizeBudgetSpec> sizeBudgetContracts() {
        return McpPolicyContractCatalog.sizeBudgetContracts();
    }

    public static List<McpSuiteSpec> mcpSuites() {
        return McpExecutionContractCatalog.mcpSuites();
    }

    public static McpSuiteSpec requireMcpSuite(String id) {
        return McpExecutionContractCatalog.requireMcpSuite(id);
    }

    public static List<McpClaimActionContractSpec> claimActionContracts() {
        return McpPolicyContractCatalog.claimActionContracts();
    }

    public static List<McpRootCauseGroupSpec> rootCauseGroups() {
        return McpPolicyContractCatalog.rootCauseGroups();
    }
}
