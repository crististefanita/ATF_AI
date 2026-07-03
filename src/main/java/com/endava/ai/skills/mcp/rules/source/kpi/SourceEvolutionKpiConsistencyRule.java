package com.endava.ai.skills.mcp.rules.source.kpi;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import com.endava.ai.skills.mcp.rules.source.inventory.SourceEvolutionInventory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@McpContract(id = "source.evolution.kpi-consistency",
        category = McpRuleCategory.SOURCE,
        htmlReq = "orchestrate-iterative-runs.html + execute-and-understand-run.html + reporting.html",
        artifact = "review/source-evolution-ledger.md")
public final class SourceEvolutionKpiConsistencyRule implements McpValidationRule {

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path ledger = context.packageRoot().resolve(McpArtifactPaths.SOURCE_EVOLUTION_LEDGER);
        Path rootDiff = context.packageRoot().resolve(McpArtifactPaths.ROOT_DIFF);
        if (!Files.isRegularFile(ledger) || !Files.isRegularFile(rootDiff)) {
            return;
        }

        String ledgerText = context.files().read(ledger, result);
        String rootDiffText = context.files().read(rootDiff, result);
        SourceEvolutionInventory inventory = SourceEvolutionInventory.load(context, rootDiffText, result);
        Map<String, Integer> declaredValues = SourceEvolutionKpiReader.declaredValues(ledgerText, inventory);
        SourceEvolutionKpiMatrix.write(context, inventory, declaredValues);

        SourceEvolutionKpiArtifactChecks.requireGeneratedKpiArtifacts(context, result);
        SourceEvolutionLedgerKpiChecks.requireLedgerValues(ledgerText, ledger, inventory, result);
        SourceEvolutionScorePolicyChecks.requireScoreCapFromKpiMatrix(context, ledgerText, ledger, inventory, result);
        SourceEvolutionReportKpiChecks.requireFinalReportKpiSummary(context, ledgerText, inventory, result);
    }
}
