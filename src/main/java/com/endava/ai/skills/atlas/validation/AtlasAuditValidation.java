package com.endava.ai.skills.atlas.validation;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasGeneratedOutputValidator;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.AtlasViolation;
import com.endava.ai.skills.atlas.contract.AtlasContractArtifacts;
import com.endava.ai.skills.atlas.contract.AtlasContractProfile;
import com.endava.ai.skills.atlas.contract.AtlasContractSuiteMetadata;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;
import com.endava.ai.core.reporting.FrameworkContractReportLabels;
import com.endava.ai.core.reporting.StepLogger;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public final class AtlasAuditValidation {
    private static final String BASE_RULE_KIND = "base";
    private static final String CANDIDATE_RULE_KIND = "candidate";

    private final AtlasGeneratedOutputValidator validator = new AtlasGeneratedOutputValidator();

    private AtlasAuditValidation() {
    }

    public static AtlasAuditValidation generatedAtlas() {
        return new AtlasAuditValidation();
    }

    public static void assertSuiteProfile(Class<?> suiteClass, AtlasContractProfile expectedProfile) {
        AtlasContractSuiteMetadata metadata = AtlasContractSuiteMetadata.require(suiteClass);
        AtlasContractAssertions.equals(metadata.profile(), expectedProfile,
                "Atlas contract suite profile must match the rule assertion helper.");
    }

    public void assertRulePasses(String ruleId) throws IOException {
        AtlasValidationResult result = validator.validateRule(ruleId);
        AtlasValidationRule rule = findRule(ruleId, validator.rules(), BASE_RULE_KIND);
        logRuleResult(AtlasContractProfile.BASE, rule, result);
        AtlasContractAssertions.isTrue(result.passed(), formatFailures(ruleId, result.violations()));
    }

    public void assertCandidateRulePasses(String ruleId) throws IOException {
        AtlasValidationResult result = validator.validateCandidateRule(ruleId);
        AtlasValidationRule rule = findRule(ruleId, validator.candidateRules(), CANDIDATE_RULE_KIND);
        logRuleResult(AtlasContractProfile.CANDIDATE, rule, result);
        AtlasContractAssertions.isTrue(result.passed(), formatFailures(ruleId, result.violations()));
    }

    public void publishCandidateRuleMatrix() throws IOException {
        AtlasCandidateRuleMatrixPublisher.publish(validator);
    }

    public void assertRuleCatalogMatchesExecutableValidatorRules() {
        AtlasRuleCatalogValidation.assertMatches(validator);
    }

    public void assertAllRulesPass() throws IOException {
        AtlasValidationResult result = validator.validate();
        AtlasContext context = AtlasContext.fromWorkingDirectory();
        StepLogger.startStep("Atlas full contract");
        StepLogger.logDetail(FrameworkContractReportLabels.EXPECTED_CONTRACT);
        StepLogger.logDetail("- HTML owner: " + AtlasContractArtifacts.HTML_OWNER);
        StepLogger.logDetail("- Atlas profile: atlas base contract");
        StepLogger.logDetail("- Required artifact: " + AtlasContractArtifacts.ATLAS_HTML);
        StepLogger.logDetail("- " + FrameworkContractReportLabels.EXPECTED_INVARIANT
                + ": generated atlas is source-read-only, navigable, business-first, and proof-shaped.");
        StepLogger.logDetail(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT);
        StepLogger.logDetail("- Package: " + context.projectRoot());
        StepLogger.logDetail("- Atlas: " + context.atlasHtml());
        StepLogger.logDetail("- Blockers: " + result.violations().size());
        if (result.passed()) {
            StepLogger.logDetail(FrameworkContractReportLabels.COLD_VERDICT);
            StepLogger.logDetail("- PASS: base atlas contract is satisfied.");
            StepLogger.pass("All atlas base rules passed");
            return;
        }
        result.violations().forEach(violation -> StepLogger.logDetail(violation.toString()));
        StepLogger.logDetail(FrameworkContractReportLabels.COLD_VERDICT);
        StepLogger.logDetail("- FAIL: atlas is not acceptable as a base generated output until blockers are repaired.");
        StepLogger.logDetail("- Next repair: fix the listed blockers and rerun AtlasGeneratedOutputContractTest.");
        StepLogger.fail("Atlas base contract failed", formatFailures("atlas.full-contract", result.violations()));
        AtlasContractAssertions.fail(formatFailures("atlas.full-contract", result.violations()));
    }

    private void logRuleResult(AtlasContractProfile profile, AtlasValidationRule rule, AtlasValidationResult result)
            throws IOException {
        AtlasContext context = AtlasContext.fromWorkingDirectory();
        AtlasDiagnostic.of(profile.name(), rule, context, result).log();
        if (result.passed()) {
            StepLogger.pass("Atlas rule passed: " + rule.id());
            return;
        }
        StepLogger.fail("Atlas rule failed: " + rule.id(), formatFailures(rule.id(), result.violations()));
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
