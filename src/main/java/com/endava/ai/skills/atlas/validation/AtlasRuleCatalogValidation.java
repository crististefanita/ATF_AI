package com.endava.ai.skills.atlas.validation;

import com.endava.ai.skills.atlas.AtlasGeneratedOutputValidator;
import com.endava.ai.skills.atlas.contract.AtlasRuleCatalog;
import com.endava.ai.skills.atlas.contract.AtlasRuleSpec;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;
import com.endava.ai.core.reporting.FrameworkContractReportLabels;
import com.endava.ai.core.reporting.StepLogger;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

final class AtlasRuleCatalogValidation {
    private AtlasRuleCatalogValidation() {
    }

    static void assertMatches(AtlasGeneratedOutputValidator validator) {
        Set<String> executableRuleIds = executableRuleIds(validator);
        Set<String> catalogRuleIds = catalogRuleIds();
        logExpectedContract();
        logObservedResult(executableRuleIds, catalogRuleIds);
        AtlasContractAssertions.equals(catalogRuleIds, executableRuleIds,
                "AtlasRuleCatalog must stay synchronized with executable Atlas validator rules.");
        AtlasRuleCatalog.allRules().forEach(AtlasRuleCatalogValidation::assertCompleteSpec);
        logColdVerdict();
    }

    private static Set<String> executableRuleIds(AtlasGeneratedOutputValidator validator) {
        Set<String> executableRuleIds = new LinkedHashSet<>();
        validator.rules().stream()
                .map(AtlasValidationRule::id)
                .forEach(executableRuleIds::add);
        validator.candidateRules().stream()
                .map(AtlasValidationRule::id)
                .forEach(executableRuleIds::add);
        return executableRuleIds;
    }

    private static Set<String> catalogRuleIds() {
        return AtlasRuleCatalog.allRules().stream()
                .map(AtlasRuleSpec::id)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static void logExpectedContract() {
        StepLogger.startStep(FrameworkContractReportLabels.EXPECTED_CONTRACT);
        StepLogger.logDetail("Every executable Atlas rule has catalog metadata.");
        StepLogger.logDetail("Every catalog rule is backed by an executable validator rule.");
        StepLogger.logDetail("Catalog metadata includes profile, category, severity, why, and repair.");
        StepLogger.pass(FrameworkContractReportLabels.EXPECTED_CONTRACT_RECORDED);
    }

    private static void logObservedResult(Set<String> executableRuleIds, Set<String> catalogRuleIds) {
        StepLogger.startStep(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT);
        StepLogger.logDetail("Executable rules: " + executableRuleIds.size());
        StepLogger.logDetail("Catalog rules: " + catalogRuleIds.size());
        StepLogger.logDetail("Missing from catalog: " + difference(executableRuleIds, catalogRuleIds));
        StepLogger.logDetail("Catalog without executable rule: " + difference(catalogRuleIds, executableRuleIds));
        StepLogger.pass(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT_RECORDED);
    }

    private static void assertCompleteSpec(AtlasRuleSpec spec) {
        AtlasContractAssertions.isFalse(spec.why().isBlank(), "Rule why must be documented: " + spec.id());
        AtlasContractAssertions.isFalse(spec.repair().isBlank(), "Rule repair must be documented: " + spec.id());
        AtlasContractAssertions.isFalse(spec.owner().isBlank(), "Rule owner must be documented: " + spec.id());
        AtlasContractAssertions.isFalse(spec.artifact().isBlank(), "Rule artifact must be documented: " + spec.id());
    }

    private static void logColdVerdict() {
        StepLogger.startStep(FrameworkContractReportLabels.COLD_VERDICT);
        StepLogger.logDetail("PASS: rule catalog is synchronized with executable Atlas validation.");
        StepLogger.pass(FrameworkContractReportLabels.COLD_VERDICT_RECORDED);
    }

    private static Set<String> difference(Set<String> left, Set<String> right) {
        return left.stream()
                .filter(item -> !right.contains(item))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
