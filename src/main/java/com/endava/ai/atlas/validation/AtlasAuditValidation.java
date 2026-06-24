package com.endava.ai.atlas.validation;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasGeneratedOutputValidator;
import com.endava.ai.atlas.AtlasValidationResult;
import com.endava.ai.atlas.AtlasViolation;
import com.endava.ai.atlas.rules.AtlasValidationRule;
import com.endava.ai.core.reporting.StepLogger;
import org.testng.Assert;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public final class AtlasAuditValidation {
    private final AtlasGeneratedOutputValidator validator = new AtlasGeneratedOutputValidator();

    private AtlasAuditValidation() {
    }

    public static AtlasAuditValidation generatedAtlas() {
        return new AtlasAuditValidation();
    }

    public void assertRulePasses(String ruleId) throws IOException {
        AtlasValidationResult result = validator.validateRule(ruleId);
        AtlasValidationRule rule = findRule(ruleId, validator.rules(), "base");
        logRuleResult("BASE", rule, result);
        Assert.assertTrue(result.passed(), formatFailures(ruleId, result.violations()));
    }

    public void assertCandidateRulePasses(String ruleId) throws IOException {
        AtlasValidationResult result = validator.validateCandidateRule(ruleId);
        AtlasValidationRule rule = findRule(ruleId, validator.candidateRules(), "candidate");
        logRuleResult("CANDIDATE", rule, result);
        Assert.assertTrue(result.passed(), formatFailures(ruleId, result.violations()));
    }

    public void publishCandidateRuleMatrix() throws IOException {
        StepLogger.startStep("Candidate rule matrix");
        AtlasContext context = AtlasContext.fromWorkingDirectory();
        StepLogger.startStep("Expected contract");
        StepLogger.logDetail("Expected contract");
        StepLogger.logDetail("- HTML owner: business-test-atlas-reference.html");
        StepLogger.logDetail("- MCP profile: atlas base + atlas candidate");
        StepLogger.logDetail("- Required artifacts: " + AtlasContext.ATLAS_HTML + ", " + AtlasContext.SHARED_CSS);
        StepLogger.logDetail("- Expected invariant: base rules prove the atlas is valid; candidate rules prove it is teacher-grade.");
        StepLogger.pass("Expected contract recorded");
        StepLogger.startStep("Observed validator result");
        StepLogger.logDetail("Observed validator result");
        StepLogger.logDetail("- Package: " + context.projectRoot());
        StepLogger.logDetail("- Atlas: " + context.atlasHtml());
        StepLogger.logDetail("- Source package: " + context.sourceRoot());
        StepLogger.logDetail("- Requirements: " + context.requirementsHtml());
        logMatrixSummary("BASE", validator.rules(), false);
        logMatrixSummary("CANDIDATE", validator.candidateRules(), true);
        StepLogger.pass("Observed validator result recorded");
        StepLogger.startStep("Rule matrix");
        for (AtlasValidationRule rule : validator.rules()) {
            AtlasValidationResult result = validator.validateRule(rule.id());
            logMatrixRow("BASE", rule, result);
        }
        for (AtlasValidationRule rule : validator.candidateRules()) {
            AtlasValidationResult result = validator.validateCandidateRule(rule.id());
            logMatrixRow("CANDIDATE", rule, result);
        }
        StepLogger.pass("Rule matrix recorded");
        StepLogger.startStep("Cold verdict");
        StepLogger.logDetail("Cold verdict");
        StepLogger.logDetail("- PASS here only means the matrix was published.");
        StepLogger.logDetail("- A candidate rule may fail intentionally until the generated atlas is repaired.");
        StepLogger.pass("Cold verdict recorded");
        StepLogger.pass("Candidate rule matrix published");
    }

    public void assertAllRulesPass() throws IOException {
        AtlasValidationResult result = validator.validate();
        StepLogger.startStep("Atlas full contract");
        StepLogger.logDetail("Expected contract");
        StepLogger.logDetail("- HTML owner: business-test-atlas-reference.html");
        StepLogger.logDetail("- MCP profile: atlas base contract");
        StepLogger.logDetail("- Required artifact: " + AtlasContext.ATLAS_HTML);
        StepLogger.logDetail("- Expected invariant: generated atlas is source-read-only, navigable, business-first, and proof-shaped.");
        StepLogger.logDetail("Observed validator result");
        StepLogger.logDetail("- Package: " + AtlasContext.fromWorkingDirectory().projectRoot());
        StepLogger.logDetail("- Atlas: " + AtlasContext.fromWorkingDirectory().atlasHtml());
        StepLogger.logDetail("- Blockers: " + result.violations().size());
        if (result.passed()) {
            StepLogger.logDetail("Cold verdict");
            StepLogger.logDetail("- PASS: base atlas contract is satisfied.");
            StepLogger.pass("All atlas base rules passed");
            return;
        }
        result.violations().forEach(violation -> StepLogger.logDetail(violation.toString()));
        StepLogger.logDetail("Cold verdict");
        StepLogger.logDetail("- FAIL: atlas is not acceptable as a base generated output until blockers are repaired.");
        StepLogger.logDetail("- Next repair: fix the listed blockers and rerun AtlasGeneratedOutputContractTest.");
        StepLogger.fail("Atlas base contract failed", formatFailures("atlas.full-contract", result.violations()));
        Assert.fail(formatFailures("atlas.full-contract", result.violations()));
    }

    private void logRuleResult(String profile, AtlasValidationRule rule, AtlasValidationResult result) throws IOException {
        AtlasContext context = AtlasContext.fromWorkingDirectory();
        AtlasDiagnostic.of(profile, rule, context, result).log();
        if (result.passed()) {
            StepLogger.pass("Atlas rule passed: " + rule.id());
            return;
        }
        StepLogger.fail("Atlas rule failed: " + rule.id(), formatFailures(rule.id(), result.violations()));
    }

    private void logMatrixSummary(String profile, List<AtlasValidationRule> rules, boolean candidate) throws IOException {
        int failed = 0;
        for (AtlasValidationRule rule : rules) {
            AtlasValidationResult result = candidate
                    ? validator.validateCandidateRule(rule.id())
                    : validator.validateRule(rule.id());
            if (!result.passed()) {
                failed++;
            }
        }
        StepLogger.logDetail(profile + " summary: total=" + rules.size()
                + ", pass=" + (rules.size() - failed)
                + ", fail=" + failed);
    }

    private void logMatrixRow(String profile, AtlasValidationRule rule, AtlasValidationResult result) {
        StepLogger.logDetail(profile + " | " + rule.id()
                + " | " + (result.passed() ? "PASS" : "FAIL")
                + " | blockers=" + result.violations().size()
                + " | invariant=" + rule.expected());
        result.violations().forEach(violation -> StepLogger.logDetail("  - " + violation));
    }

    private AtlasValidationRule findRule(String ruleId, List<AtlasValidationRule> rules, String profile) {
        return rules.stream()
                .filter(candidate -> candidate.id().equals(ruleId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown " + profile + " atlas rule: " + ruleId));
    }

    private String formatFailures(String ruleId, List<AtlasViolation> violations) {
        return ruleId + " blockers:" + System.lineSeparator()
                + violations.stream()
                .map(AtlasViolation::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
