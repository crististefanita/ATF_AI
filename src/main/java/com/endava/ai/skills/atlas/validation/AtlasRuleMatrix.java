package com.endava.ai.skills.atlas.validation;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasGeneratedOutputValidator;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.contract.AtlasContractProfile;
import com.endava.ai.skills.atlas.contract.AtlasRuleCatalog;
import com.endava.ai.skills.atlas.contract.AtlasRuleSpec;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;
import com.endava.ai.core.reporting.StepLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Agent-facing matrix of Atlas contract rules.
 * It is intentionally generated from executable rules, not maintained by hand.
 */
final class AtlasRuleMatrix {
    private final AtlasContext context;
    private final List<AtlasRuleMatrixRow> rows;

    private AtlasRuleMatrix(AtlasContext context, List<AtlasRuleMatrixRow> rows) {
        this.context = context;
        this.rows = List.copyOf(rows);
    }

    static AtlasRuleMatrix build(AtlasContext context, AtlasGeneratedOutputValidator validator) throws IOException {
        List<AtlasRuleMatrixRow> rows = new ArrayList<>();
        rows.addAll(matrixRows(context, AtlasContractProfile.BASE, validator.rules(), false, validator));
        rows.addAll(matrixRows(context, AtlasContractProfile.CANDIDATE, validator.candidateRules(), true, validator));
        return new AtlasRuleMatrix(context, rows);
    }

    void logSummary() {
        logProfileSummary(AtlasContractProfile.BASE);
        logProfileSummary(AtlasContractProfile.CANDIDATE);
    }

    void logRows() {
        rows.forEach(row -> {
            StepLogger.logDetail(row.profile()
                    + " | " + row.spec().category()
                    + " | " + row.spec().severity()
                    + " | " + row.spec().id()
                    + " | " + row.status()
                    + " | blockers=" + row.result().violations().size()
                    + " | observed=" + row.repairTarget().observedArtifact()
                    + " | repairTarget=" + row.repairTarget().primaryRepairArtifact()
                    + " | repair=" + row.spec().repair());
            row.result().violations().forEach(violation -> StepLogger.logDetail("  - " + violation));
        });
    }

    void writeArtifacts() throws IOException {
        Path targetRoot = context.projectRoot().resolve("target");
        Files.createDirectories(targetRoot);
        Path markdown = targetRoot.resolve("atlas-rule-matrix.md");
        Path html = targetRoot.resolve("atlas-rule-matrix.html");

        Files.writeString(markdown, AtlasRuleMatrixRenderer.toMarkdown(rows), StandardCharsets.UTF_8);
        Files.writeString(html, AtlasRuleMatrixRenderer.toHtml(rows), StandardCharsets.UTF_8);

        StepLogger.logDetail("Rule matrix artifact MD: " + markdown);
        StepLogger.logDetail("Rule matrix artifact HTML: " + html);
    }

    private void logProfileSummary(AtlasContractProfile profile) {
        long total = rows.stream()
                .filter(row -> row.profile() == profile)
                .count();
        long failed = rows.stream()
                .filter(row -> row.profile() == profile)
                .filter(row -> !row.result().passed())
                .count();
        StepLogger.logDetail(profile + " summary: total=" + total
                + ", pass=" + (total - failed)
                + ", fail=" + failed);
    }

    private static List<AtlasRuleMatrixRow> matrixRows(AtlasContext context,
                                                       AtlasContractProfile profile,
                                                       List<AtlasValidationRule> rules,
                                                       boolean candidate,
                                                       AtlasGeneratedOutputValidator validator) throws IOException {
        List<AtlasRuleMatrixRow> rows = new ArrayList<>();
        for (AtlasValidationRule rule : rules) {
            AtlasValidationResult result = candidate
                    ? validator.validateCandidateRule(rule.id())
                    : validator.validateRule(rule.id());
            AtlasRuleSpec spec = AtlasRuleCatalog.require(rule.id());
            rows.add(new AtlasRuleMatrixRow(profile, spec, result, AtlasRepairTarget.forRule(context, spec)));
        }
        return rows;
    }
}
