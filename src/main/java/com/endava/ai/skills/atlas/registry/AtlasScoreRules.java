package com.endava.ai.skills.atlas.registry;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;
import com.endava.ai.skills.atlas.rules.scoring.AtlasScoreAxisConsistencyRule;
import com.endava.ai.skills.atlas.rules.scoring.AtlasScoreEvidenceTraceRule;
import com.endava.ai.skills.atlas.rules.scoring.AtlasScoreFormulaTraceRule;
import com.endava.ai.skills.atlas.rules.scoring.AtlasScoreGqmTraceabilityRule;
import com.endava.ai.skills.atlas.rules.scoring.AtlasScoreOpeningAuditPointerRule;
import com.endava.ai.skills.atlas.rules.scoring.AtlasScoreOverallCalculationRule;
import com.endava.ai.skills.atlas.rules.scoring.AtlasScoreRationaleAuditRule;
import com.endava.ai.skills.atlas.rules.scoring.AtlasScoreSupportTraceRule;
import com.endava.ai.skills.atlas.rules.scoring.AtlasScoreUniformityCapRule;
import com.endava.ai.skills.atlas.rules.scoring.cards.AtlasScoreCardReadabilityRule;
import com.endava.ai.skills.atlas.rules.scoring.cards.AtlasScoreCardsDocumentedRule;
import com.endava.ai.skills.atlas.rules.scoring.cards.AtlasScoreCardsShapeRule;
import com.endava.ai.skills.atlas.rules.scoring.limits.AtlasEndToEndLimitScoreCapRule;
import com.endava.ai.skills.atlas.rules.scoring.limits.AtlasExcellentOverallLimitDisciplineRule;
import com.endava.ai.skills.atlas.rules.scoring.limits.AtlasSuiteCompletenessSeparationRule;

import java.util.List;

public final class AtlasScoreRules {
    private AtlasScoreRules() {
    }

    public static List<AtlasValidationRule> rules() {
        return List.of(
                new AtlasScoreCardsShapeRule(),
                new AtlasScoreAxisConsistencyRule(),
                new AtlasScoreCardReadabilityRule(),
                new AtlasScoreOpeningAuditPointerRule(),
                new AtlasScoreRationaleAuditRule(),
                new AtlasScoreSupportTraceRule(),
                new AtlasScoreFormulaTraceRule(),
                new AtlasScoreGqmTraceabilityRule(),
                new AtlasScoreUniformityCapRule(),
                new AtlasEndToEndLimitScoreCapRule(),
                new AtlasExcellentOverallLimitDisciplineRule(),
                new AtlasSuiteCompletenessSeparationRule(),
                new AtlasScoreOverallCalculationRule(),
                new AtlasScoreCardsDocumentedRule(),
                new AtlasScoreEvidenceTraceRule()
        );
    }
}
