package com.endava.ai.skills.atlas.registry;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;
import com.endava.ai.skills.atlas.rules.business.trace.AtlasLayerTraceConcreteEvidenceRule;
import com.endava.ai.skills.atlas.rules.business.trace.AtlasLayerTraceMapRule;
import com.endava.ai.skills.atlas.rules.business.trace.AtlasMappingDepthRule;
import com.endava.ai.skills.atlas.rules.business.trace.AtlasSourceTraceApplicationBusinessRule;
import com.endava.ai.skills.atlas.rules.business.trace.AtlasSourceTraceBackedBySourceRule;
import com.endava.ai.skills.atlas.rules.business.trace.AtlasSourceTraceMethodLevelRule;

import java.util.List;

public final class AtlasTraceRules {
    private AtlasTraceRules() {
    }

    public static List<AtlasValidationRule> rules() {
        return List.of(
                new AtlasMappingDepthRule(),
                new AtlasLayerTraceMapRule(),
                new AtlasLayerTraceConcreteEvidenceRule(),
                new AtlasSourceTraceMethodLevelRule(),
                new AtlasSourceTraceBackedBySourceRule(),
                new AtlasSourceTraceApplicationBusinessRule()
        );
    }
}
