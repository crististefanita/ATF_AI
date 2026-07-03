package com.endava.ai.skills.atlas;

import com.endava.ai.skills.atlas.registry.AtlasBaseRules;
import com.endava.ai.skills.atlas.registry.AtlasBusinessRules;
import com.endava.ai.skills.atlas.registry.AtlasGraphRules;
import com.endava.ai.skills.atlas.registry.AtlasScoreRules;
import com.endava.ai.skills.atlas.registry.AtlasSkillAuditRules;
import com.endava.ai.skills.atlas.registry.AtlasStructureRules;
import com.endava.ai.skills.atlas.registry.AtlasTraceRules;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical executable rule registry for atlas validation.
 * <p>
 * The validator owns orchestration; this registry owns rule composition. Keeping
 * the long rule inventory here makes the public validator read like a workflow
 * instead of a constructor stuffed with implementation details.
 */
final class AtlasValidationRuleRegistry {
    private AtlasValidationRuleRegistry() {
    }

    static List<AtlasValidationRule> baseRules() {
        return AtlasBaseRules.rules();
    }

    static List<AtlasValidationRule> candidateRules() {
        List<AtlasValidationRule> rules = new ArrayList<>();
        rules.addAll(AtlasScoreRules.rules());
        rules.addAll(AtlasStructureRules.rules());
        rules.addAll(AtlasGraphRules.rules());
        rules.addAll(AtlasBusinessRules.rules());
        rules.addAll(AtlasTraceRules.rules());
        rules.addAll(AtlasSkillAuditRules.rules());
        return List.copyOf(rules);
    }
}
