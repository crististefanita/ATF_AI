package com.endava.ai.skills.atlas.registry;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;
import com.endava.ai.skills.atlas.rules.graph.AtlasBusinessSubgraphsWidthRule;
import com.endava.ai.skills.atlas.rules.graph.AtlasGraphMinimalStylePerSectionRule;
import com.endava.ai.skills.atlas.rules.graph.AtlasGraphNodeBusinessProofStripRule;
import com.endava.ai.skills.atlas.rules.graph.AtlasLocalProofStripSourceBackedRule;
import com.endava.ai.skills.atlas.rules.graph.AtlasReferenceDiagramPrimitiveRule;
import com.endava.ai.skills.atlas.rules.graph.AtlasReusableGraphCssRule;
import com.endava.ai.skills.atlas.rules.graph.AtlasSubgraphBusinessUnderstandingStripRule;
import com.endava.ai.skills.atlas.rules.graph.AtlasSubgraphSourceSpecificDominanceRule;
import com.endava.ai.skills.atlas.rules.graph.legend.AtlasBusinessGraphLegendRule;
import com.endava.ai.skills.atlas.rules.graph.legend.AtlasLegendCollapseRule;
import com.endava.ai.skills.atlas.rules.graph.legend.AtlasLegendContentQualityRule;
import com.endava.ai.skills.atlas.rules.graph.legend.AtlasSupportingGraphsLegendRule;
import com.endava.ai.skills.atlas.rules.graph.proof.AtlasGraphProofStripPlacementRule;
import com.endava.ai.skills.atlas.rules.graph.proof.AtlasMainGraphNodeMaturityProofRule;
import com.endava.ai.skills.atlas.rules.graph.proof.AtlasSubgraphSemanticDensityRule;
import com.endava.ai.skills.atlas.rules.graph.style.AtlasGraphVisualScaleRule;

import java.util.List;

public final class AtlasGraphRules {
    private AtlasGraphRules() {
    }

    public static List<AtlasValidationRule> rules() {
        return List.of(
                new AtlasBusinessGraphLegendRule(),
                new AtlasSupportingGraphsLegendRule(),
                new AtlasReferenceDiagramPrimitiveRule(),
                new AtlasReusableGraphCssRule(),
                new AtlasGraphMinimalStylePerSectionRule(),
                new AtlasGraphVisualScaleRule(),
                new AtlasBusinessSubgraphsWidthRule(),
                new AtlasLegendCollapseRule(),
                new AtlasLegendContentQualityRule(),
                new AtlasGraphNodeBusinessProofStripRule(),
                new AtlasMainGraphNodeMaturityProofRule(),
                new AtlasGraphProofStripPlacementRule(),
                new AtlasSubgraphBusinessUnderstandingStripRule(),
                new AtlasSubgraphSemanticDensityRule(),
                new AtlasSubgraphSourceSpecificDominanceRule(),
                new AtlasLocalProofStripSourceBackedRule()
        );
    }
}
