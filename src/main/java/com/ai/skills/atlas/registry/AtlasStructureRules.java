package com.ai.skills.atlas.registry;

import com.ai.skills.atlas.rules.AtlasValidationRule;
import com.ai.skills.atlas.rules.business.AtlasObservedGapsSecondaryRule;
import com.ai.skills.atlas.rules.graph.AtlasBusinessSubgraphsCollapsedRule;
import com.ai.skills.atlas.rules.structure.AtlasExecutiveContextShapeRule;
import com.ai.skills.atlas.rules.structure.AtlasReportLanguageRule;
import com.ai.skills.atlas.rules.structure.AtlasSectionNumberPlacementRule;
import com.ai.skills.atlas.rules.structure.AtlasSectionNumberingRule;
import com.ai.skills.atlas.rules.structure.AtlasSectionToolbarRule;
import com.ai.skills.atlas.rules.structure.AtlasSemanticContractMarkersRule;
import com.ai.skills.atlas.rules.structure.AtlasSharedReportShellRule;
import com.ai.skills.atlas.rules.structure.AtlasTopLevelSectionContinuityRule;

import java.util.List;

public final class AtlasStructureRules {
    private AtlasStructureRules() {
    }

    public static List<AtlasValidationRule> rules() {
        return List.of(
                new AtlasSharedReportShellRule(),
                new AtlasExecutiveContextShapeRule(),
                new AtlasSemanticContractMarkersRule(),
                new AtlasBusinessSubgraphsCollapsedRule(),
                new AtlasSectionToolbarRule(),
                new AtlasReportLanguageRule(),
                new AtlasSectionNumberingRule(),
                new AtlasSectionNumberPlacementRule(),
                new AtlasTopLevelSectionContinuityRule(),
                new AtlasObservedGapsSecondaryRule()
        );
    }
}
