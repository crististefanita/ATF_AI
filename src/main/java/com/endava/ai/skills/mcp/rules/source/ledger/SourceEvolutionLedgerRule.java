package com.endava.ai.skills.mcp.rules.source.ledger;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.context.McpRunArtifacts;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.contracts.policy.McpSourceEvolutionLedgerSpec;
import com.endava.ai.skills.mcp.fs.DiffFileExtractor;
import com.endava.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@McpContract(id = "source.evolution.ledger",
        category = McpRuleCategory.SOURCE,
        htmlReq = "orchestrate-iterative-runs.html + execute-and-understand-run.html + reporting.html",
        artifact = "review/source-evolution-ledger.md")
public final class SourceEvolutionLedgerRule implements McpValidationRule {
    private final DiffFileExtractor diffFileExtractor = new DiffFileExtractor();

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path ledger = context.packageRoot().resolve(McpArtifactPaths.SOURCE_EVOLUTION_LEDGER);
        if (!Files.isRegularFile(ledger)) {
            result.add(McpPackageViolation.blocker("source-evolution.ledger.missing",
                    "Missing source evolution ledger: " + McpArtifactPaths.SOURCE_EVOLUTION_LEDGER,
                    ledger));
            return;
        }

        String content = context.files().read(ledger, result);
        String normalized = content.toLowerCase(Locale.ROOT).replace('\\', '/');
        requireYamlTokens(normalized, "required-section", "source-evolution.ledger.section-missing",
                "Source evolution ledger must publish the YAML-declared close-out sections", ledger, result);
        requireYamlTokens(normalized, "required-kpi-label", "source-evolution.ledger.kpi-missing",
                "Source evolution ledger must publish the YAML-declared progress KPI labels", ledger, result);
        requireToken(normalized, McpArtifactPaths.ROOT_DIFF, "source-evolution.ledger.root-diff-link-missing",
                "Source evolution ledger must link the compiled root diff", ledger, result);
        requireToken(normalized, "git status", "source-evolution.ledger.git-status-missing",
                "Source evolution ledger must state the baseline/final git status boundary", ledger, result);

        for (Path runDirectory : context.files().runDirectories(context.paths().runsRoot(), result)) {
            String run = runDirectory.getFileName().toString();
            requireToken(normalized, run + "_delta", "source-evolution.ledger.run-section-missing",
                    "Source evolution ledger must publish a delta row/section for " + run, ledger, result);
            requireToken(normalized, "runs/" + run + "/" + McpRunArtifacts.DIFF,
                    "source-evolution.ledger.run-diff-link-missing",
                    "Source evolution ledger must link " + run + " diff", ledger, result);
            requireToken(normalized, "runs/" + run + "/" + McpRunArtifacts.STATE,
                    "source-evolution.ledger.run-state-link-missing",
                    "Source evolution ledger must link " + run + " state", ledger, result);
            requireOneRunSourcePath(context, runDirectory, normalized, ledger, result);
        }
    }

    private void requireYamlTokens(String normalized,
                                   String kind,
                                   String code,
                                   String message,
                                   Path ledger,
                                   McpPackageValidationResult result) {
        for (McpSourceEvolutionLedgerSpec contract : ContractCatalog.sourceEvolutionLedgerContracts()) {
            if (!kind.equals(contract.kind())) {
                continue;
            }
            String token = contract.token();
            String reason = contract.reason();
            requireToken(normalized, token, code, message + ": " + token + " (" + reason + ")", ledger, result);
        }
    }

    private void requireOneRunSourcePath(McpPackageContext context,
                                         Path runDirectory,
                                         String normalizedLedger,
                                         Path ledger,
                                         McpPackageValidationResult result) {
        Path diff = runDirectory.resolve(McpRunArtifacts.DIFF);
        if (!Files.isRegularFile(diff)) {
            return;
        }
        Set<String> runSources = diffFileExtractor.referencedFiles(context.files().read(diff, result)).stream()
                .map(path -> path.replace('\\', '/').toLowerCase(Locale.ROOT))
                .filter(path -> path.startsWith("src/"))
                .collect(Collectors.toSet());
        if (!runSources.isEmpty() && runSources.stream().noneMatch(normalizedLedger::contains)) {
            result.add(McpPackageViolation.blocker("source-evolution.ledger.run-source-path-missing",
                    "Source evolution ledger must quote at least one source path from "
                            + runDirectory.getFileName() + " diff",
                    ledger));
        }
    }

    private void requireToken(String normalized,
                              String token,
                              String code,
                              String message,
                              Path ledger,
                              McpPackageValidationResult result) {
        if (!normalized.contains(token.toLowerCase(Locale.ROOT).replace('\\', '/'))) {
            result.add(McpPackageViolation.blocker(code, message, ledger));
        }
    }

}
