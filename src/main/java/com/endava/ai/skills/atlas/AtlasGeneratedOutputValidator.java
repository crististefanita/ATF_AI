package com.endava.ai.skills.atlas;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
public final class AtlasGeneratedOutputValidator {
    private final List<AtlasValidationRule> rules;
    private final List<AtlasValidationRule> candidateRules;

    public AtlasGeneratedOutputValidator() {
        this(AtlasValidationRuleRegistry.baseRules(), AtlasValidationRuleRegistry.candidateRules());
    }

    public AtlasGeneratedOutputValidator(List<AtlasValidationRule> rules) {
        this(rules, List.of());
    }

    public AtlasGeneratedOutputValidator(List<AtlasValidationRule> rules, List<AtlasValidationRule> candidateRules) {
        this.rules = List.copyOf(rules);
        this.candidateRules = List.copyOf(candidateRules);
    }

    public AtlasValidationResult validate() throws IOException {
        return validateRules(rules);
    }

    public AtlasValidationResult validateRule(String ruleId) throws IOException {
        return validateRuleFrom(ruleId, rules);
    }

    public AtlasValidationResult validateCandidate() throws IOException {
        return validateRules(candidateRules);
    }

    public AtlasValidationResult validateCandidateRule(String ruleId) throws IOException {
        return validateRuleFrom(ruleId, candidateRules);
    }

    private AtlasValidationResult validateRuleFrom(String ruleId, List<AtlasValidationRule> selectedRules) throws IOException {
        return validateRules(selectedRules.stream()
                .filter(rule -> rule.id().equals(ruleId))
                .collect(Collectors.toList()));
    }

    private AtlasValidationResult validateRules(List<AtlasValidationRule> selectedRules) throws IOException {
        AtlasContext context = AtlasContext.fromWorkingDirectory();
        AtlasValidationResult result = new AtlasValidationResult();
        selectedRules.forEach(rule -> rule.validate(context, result));
        return result;
    }

}
