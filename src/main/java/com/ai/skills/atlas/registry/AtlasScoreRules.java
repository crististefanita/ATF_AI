package com.ai.skills.atlas.registry;

import com.ai.skills.atlas.rules.AtlasValidationRule;
import com.ai.skills.atlas.rules.scoring.AtlasScoreAxisConsistencyRule;
import com.ai.skills.atlas.rules.scoring.AtlasScoreEvidenceTraceRule;
import com.ai.skills.atlas.rules.scoring.AtlasScoreFormulaTraceRule;
import com.ai.skills.atlas.rules.scoring.AtlasScoreGqmTraceabilityRule;
import com.ai.skills.atlas.rules.scoring.AtlasScoreOpeningAuditPointerRule;
import com.ai.skills.atlas.rules.scoring.AtlasScoreOverallCalculationRule;
import com.ai.skills.atlas.rules.scoring.AtlasScoreRationaleAuditRule;
import com.ai.skills.atlas.rules.scoring.AtlasScoreSupportTraceRule;
import com.ai.skills.atlas.rules.scoring.AtlasScoreUniformityCapRule;
import com.ai.skills.atlas.rules.scoring.cards.AtlasScoreCardReadabilityRule;
import com.ai.skills.atlas.rules.scoring.cards.AtlasScoreCardsDocumentedRule;
import com.ai.skills.atlas.rules.scoring.cards.AtlasScoreCardsShapeRule;
import com.ai.skills.atlas.rules.scoring.limits.AtlasEndToEndLimitScoreCapRule;
import com.ai.skills.atlas.rules.scoring.limits.AtlasExcellentOverallLimitDisciplineRule;
import com.ai.skills.atlas.rules.scoring.limits.AtlasSuiteCompletenessSeparationRule;

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
