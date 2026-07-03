package com.ai.skills.atlas.registry;

import com.ai.skills.atlas.rules.AtlasValidationRule;
import com.ai.skills.atlas.rules.business.trace.AtlasLayerTraceConcreteEvidenceRule;
import com.ai.skills.atlas.rules.business.trace.AtlasLayerTraceMapRule;
import com.ai.skills.atlas.rules.business.trace.AtlasMappingDepthRule;
import com.ai.skills.atlas.rules.business.trace.AtlasSourceTraceApplicationBusinessRule;
import com.ai.skills.atlas.rules.business.trace.AtlasSourceTraceBackedBySourceRule;
import com.ai.skills.atlas.rules.business.trace.AtlasSourceTraceMethodLevelRule;

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
