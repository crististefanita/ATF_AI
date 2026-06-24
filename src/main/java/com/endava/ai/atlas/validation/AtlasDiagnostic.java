package com.endava.ai.atlas.validation;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;
import com.endava.ai.atlas.AtlasViolation;
import com.endava.ai.atlas.rules.AtlasValidationRule;
import com.endava.ai.core.reporting.StepLogger;

import java.util.List;
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
    private final AtlasContext context;
    private final AtlasValidationResult result;

    private AtlasDiagnostic(String profile,
                            AtlasValidationRule rule,
                            AtlasContext context,
                            AtlasValidationResult result) {
        this.profile = profile;
        this.rule = rule;
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
        StepLogger.startStep("Expected contract");
        StepLogger.logDetail("HTML owner: business-test-atlas-reference.html");
        StepLogger.logDetail("Atlas profile: " + profile.toLowerCase(java.util.Locale.ROOT));
        StepLogger.logDetail("Required artifacts: " + AtlasContext.ATLAS_HTML);
        StepLogger.logDetail("Rule: " + rule.id());
        StepLogger.logDetail("Expected invariant: " + rule.expected());
        StepLogger.pass("Expected contract recorded");
    }

    private void logObservedResult() {
        StepLogger.startStep("Observed validator result");
        StepLogger.logDetail("Package: " + context.projectRoot());
        StepLogger.logDetail("Atlas: " + context.atlasHtml());
        StepLogger.logDetail("Source package: " + context.sourceRoot());
        StepLogger.logDetail("Status: " + (result.passed() ? "PASS" : "FAIL"));
        StepLogger.logDetail("Blockers: " + result.violations().size());
        if (result.passed()) {
            StepLogger.logDetail("Observed blockers: none");
            StepLogger.pass("Observed validator result recorded");
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
        StepLogger.pass("Observed validator result recorded");
    }

    private void summarizeBlockers(List<AtlasViolation> violations) {
        violations.stream()
                .collect(Collectors.groupingBy(
                        AtlasViolation::code,
                        java.util.LinkedHashMap::new,
                        Collectors.counting()))
                .forEach((code, count) -> StepLogger.logDetail("Blocker group: " + code + " = " + count));
    }

    private void logColdVerdict() {
        StepLogger.startStep("Cold verdict");
        StepLogger.logDetail("PASS / FAIL: " + (result.passed() ? "PASS" : "FAIL"));
        StepLogger.logDetail("Why: " + coldReason());
        StepLogger.pass("Cold verdict recorded");
    }

    private String coldReason() {
        return result.passed()
                ? "no blocker violations for " + rule.id() + "."
                : result.violations().size() + " blocker(s) violate " + rule.id() + ".";
    }

    private void logRepairHint() {
        StepLogger.startStep("Repair hint");
        if (result.passed()) {
            StepLogger.logDetail("No repair required for this rule; rerun atlas tests after any atlas HTML/CSS edit.");
            StepLogger.pass("Repair hint recorded");
            return;
        }
        result.violations().stream()
                .map(AtlasViolation::repairHint)
                .distinct()
                .forEach(StepLogger::logDetail);
        StepLogger.pass("Repair hint recorded");
    }
}
