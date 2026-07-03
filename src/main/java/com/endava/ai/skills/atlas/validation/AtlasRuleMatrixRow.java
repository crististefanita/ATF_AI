package com.endava.ai.skills.atlas.validation;

import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.AtlasViolation;
import com.endava.ai.skills.atlas.contract.AtlasContractProfile;
import com.endava.ai.skills.atlas.contract.AtlasRuleSpec;
import com.endava.ai.skills.atlas.contract.yaml.AtlasYamlRulePolicy;
import com.endava.ai.skills.atlas.contract.yaml.AtlasYamlRulePolicyCatalog;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter(AccessLevel.PACKAGE)
@Accessors(fluent = true)
final class AtlasRuleMatrixRow {
    private final AtlasContractProfile profile;
    private final AtlasRuleSpec spec;
    private final AtlasValidationResult result;
    private final AtlasRepairTarget repairTarget;
    private final AtlasYamlRulePolicy policy;

    AtlasRuleMatrixRow(AtlasContractProfile profile,
                       AtlasRuleSpec spec,
                       AtlasValidationResult result,
                       AtlasRepairTarget repairTarget) {
        this.profile = profile;
        this.spec = spec;
        this.result = result;
        this.repairTarget = repairTarget;
        this.policy = AtlasYamlRulePolicyCatalog.requireFor(spec.category(), spec.profile());
    }

    String status() {
        return result.passed() ? "PASS" : "FAIL";
    }

    String firstBlocker() {
        return result.violations().stream()
                .findFirst()
                .map(AtlasViolation::toString)
                .orElse("none");
    }
}
