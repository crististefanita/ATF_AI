package com.endava.ai.skills.atlas.contract.catalog;

import com.endava.ai.skills.atlas.contract.AtlasContractProfile;
import com.endava.ai.skills.atlas.contract.AtlasRuleCategory;
import com.endava.ai.skills.atlas.contract.AtlasRuleSeverity;
import com.endava.ai.skills.atlas.contract.AtlasRuleSpec;

final class AtlasRuleCatalogSupport {
    static final String OUTPUT_REPAIR =
            "Regenerate the declared atlas output from the explicit source package without changing analyzed source files.";
    static final String GRAPH_REPAIR =
            "Keep graph nodes business-readable and attach source-backed proof strips instead of decorative labels.";
    static final String SCORE_REPAIR =
            "Tie each score to goal, question, source/layer evidence, formula, cap, and visible rationale.";
    static final String TRACE_REPAIR =
            "Replace invented or generic trace rows with real test methods and concrete layer paths from analyzed source.";

    private AtlasRuleCatalogSupport() {
    }

    static AtlasRuleSpec base(String id,
                              AtlasRuleCategory category,
                              AtlasRuleSeverity severity,
                              String why,
                              String repair) {
        return AtlasRuleSpec.of(id, AtlasContractProfile.BASE, category, severity, why, repair);
    }

    static AtlasRuleSpec candidate(String id,
                                   AtlasRuleCategory category,
                                   AtlasRuleSeverity severity,
                                   String why,
                                   String repair) {
        return AtlasRuleSpec.of(id, AtlasContractProfile.CANDIDATE, category, severity, why, repair);
    }
}
