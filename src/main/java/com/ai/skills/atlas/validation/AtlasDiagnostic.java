package com.ai.skills.atlas.validation;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.AtlasViolation;
import com.ai.skills.atlas.contract.AtlasContractArtifacts;
import com.ai.skills.atlas.contract.AtlasRuleCatalog;
import com.ai.skills.atlas.contract.AtlasRuleSpec;
import com.ai.skills.atlas.rules.AtlasValidationRule;
import com.ai.core.reporting.FrameworkContractReportLabels;
import com.ai.core.reporting.StepLogger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Extent-facing explanation for one atlas validation axis.
 * It mirrors the MCP diagnostic rhythm so an agent can repair the atlas
 * without reading Java assertion code first.
 */
final class AtlasDiagnostic {
    private static final int MAX_VISIBLE_BLOCKERS = 12;

    private final String profile;
    private final AtlasValidationRule rule;
    private final AtlasRuleSpec spec;
    private final AtlasRepairTarget repairTarget;
    private final AtlasContext context;
    private final AtlasValidationResult result;

    private AtlasDiagnostic(String profile,
                            AtlasValidationRule rule,
                            AtlasContext context,
                            AtlasValidationResult result) {
        this.profile = profile;
        this.rule = rule;
        this.spec = AtlasRuleCatalog.require(rule.id());
        this.repairTarget = AtlasRepairTarget.forRule(context, spec);
        this.context = context;
        this.result = result;
    }

    static AtlasDiagnostic of(String profile,
                              AtlasValidationRule rule,
                              AtlasContext context,
                              AtlasValidationResult result) {
        return new AtlasDiagnostic(profile, rule, context, result);
    }

    void log() {
        StepLogger.startStep("Atlas rule: " + rule.id());
        logExpectedContract();
        logObservedResult();
        logColdVerdict();
        logRepairHint();
    }

    private void logExpectedContract() {
        StepLogger.startStep(FrameworkContractReportLabels.EXPECTED_CONTRACT);
        StepLogger.logDetail("HTML owner: " + AtlasContractArtifacts.HTML_OWNER);
        StepLogger.logDetail("Atlas profile: " + profile.toLowerCase(Locale.ROOT));
        StepLogger.logDetail("Rule category: " + spec.category());
        StepLogger.logDetail("Severity: " + spec.severity());
        StepLogger.logDetail("Catalog artifact: " + spec.artifact());
        StepLogger.logDetail("Observed artifact: " + repairTarget.observedArtifact());
        StepLogger.logDetail("Repair target: " + repairTarget.primaryRepairArtifact());
        StepLogger.logDetail("Supporting artifact: " + repairTarget.supportingArtifact());
        StepLogger.logDetail("Rule: " + rule.id());
        StepLogger.logDetail("Why this rule exists: " + spec.why());
        logDetail(FrameworkContractReportLabels.EXPECTED_INVARIANT, rule.expected());
        StepLogger.pass(FrameworkContractReportLabels.EXPECTED_CONTRACT_RECORDED);
    }

    private void logObservedResult() {
        StepLogger.startStep(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT);
        StepLogger.logDetail("Package: " + context.projectRoot());
        StepLogger.logDetail("Atlas: " + context.atlasHtml());
        StepLogger.logDetail("Source package: " + context.sourceRoot());
        StepLogger.logDetail("Observed artifact: " + repairTarget.observedArtifact());
        StepLogger.logDetail("Status: " + (result.passed() ? "PASS" : "FAIL"));
        StepLogger.logDetail("Blockers: " + result.violations().size());
        if (result.passed()) {
            StepLogger.logDetail("Observed blockers: none");
            StepLogger.pass(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT_RECORDED);
            return;
        }
        summarizeBlockers(result.violations());
        result.violations().stream()
                .limit(MAX_VISIBLE_BLOCKERS)
                .forEach(violation -> StepLogger.logDetail(violation.toString()));
        if (result.violations().size() > MAX_VISIBLE_BLOCKERS) {
            StepLogger.logDetail("Additional blockers hidden from inline list: "
                    + (result.violations().size() - MAX_VISIBLE_BLOCKERS));
        }
        StepLogger.logCodeBlock(result.violations().stream()
                .map(AtlasViolation::toString)
                .collect(Collectors.joining(System.lineSeparator())));
        StepLogger.pass(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT_RECORDED);
    }

    private void summarizeBlockers(List<AtlasViolation> violations) {
        violations.stream()
                .collect(Collectors.groupingBy(
                        AtlasViolation::code,
                        LinkedHashMap::new,
                        Collectors.counting()))
                .forEach((code, count) -> StepLogger.logDetail("Blocker group: " + code + " = " + count));
    }

    private void logColdVerdict() {
        StepLogger.startStep(FrameworkContractReportLabels.COLD_VERDICT);
        logDetail(FrameworkContractReportLabels.PASS_FAIL, result.passed() ? "PASS" : "FAIL");
        logDetail(FrameworkContractReportLabels.WHY, coldReason());
        StepLogger.logDetail("Repair contract: " + spec.repair());
        StepLogger.logDetail("Repair target: " + repairTarget.primaryRepairArtifact());
        StepLogger.pass(FrameworkContractReportLabels.COLD_VERDICT_RECORDED);
    }

    private String coldReason() {
        return result.passed()
                ? "no blocker violations for " + rule.id() + "."
                : result.violations().size() + " blocker(s) violate " + rule.id() + ".";
    }

    private void logRepairHint() {
        StepLogger.startStep(FrameworkContractReportLabels.REPAIR_HINT);
        if (result.passed()) {
            StepLogger.logDetail("No repair required for this rule; rerun atlas tests after any atlas HTML/CSS edit.");
            StepLogger.logDetail("Verification artifact: " + repairTarget.observedArtifact());
            StepLogger.pass(FrameworkContractReportLabels.REPAIR_HINT_RECORDED);
            return;
        }
        StepLogger.logDetail("Repair target: " + repairTarget.primaryRepairArtifact());
        StepLogger.logDetail("Supporting artifact: " + repairTarget.supportingArtifact());
        StepLogger.logDetail("Repair route: " + repairTarget.route());
        result.violations().stream()
                .map(AtlasViolation::repairHint)
                .distinct()
                .forEach(StepLogger::logDetail);
        StepLogger.logDetail("Catalog repair: " + spec.repair());
        StepLogger.pass(FrameworkContractReportLabels.REPAIR_HINT_RECORDED);
    }

    private static void logDetail(String label, Object value) {
        StepLogger.logDetail(label + ": " + value);
    }
}
