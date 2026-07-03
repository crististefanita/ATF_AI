package com.endava.ai.skills.atlas.registry;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;
import com.endava.ai.skills.atlas.rules.business.AtlasBusinessObjectCenteredRule;
import com.endava.ai.skills.atlas.rules.business.AtlasBusinessOnlyFlowRule;
import com.endava.ai.skills.atlas.rules.business.AtlasComprehensionVocabularyRule;
import com.endava.ai.skills.atlas.rules.business.AtlasEndToEndClosureLadderRule;
import com.endava.ai.skills.atlas.rules.business.AtlasFlowMaturityDistributionRule;
import com.endava.ai.skills.atlas.rules.business.AtlasFlowStoryCompletenessRule;
import com.endava.ai.skills.atlas.rules.business.AtlasObjectMaturityMatrixRule;
import com.endava.ai.skills.atlas.rules.business.AtlasPartialFlowOwnershipConsistencyRule;
import com.endava.ai.skills.atlas.rules.business.AtlasSubgraphBusinessComplexityRule;

import java.util.List;

public final class AtlasBusinessRules {
    private AtlasBusinessRules() {
    }

    public static List<AtlasValidationRule> rules() {
        return List.of(
                new AtlasBusinessOnlyFlowRule(),
                new AtlasFlowStoryCompletenessRule(),
                new AtlasSubgraphBusinessComplexityRule(),
                new AtlasBusinessObjectCenteredRule(),
                new AtlasObjectMaturityMatrixRule(),
                new AtlasEndToEndClosureLadderRule(),
                new AtlasFlowMaturityDistributionRule(),
                new AtlasPartialFlowOwnershipConsistencyRule(),
                new AtlasComprehensionVocabularyRule()
        );
    }
}
