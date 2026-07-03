package com.ai.skills.mcp.contracts.catalog;

import com.ai.skills.mcp.contracts.candidate.McpHtmlExCorpusSpec;
import com.ai.skills.mcp.contracts.advisor.McpAiTaskAdvisorSpec;
import com.ai.skills.mcp.contracts.policy.McpClaimActionContractSpec;
import com.ai.skills.mcp.contracts.provenance.McpExtentProvenanceSpec;
import com.ai.skills.mcp.contracts.policy.McpPackagePolicySpec;
import com.ai.skills.mcp.contracts.policy.McpProtectedRootSpec;
import com.ai.skills.mcp.contracts.policy.McpReportHyperlinkSpec;
import com.ai.skills.mcp.contracts.policy.McpReportTemplateSpec;
import com.ai.skills.mcp.contracts.policy.McpRootCauseGroupSpec;
import com.ai.skills.mcp.contracts.policy.McpScoreCapSpec;
import com.ai.skills.mcp.contracts.policy.McpScoreTruthSyncSpec;
import com.ai.skills.mcp.contracts.policy.McpSizeBudgetSpec;
import com.ai.skills.mcp.contracts.policy.McpSourceEvolutionLedgerSpec;
import com.ai.core.support.contract.yaml.ContractResource;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

final class McpPolicyContractCatalog {
    private static final ContractLazy<List<McpReportHyperlinkSpec>> REPORT_HYPERLINKS = typedPolicy(
            McpContractResources.REPORT_HYPERLINKS,
            entry -> McpReportHyperlinkSpec.from(entry, McpContractResources.REPORT_HYPERLINKS));
    private static final ContractLazy<List<McpScoreCapSpec>> SCORE_CAPS = typedPolicy(
            McpContractResources.SCORE_CAPS,
            entry -> McpScoreCapSpec.from(entry, McpContractResources.SCORE_CAPS));
    private static final ContractLazy<List<McpScoreTruthSyncSpec>> SCORE_TRUTH_SYNC = typedPolicy(
            McpContractResources.SCORE_TRUTH_SYNC,
            entry -> McpScoreTruthSyncSpec.from(entry, McpContractResources.SCORE_TRUTH_SYNC));
    private static final ContractLazy<List<McpAiTaskAdvisorSpec>> AI_TASK_ADVISOR = typedPolicy(
            McpContractResources.AI_TASK_ADVISOR,
            entry -> McpAiTaskAdvisorSpec.from(entry, McpContractResources.AI_TASK_ADVISOR));
    private static final ContractLazy<List<McpProtectedRootSpec>> PROTECTED_ROOTS = typedPolicy(
            McpContractResources.PROTECTED_ROOTS,
            entry -> McpProtectedRootSpec.from(entry, McpContractResources.PROTECTED_ROOTS));
    private static final ContractLazy<List<McpSourceEvolutionLedgerSpec>> SOURCE_EVOLUTION_LEDGER = typedPolicy(
            McpContractResources.SOURCE_EVOLUTION_LEDGER,
            entry -> McpSourceEvolutionLedgerSpec.from(entry, McpContractResources.SOURCE_EVOLUTION_LEDGER));
    private static final ContractLazy<List<McpPackagePolicySpec>> PACKAGE_POLICY = typedPolicy(
            McpContractResources.PACKAGE_POLICY,
            entry -> McpPackagePolicySpec.from(entry, McpContractResources.PACKAGE_POLICY));
    private static final ContractLazy<List<McpReportTemplateSpec>> REPORT_TEMPLATE = typedPolicy(
            McpContractResources.REPORT_TEMPLATE,
            entry -> McpReportTemplateSpec.from(entry, McpContractResources.REPORT_TEMPLATE));
    private static final ContractLazy<List<McpExtentProvenanceSpec>> EXTENT_PROVENANCE = typedPolicy(
            McpContractResources.EXTENT_PROVENANCE,
            entry -> McpExtentProvenanceSpec.from(entry, McpContractResources.EXTENT_PROVENANCE));
    private static final ContractLazy<List<McpHtmlExCorpusSpec>> HTML_EX_CORPUS = typedPolicy(
            McpContractResources.HTML_EX_CORPUS,
            entry -> McpHtmlExCorpusSpec.from(entry, McpContractResources.HTML_EX_CORPUS));
    private static final ContractLazy<List<McpSizeBudgetSpec>> SIZE_BUDGETS = typedPolicy(
            McpContractResources.SIZE_BUDGETS,
            entry -> McpSizeBudgetSpec.from(entry, McpContractResources.SIZE_BUDGETS));
    private static final ContractLazy<List<McpClaimActionContractSpec>> CLAIM_ACTION_CONTRACTS = typedPolicy(
            McpContractResources.CLAIM_ACTION_CONTRACTS,
            entry -> McpClaimActionContractSpec.from(entry, McpContractResources.CLAIM_ACTION_CONTRACTS));
    private static final ContractLazy<List<McpRootCauseGroupSpec>> ROOT_CAUSE_GROUPS = typedPolicy(
            McpContractResources.ROOT_CAUSE_GROUPS,
            entry -> McpRootCauseGroupSpec.from(entry, McpContractResources.ROOT_CAUSE_GROUPS));

    private McpPolicyContractCatalog() {
    }

    static List<McpReportHyperlinkSpec> reportHyperlinkContracts() {
        return REPORT_HYPERLINKS.get();
    }

    static List<McpScoreCapSpec> scoreCapContracts() {
        return SCORE_CAPS.get();
    }

    static List<McpScoreTruthSyncSpec> scoreTruthSyncContracts() {
        return SCORE_TRUTH_SYNC.get();
    }

    static List<McpAiTaskAdvisorSpec> aiTaskAdvisorContracts() {
        return AI_TASK_ADVISOR.get();
    }

    static List<McpProtectedRootSpec> protectedRootContracts() {
        return PROTECTED_ROOTS.get();
    }

    static List<McpSourceEvolutionLedgerSpec> sourceEvolutionLedgerContracts() {
        return SOURCE_EVOLUTION_LEDGER.get();
    }

    static List<McpPackagePolicySpec> packagePolicyContracts() {
        return PACKAGE_POLICY.get();
    }

    static List<String> packagePolicyTokens(String kind) {
        return PACKAGE_POLICY.get().stream()
                .filter(entry -> kind.equals(entry.kind()))
                .map(McpPackagePolicySpec::token)
                .filter(token -> !token.isBlank())
                .collect(Collectors.toUnmodifiableList());
    }

    static List<McpReportTemplateSpec> reportTemplateContracts() {
        return REPORT_TEMPLATE.get();
    }

    static List<String> reportTemplateTokens(String kind) {
        return REPORT_TEMPLATE.get().stream()
                .filter(entry -> kind.equals(entry.kind()))
                .map(McpReportTemplateSpec::token)
                .filter(token -> !token.isBlank())
                .collect(Collectors.toUnmodifiableList());
    }

    static List<McpExtentProvenanceSpec> extentProvenanceContracts() {
        return EXTENT_PROVENANCE.get();
    }

    static List<String> extentProvenanceTokens(String kind) {
        return EXTENT_PROVENANCE.get().stream()
                .filter(entry -> kind.equals(entry.kind()))
                .map(McpExtentProvenanceSpec::token)
                .filter(token -> !token.isBlank())
                .collect(Collectors.toUnmodifiableList());
    }

    static int extentProvenanceThreshold(String kind, int fallback) {
        return EXTENT_PROVENANCE.get().stream()
                .filter(entry -> kind.equals(entry.kind()))
                .map(McpExtentProvenanceSpec::threshold)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(fallback);
    }

    static List<McpHtmlExCorpusSpec> htmlExCorpusContracts() {
        return HTML_EX_CORPUS.get();
    }

    static List<McpSizeBudgetSpec> sizeBudgetContracts() {
        return SIZE_BUDGETS.get();
    }

    static List<McpClaimActionContractSpec> claimActionContracts() {
        return CLAIM_ACTION_CONTRACTS.get();
    }

    static List<McpRootCauseGroupSpec> rootCauseGroups() {
        return ROOT_CAUSE_GROUPS.get();
    }

    private static <T> ContractLazy<List<T>> typedPolicy(String resource,
                                                         java.util.function.Function<Map<String, String>, T> mapper) {
        return ContractLazy.of(resource, () -> ContractResource.of(resource, mapper).load());
    }
}
